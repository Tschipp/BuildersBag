package tschipp.buildersbag.common.crafting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.data.Tuple;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.compat.gamestages.StageHelper;

//Don't touch this class, just hope that it works
public class RecipeTree
{
	private Map<String, RecipeNode> nodes = new HashMap<String, RecipeNode>();
	private Map<String, RecipeNode> rootNodes = new HashMap<String, RecipeNode>();
	private Map<String, Boolean> markedNodes = new HashMap<String, Boolean>();
	private Map<String, Boolean> validatedNodes = new HashMap<String, Boolean>();
	private Map<String, Boolean> invalidatedNodes = new HashMap<String, Boolean>();
	private Stack<String> callStack = new Stack();
	private boolean aborted = false;

	public Set<RecipeContainer> blacklistedRecipes = new HashSet<RecipeContainer>();

	public void add(IRecipe recipe)
	{
		ItemStack output = recipe.getRecipeOutput();
		if (!output.isEmpty())
		{
			NonNullList<Ingredient> ingredients = recipe.getIngredients();

			String outputString = CraftingHandler.getItemString(output);
			RecipeContainer cont = new RecipeContainer(ingredients, output, CraftingHandler.getTierIfStaged(recipe));

			RecipeNode outputNode = nodes.get(outputString);
			if (outputNode == null)
			{
				outputNode = new RecipeNode(outputString);
				nodes.put(outputString, outputNode);
			}

			String oreOutputString = CraftingHandler.getStackIngredientString(output, false);
			RecipeNode altOutputNode = nodes.get(oreOutputString);
			if (altOutputNode == null)
			{
				altOutputNode = new RecipeNode(outputString);
				nodes.put(oreOutputString, altOutputNode);
			}

			for (Ingredient ing : ingredients)
			{
				if (ing.getMatchingStacks().length == 0)
					continue;

				CraftingHandler.addIngredientIfAlternative(ing);

				String ingString = CraftingHandler.getIngredientString(ing);

				RecipeNode node = this.nodes.get(ingString);
				if (node == null)
				{
					node = new RecipeNode(ingString);
					nodes.put(ingString, node);
				}
				node.add(outputNode, cont);
				node.add(altOutputNode, cont);
			}
		}
	}

