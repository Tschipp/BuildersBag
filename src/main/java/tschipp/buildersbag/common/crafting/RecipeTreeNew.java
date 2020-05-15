package tschipp.buildersbag.common.crafting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.helper.Tuple;

//Don't touch this class, just hope that it works
public class RecipeTreeNew
{
	private Map<String, RecipeNode> nodes = new HashMap<String, RecipeNode>();
	private Map<String, RecipeNode> stackAvailableNodes = new HashMap<String, RecipeNode>();
	private Map<String, Boolean> markedNodes = new HashMap<String, Boolean>();
	private Map<String, Boolean> validatedNodes = new HashMap<String, Boolean>();
	private Map<String, Boolean> invalidatedNodes = new HashMap<String, Boolean>();

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
			}
		}
	}

	public RecipeTreeNew getSubtree(NonNullList<ItemStack> stacks)
	{
		RecipeTreeNew subtree = new RecipeTreeNew();

		long time = System.currentTimeMillis();

		for (ItemStack stack : stacks)
		{
			if (!stack.isEmpty())
			{
				String[] names = CraftingHandler.getStackIngredientStrings(stack, true);

				for (String name : names)
				{
					RecipeNode node = this.nodes.get(name);

					if (node != null && !subtree.nodes.containsKey(node.id))
					{
						subtree.stackAvailableNodes.put(node.id, node);
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

//		subtree.visualize();

		return subtree;
	}

	public NonNullList<ItemStack> getPossibleStacks(boolean removeAvailable) // RemoveAvailable just means that it should not show blocks that already exist in this list

	{
		NonNullList<ItemStack> stacks = NonNullList.create();

		for (RecipeNode node : nodes.values())
		{
			if (removeAvailable ? stackAvailableNodes.get(node.id) == null : true)
			{
				String[] split = node.id.split(";");

				if (split.length <= 1)
					stacks.add(CraftingHandler.getItemFromString(node.id));
			}
		}

		return stacks;
	}

	public RecipeTreeNew getRecipeTree(ItemStack requested)
	{
		RecipeTreeNew recipeTree = new RecipeTreeNew();
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

		if (this.invalidatedNodes.containsKey(node.id)) //Node has been confirmed to be invalidated
			return false;
		
		if (this.validatedNodes.containsKey(node.id)) // If we have already checked this node, return true
			return true;

		if (this.stackAvailableNodes.containsKey(node.id)) // If this node is a provided root, return true
			return true;

		if (this.markedNodes.containsKey(node.id)) // If this node is marked, which means that is is part of the recursion stack, return false.
		{
			excludeRecipes.add(callingRecipe);
			if(checkRecipes(node, excludeRecipes))
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
		if (this.invalidatedNodes.containsKey(node.id)) //Node has been confirmed to be invalidated
			return false;
		
		if (this.validatedNodes.containsKey(node.id)) // If we have already checked this node, return true
			return true;
		
		if (this.stackAvailableNodes.containsKey(node.id)) // If this node is a provided root, return true
			return true;
		
		Map<RecipeContainer, Boolean> validRecipeMap = new HashMap<RecipeContainer, Boolean>();

		for (Tuple<RecipeNode, RecipeContainer> parent : node.parentNodes)
		{
			RecipeNode parentNode = parent.getFirst();
			RecipeContainer parentRecipe = parent.getSecond();
			
			if(exclude.contains(parentRecipe))
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

	private void addNodesRecursively(RecipeNode node, RecipeTreeNew parentTree)
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
	 * Only used to debug
	 * I hope I never have to debug this shit again.
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
