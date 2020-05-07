package tschipp.buildersbag.common.crafting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import tschipp.buildersbag.common.helper.Tuple;

public class RecipeTree
{
	private Map<String, Set<RecipeNode>> nodes = new HashMap<String, Set<RecipeNode>>();
	private Map<String, RecipeNode> stackAvailableNodes = new HashMap<String, RecipeNode>();

	public void add(IRecipe recipe)
	{
		ItemStack output = recipe.getRecipeOutput();
		if (!output.isEmpty())
		{
			NonNullList<Ingredient> ingredients = recipe.getIngredients();

			String outputString = CraftingHandler.getItemString(output);
			RecipeContainer cont = new RecipeContainer(ingredients, output, CraftingHandler.getTierIfStaged(recipe));

			RecipeNode outputNode = getNodeExact(output);
			if (outputNode == null)
			{
				outputNode = new RecipeNode(outputString);
				putNode(outputString, outputNode);
			}

			for (Ingredient ing : ingredients)
			{
				if (ing.getMatchingStacks().length == 0)
					continue;

				String ingString = CraftingHandler.getIngredientString(ing);

				RecipeNode ingNode = getNodeExact(ing);
				if (ingNode == null)
				{
					ingNode = new RecipeNode(ingString);
					putNode(ingString, ingNode);
				}

				ingNode.add(outputNode, cont);

				for (ItemStack ingStack : ing.getMatchingStacks())
				{
					String ingStackString = CraftingHandler.getItemString(ingStack);

					putNode(ingStackString, ingNode);

				}

			}
		}
	}

	public RecipeTree getSubtree(NonNullList<ItemStack> stacks)
	{
		RecipeTree subtree = new RecipeTree();

		for (ItemStack stack : stacks)
		{
			if (!stacks.isEmpty())
			{
				Set<RecipeNode> nodes = getNodes(CraftingHandler.getItemString(stack));
				for (RecipeNode node : nodes)
				{
					subtree.stackAvailableNodes.put(node.id, node);
					subtree.putNode(node.id, node);
					subtree.addNodesRecursively(node, this);
				}
			}
		}

		// Do this 6 times, so that it's pretty certain that all illegal blocks
		// are removed
		for (int i = 0; i < 6; i++)
		{
			Map<String, Set<RecipeNode>> nodeCopy = new HashMap<String, Set<RecipeNode>>();
			nodeCopy.putAll(subtree.nodes);

			List<String> availableStacks = new ArrayList<String>();
			for (ItemStack st : stacks)
				availableStacks.add(CraftingHandler.getItemString(st));

			for (Set<RecipeNode> nodeSet : nodeCopy.values())
			{
				for (RecipeNode node : nodeSet)
				{
					boolean containsAll = true;

					for (Tuple<RecipeNode, RecipeContainer> parent : node.parentNodes)
					{
						RecipeNode parentNode = parent.getFirst();

						if (subtree.nodes.get(parentNode.id) == null && !availableStacks.contains(node.id))
						{
							containsAll = false;
							break;
						}
					}

					if (node.parentNodes.isEmpty())
					{
						boolean hasAny = false;
						for (String av : availableStacks)
							if (node.id.contains(av))
							{
								hasAny = true;
								break;
							}

						if (!hasAny)
						{
							String[] split = node.id.split(";");
							for (String spl : split)
							{
								if (subtree.nodes.get(spl + ";") != null)
								{
									hasAny = true;
									break;
								}
							}
						}

						if (!hasAny)
							containsAll = false;
					}

					if (!containsAll)
					{
						subtree.nodes.remove(node.id);
						continue;
					}
				}
			}
		}

		return subtree;
	}

	public NonNullList<ItemStack> getPossibleStacks()
	{
		NonNullList<ItemStack> stacks = NonNullList.create();

		for (Set<RecipeNode> nodeSet : nodes.values())
		{
			for (RecipeNode node : nodeSet)
			{
				if (stackAvailableNodes.get(node.id) == null)
				{
					String[] split = node.id.split(";");

					if (split.length <= 1)
						stacks.add(CraftingHandler.getItemFromString(node.id));
				}
			}
		}

		return stacks;
	}

	public RecipeTree getRecipeTree(ItemStack requested)
	{
		RecipeTree recipeTree = new RecipeTree();
		RecipeNode node = getNodeExact(requested);

		if (node == null)
			return recipeTree;

		recipeTree.putNode(node.id, node);
		recipeTree.addPredecessorRecursively(node);

		return recipeTree;
	}

	private void addPredecessorRecursively(RecipeNode node)
	{
		for (Tuple<RecipeNode, RecipeContainer> parent : node.parentNodes)
		{
			if (this.nodes.get(parent.getFirst().id) == null)
			{
				this.putNode(parent.getFirst().id, parent.getFirst());
				this.addPredecessorRecursively(parent.getFirst());
			}
		}
	}

	private void addNodesRecursively(RecipeNode node, RecipeTree parentTree)
	{
		for (Tuple<RecipeNode, RecipeContainer> adjacent : node.adjacentNodes)
		{
			RecipeNode adjacentNode = adjacent.getFirst();
			for (RecipeNode n : parentTree.getNodes(adjacentNode.id))
			{
				if (nodes.get(n.id) == null)
				{
					this.putNode(n.id, n);
					this.addNodesRecursively(n, parentTree);
				}
			}
		}
	}

	public RecipeNode getNodeExact(ItemStack stack)
	{
		String itemString = CraftingHandler.getItemString(stack);
		Set<RecipeNode> rnodes = nodes.get(itemString);
		if (rnodes == null)
			return null;

		for (RecipeNode node : rnodes)
			if (node.id.equals(itemString))
				return node;

		return null;
	}

	private Set<RecipeNode> getNodes(String key)
	{
		Set<RecipeNode> rnodes = nodes.get(key);
		if (rnodes == null)
			return Collections.EMPTY_SET;

		return rnodes;
	}

	private RecipeNode getNodeExact(Ingredient ing)
	{
		String itemString = CraftingHandler.getIngredientString(ing);
		Set<RecipeNode> rnodes = nodes.get(itemString);
		if (rnodes == null)
			return null;

		for (RecipeNode node : rnodes)
			if (node.id.equals(itemString))
				return node;

		return null;
	}

	private void putNode(String key, RecipeNode node)
	{
		Set<RecipeNode> lnodes = nodes.get(key);
		if (lnodes == null)
			lnodes = new HashSet<RecipeNode>();

		lnodes.add(node);

		nodes.put(key, lnodes);
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
	}

}