	@Nullable
	public RecipeRequirementList generateRequirementList(String toCreate, @Nullable RecipeRequirementList reqList, EntityPlayer player, IBagCap cap)
	{
		RecipeNode toCreateNode = this.nodes.get(toCreate);
		RecipeNode parentNode = null;
		RecipeContainer fastestRecipe = null;

		if (aborted)
			return reqList;

		if (reqList == null)
		{
			validatedNodes.clear();
			invalidatedNodes.clear();
			callStack.clear();
			this.aborted = false;
		}

		if (toCreateNode == null)
			return reqList;

		if (this.invalidatedNodes.containsKey(toCreateNode.id))
			return reqList;

		markedNodes.clear();

		Queue<RecipeNode> bfsqueue = new ArrayDeque<RecipeNode>();
		bfsqueue.add(toCreateNode);

		markedNodes.put(toCreateNode.id, true);

		while (!bfsqueue.isEmpty()) // Go up the tree until we find the furthest parent
		{
			RecipeNode current = bfsqueue.poll();
			for (Tuple<RecipeNode, RecipeContainer> parent : current.parentNodes)
			{
				if (this.blacklistedRecipes.contains(parent.getSecond()))
					continue;

				if (!StageHelper.hasStage(player, parent.getSecond().getStage()))
					continue;

				RecipeNode p = parent.getFirst();
				if (!markedNodes.containsKey(p.id))
				{
					if(p.id.split(";").length > 1)
					{
						parentNode = p;
						markedNodes.put(p.id, true);
						break;
					}
					
					if (this.rootNodes.containsKey(p.id))
					{
						parentNode = p;
					}
					bfsqueue.add(p);
					markedNodes.put(p.id, true);
				}
			}
		}

		if (parentNode == null)
			return reqList;

		bfsqueue.clear();
		bfsqueue.add(parentNode);
		invalidatedNodes.put(parentNode.id, true);
		markedNodes.clear();

		findcreate: while (!bfsqueue.isEmpty()) // Go down the tree until we find the requested node
		{
			RecipeNode current = bfsqueue.poll();
			for (Tuple<RecipeNode, RecipeContainer> child : current.adjacentNodes)
			{
				if (this.blacklistedRecipes.contains(child.getSecond()))
					continue;

				if (!StageHelper.hasStage(player, child.getSecond().getStage()))
					continue;

				RecipeNode c = child.getFirst();

				if (!markedNodes.containsKey(c.id))
				{
					if (toCreateNode.id.equals(c.id))
					{
						fastestRecipe = child.getSecond();
						break findcreate;
					}
					bfsqueue.add(c);
					markedNodes.put(c.id, true);
				}
			}
		}

		if (fastestRecipe == null)
			return null;

		int creationCount = fastestRecipe.getOutput().getCount();

		boolean isRoot = reqList == null;

		if (reqList == null)
			reqList = new RecipeRequirementList(this, CraftingHandler.getItemFromString(toCreate), creationCount, fastestRecipe);

		if (isRoot)
			validatedNodes.clear();

		reqList.setCreationRecipe(CraftingHandler.getItemFromString(toCreate), fastestRecipe);

		top: for (Tuple<RecipeNode, RecipeContainer> parent : toCreateNode.parentNodes) // Generate the required item amounts (excluding roots)
		{
			if (parent.getSecond() == fastestRecipe)
			{
				boolean isRootElement = this.rootNodes.containsKey(parent.getFirst().id);

				String requirementNode = parent.getFirst().id;
				
				String[] split = requirementNode.split(";");
				boolean added = false;
				for (String str : split)
				{
					RecipeNode n = this.nodes.get(str + ";");
					if (n != null)
					{						
						reqList.addOreReplacement(requirementNode, str + ";");
					}
				}

				double ingCount = 0;
				for (Ingredient ing : parent.getSecond().getIngredients())
				{
					if (CraftingHandler.getIngredientString(ing).equals(requirementNode))
					{
						ingCount += 1;
					}
				}

				reqList.addItemRequirement(toCreate, requirementNode, ingCount / creationCount, isRoot);

				boolean wasValidated = this.validatedNodes.containsKey(requirementNode);
				boolean inQueue = this.callStack.contains(requirementNode);

				if (inQueue)
				{
					reqList.removeItemRequirement(requirementNode);
					continue top;
				}

				callStack.add(parent.getFirst().id);

				if (!wasValidated)
					this.generateRequirementList(requirementNode, reqList, player, cap);

				callStack.pop();

				this.validatedNodes.put(parent.getFirst().id, true);

				continue top;
			}
		}

		if (isRoot && aborted)
		{
			this.aborted = false;
			reqList = this.generateRequirementList(toCreate, null, player, cap);
		}

		if (isRoot && reqList != null)
			reqList.finalizeRequirements(player, cap);

		return reqList;
	}

	private boolean hasEnoughMaterialsForRoot(RecipeNode p, RecipeContainer recipeContainer, EntityPlayer player, IBagCap cap)
	{
		String[] split = p.id.split(";");
		for (String str : split)
		{
			ItemStack stack = CraftingHandler.getItemFromString(str + ";");

			double ingCount = 0;
			for (Ingredient ing : recipeContainer.getIngredients())
			{
				if (ing.test(stack))
				{
					ingCount += 1;
				}
			}

			int provided = InventoryHelper.getMatchingStacksWithSizeOne(stack, InventoryHelper.getInventoryStacks(cap, player)).size();

			if (provided >= ingCount)
				return true;
		}

		return false;
	}

	public RecipeTree getSubtree(NonNullList<ItemStack> stacks)
	{
		RecipeTree subtree = new RecipeTree();

		long time = System.currentTimeMillis();

		for (ItemStack stack : stacks)
		{
			if (!stack.isEmpty())
			{
				String[] names = CraftingHandler.getStackIngredientStrings(stack, true);

				RecipeNode n = this.nodes.get(CraftingHandler.getItemString(stack));
				if (n != null)
					subtree.rootNodes.put(n.id, n);

				for (String name : names)
				{
					RecipeNode node = this.nodes.get(name);

					if (node != null && !subtree.nodes.containsKey(node.id))
					{
						subtree.rootNodes.put(node.id, node);
						subtree.nodes.put(name, node);
						subtree.addNodesRecursively(node, this);

					}
				}
			}
		}

		List<String> availableStacks = new ArrayList<String>();
		for (ItemStack st : stacks)
			availableStacks.add(CraftingHandler.getItemString(st));

		time = System.currentTimeMillis();

		int lastNodeSize = 0;

		// Do this 6 times or less, so that it's pretty certain that all illegal blocks are removed
		for (int i = 0; i < 6; i++)
		{
			Set<RecipeNode> nodeCopy = new HashSet<RecipeNode>();
			nodeCopy.addAll(subtree.nodes.values());

			for (RecipeNode node : nodeCopy)
			{
				if (!node.parentNodes.isEmpty())
				{
					subtree.checkDependenciesRecursively(node, null, new ArrayList<RecipeContainer>());
				}
				else
				{
					boolean hasAny = false;
					for (String av : availableStacks)
					{
						if (node.id.contains(av))
						{
							hasAny = true;
							break;
						}
					}

					if (!hasAny)
					{
						subtree.nodes.remove(node.id);
					}
				}

			}

			if (subtree.nodes.size() == lastNodeSize)
				break;

			lastNodeSize = subtree.nodes.size();
		}

		return subtree;
	}

	public NonNullList<ItemStack> getPossibleStacks(boolean removeAvailable) // RemoveAvailable just means that it should not show blocks that already exist in this list
	{
		NonNullList<ItemStack> stacks = NonNullList.create();

		for (RecipeNode node : nodes.values())
		{
			if (removeAvailable ? rootNodes.get(node.id) == null : true)
			{
				String[] split = node.id.split(";");

				if (split.length <= 1)
					stacks.add(CraftingHandler.getItemFromString(node.id));
			}
		}

		return stacks;
	}

	public RecipeTree getRecipeTree(ItemStack requested)
	{
		RecipeTree recipeTree = new RecipeTree();
		RecipeNode node = nodes.get(CraftingHandler.getItemString(requested));

		if (node == null)
			return recipeTree;

		recipeTree.nodes.put(node.id, node);
		recipeTree.addPredecessorRecursively(node);

		return recipeTree;
	}

	private void addPredecessorRecursively(RecipeNode node)
	{
		for (Tuple<RecipeNode, RecipeContainer> parent : node.parentNodes)
		{
			if (this.nodes.get(parent.getFirst().id) == null)
			{
				this.nodes.put(parent.getFirst().id, parent.getFirst());
				this.addPredecessorRecursively(parent.getFirst());
			}
		}
	}

	private boolean checkDependenciesRecursively(RecipeNode node, RecipeContainer callingRecipe, List<RecipeContainer> excludeRecipes)
	{
		if (node == null)
			return false;

		if (this.invalidatedNodes.containsKey(node.id)) // Node has been confirmed to be invalidated
			return false;

		if (this.validatedNodes.containsKey(node.id)) // If we have already checked this node, return true
			return true;

		if (this.rootNodes.containsKey(node.id)) // If this node is a provided root, return true
			return true;

		if (this.markedNodes.containsKey(node.id)) // If this node is marked, which means that is is part of the recursion stack, return false.
		{
			excludeRecipes.add(callingRecipe);
			if (checkRecipes(node, excludeRecipes))
			{
				this.validatedNodes.put(node.id, true);
				excludeRecipes.clear();
				return true;
			}
			this.invalidatedNodes.put(node.id, true);
			return false;
		}

		this.markedNodes.put(node.id, true); // Mark this node as active

		boolean hasValidRecipe = checkRecipes(node, excludeRecipes);

		this.markedNodes.remove(node.id); // Unmark the node as active

		if (!hasValidRecipe)
		{
			nodes.remove(node.id); // If there's no valid recipe for the item, remove it from the tree
			this.invalidatedNodes.put(node.id, true);
			return false;
		}

		this.validatedNodes.put(node.id, true); // There is a valid recipe, so mark this node as valid.
		return true;
	}

	private boolean checkRecipes(RecipeNode node, List<RecipeContainer> exclude)
	{
		if (this.invalidatedNodes.containsKey(node.id)) // Node has been confirmed to be invalidated
			return false;

		if (this.validatedNodes.containsKey(node.id)) // If we have already checked this node, return true
			return true;

		if (this.rootNodes.containsKey(node.id)) // If this node is a provided root, return true
			return true;

		Map<RecipeContainer, Boolean> validRecipeMap = new HashMap<RecipeContainer, Boolean>();

		for (Tuple<RecipeNode, RecipeContainer> parent : node.parentNodes)
		{
			RecipeNode parentNode = parent.getFirst();
			RecipeContainer parentRecipe = parent.getSecond();

			if (exclude.contains(parentRecipe))
				continue;

			Boolean prevRecipeVal = validRecipeMap.get(parentRecipe);
			if (prevRecipeVal == null)
				validRecipeMap.put(parent.getSecond(), true);
			else if (prevRecipeVal != null && !prevRecipeVal)
				continue; // This recipe is invalid

			if (checkDependenciesRecursively(parentNode, parentRecipe, exclude))
				continue;

			boolean hasAny = false;
			String[] split = parentNode.id.split(";");
			for (String spl : split)
			{
				if (checkDependenciesRecursively(nodes.get(spl + ";"), parentRecipe, exclude))
				{
					hasAny = true;
					break;
				}
			}

			if (!hasAny)
				validRecipeMap.put(parent.getSecond(), false); // mark the recipe as impossible
		}

		boolean hasValidRecipe = false;
		for (boolean b : validRecipeMap.values())
		{
			hasValidRecipe |= b;
		}

		return hasValidRecipe;
	}

	private void addNodesRecursively(RecipeNode node, RecipeTree parentTree)
	{
		for (Tuple<RecipeNode, RecipeContainer> adjacent : node.adjacentNodes)
		{
			RecipeNode adjacentNode = adjacent.getFirst();

			RecipeNode n = parentTree.nodes.get(adjacentNode.id);

			if (nodes.get(n.id) == null)
			{
				this.nodes.put(n.id, n);
				this.addNodesRecursively(n, parentTree);

				String[] split = n.id.split(";");
				if (split.length == 1)
				{
					String[] altNames = CraftingHandler.getStackIngredientStrings(CraftingHandler.getItemFromString(split[0] + ";"), false);
					for (String alt : altNames)
					{
						RecipeNode ni = parentTree.nodes.get(alt);
						if (ni != null)
						{
							if (nodes.get(ni.id) == null)
							{
								this.nodes.put(ni.id, ni);
								this.addNodesRecursively(ni, parentTree);
							}
						}
					}
				}
			}
		}
	}

	private void removeNodesRecursively(RecipeNode node)
	{

		for (Tuple<RecipeNode, RecipeContainer> adjacent : node.adjacentNodes)
		{
			RecipeNode adjacentNode = adjacent.getFirst();

			if (nodes.get(adjacentNode.id) != null)
			{
				this.nodes.remove(adjacentNode.id);
				this.removeNodesRecursively(adjacentNode);
			}
		}
	}

	public static class RecipeNode
	{
		public String id;
		public Set<Tuple<RecipeNode, RecipeContainer>> adjacentNodes;
		public Set<Tuple<RecipeNode, RecipeContainer>> parentNodes;

		private RecipeNode(String id)
		{
			this.id = id;
			this.adjacentNodes = new HashSet<Tuple<RecipeNode, RecipeContainer>>();
			this.parentNodes = new HashSet<Tuple<RecipeNode, RecipeContainer>>();
		}

		private void add(RecipeNode n, RecipeContainer c)
		{
			this.adjacentNodes.add(new Tuple<RecipeNode, RecipeContainer>(n, c));
			n.parentNodes.add(new Tuple<RecipeNode, RecipeContainer>(this, c));
		}

		@Override
		public String toString()
		{
			return id;
		}
	}

	/**
	 * Only used to debug I hope I never have to debug this shit again.
	 */
	public void visualize()
	{
		File output = new File("recipetree.txt");
		try
		{
			FileWriter writer = new FileWriter(output);
			writer.write("digraph G {\n");
			// writer.write("graph [ dpi = 75 ];");
			// writer.write("ranksep = 1\n");

			List<RecipeNode> drawn = new ArrayList<RecipeNode>();

			for (RecipeNode node : nodes.values())
			{
				for (Tuple<RecipeNode, RecipeContainer> adj : node.adjacentNodes)
				{
					if (this.nodes.get(adj.getFirst().id) == null)
						continue;

					writer.write("\"" + node.id + "\" -> \"" + adj.getFirst().id + "\"\n");
				}

			}

			writer.write("}");

			writer.close();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
