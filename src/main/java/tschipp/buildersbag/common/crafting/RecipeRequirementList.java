package tschipp.buildersbag.common.crafting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Stack;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.data.ItemContainer;
import tschipp.buildersbag.common.data.Tuple;
import tschipp.buildersbag.common.helper.InventoryHelper;

public class RecipeRequirementList
{
	private RecipeTreeNew tree;

	private Map<ItemContainer, Double> totalRequirements = new HashMap<ItemContainer, Double>();
	private Map<ItemContainer, Requirement> itemRequirements = new HashMap<ItemContainer, Requirement>();
	private Map<ItemContainer, RecipeContainer> recipes = new HashMap<ItemContainer, RecipeContainer>();

	private ItemStack createdItem;
	private int creationCount;
	private RecipeContainer creationRecipe;

	boolean invalid = false;

	public RecipeRequirementList(RecipeTreeNew tree, ItemStack createdItem, int creationCount, RecipeContainer creationRecipe)
	{
		this.tree = tree;
		this.createdItem = createdItem;
		this.creationCount = creationCount;
		this.creationRecipe = creationRecipe;
	}

	public void addItemRequirement(ItemStack forStack, ItemStack stack, double count, boolean root)
	{
		ItemContainer cont = ItemContainer.forStack(forStack);

		Requirement requirement = itemRequirements.get(cont);
		if (requirement == null)
			requirement = new Requirement();

		requirement.addItemRequirement(stack, count);

		itemRequirements.put(cont, requirement);

		if (root)
		{
			cont = ItemContainer.forStack(stack);
			Double i = totalRequirements.get(cont);
			if (i == null)
				i = 0.0;
			i += count;
			totalRequirements.put(cont, i);
		}
	}

	public void removeItemRequirement(ItemStack forStack)
	{
		itemRequirements.remove(ItemContainer.forStack(forStack));
	}

	public boolean containsItemRequirement(String key)
	{
		ItemStack s = CraftingHandler.getItemFromString(key);
		return itemRequirements.containsKey(ItemContainer.forStack(s));
	}

	public void setCreationRecipe(ItemStack forStack, RecipeContainer recipe)
	{
		ItemContainer cont = ItemContainer.forStack(forStack);

		recipes.put(cont, recipe);
	}

	public void blacklist(RecipeContainer recipe)
	{
		this.tree.blacklistedRecipes.add(recipe);
	}

	public void finalizeRequirements(EntityPlayer player, IBagCap bag)
	{
		Map<ItemContainer, Double> moreRequirements = new HashMap<ItemContainer, Double>();

		for (Entry<ItemContainer, Double> entry : totalRequirements.entrySet())
		{
			addReqRecursively(moreRequirements, entry.getKey(), entry.getValue(), new ArrayDeque<ItemContainer>());
		}

		for (Entry<ItemContainer, Double> entry : moreRequirements.entrySet())
		{
			Double d = totalRequirements.get(entry.getKey());
			if (d == null)
				d = 0.0;
			d += entry.getValue();
			totalRequirements.put(entry.getKey(), d);
		}
	}

	private Map<ItemContainer, Integer> generateExactRequirementList(int amountToCraft, EntityPlayer player, IBagCap bag)
	{
		int craftingOps = (int) Math.ceil(((double) amountToCraft) / creationCount);
		int totalItemsCrafted = craftingOps * creationCount;

		Map<ItemContainer, Integer> exact = new HashMap<ItemContainer, Integer>();

		for (Entry<ItemContainer, Double> entry : totalRequirements.entrySet())
		{
			RecipeContainer r = this.recipes.get(entry.getKey());
			if (r != null)
			{
				ItemStack out = r.getOutput();
				int outCount = out.getCount();

				int totalItemsAlreadyThere = InventoryHelper.getMatchingStacksWithSizeOne(out, InventoryHelper.getInventoryStacks(bag, player)).size();

				double minNeeded = (entry.getValue() * totalItemsCrafted);
				int recipeOps = (int) Math.ceil(((double) minNeeded) / outCount);
				int actualNeeded = recipeOps * outCount;

				exact.put(entry.getKey(), Math.max(actualNeeded - totalItemsAlreadyThere, 0));
			}
		}

		return exact;
	}

	public CraftingOrderList generateCraftingOrderList(int amountToCraft, EntityPlayer player, IBagCap bag)
	{
		CraftingOrderList orderList = new CraftingOrderList();

		if (invalid)
			return orderList;

		int craftingOps = (int) Math.ceil(((double) amountToCraft) / creationCount);
		int totalItemsCrafted = craftingOps * creationCount;

		Map<ItemContainer, Integer> exactRequirements = generateExactRequirementList(amountToCraft, player, bag);

		Map<ItemContainer, Requirement> requirementHierarchy = new HashMap<ItemContainer, Requirement>();
		for (Entry<ItemContainer, Requirement> e : itemRequirements.entrySet())
		{
			if (totalRequirements.containsKey(e.getKey()) || e.getKey().equals(ItemContainer.forStack(createdItem)))
				requirementHierarchy.put(e.getKey(), e.getValue().clone());
		}

		do
		{
			for (Entry<ItemContainer, Double> entry : totalRequirements.entrySet())
			{
				if (!requirementHierarchy.containsKey(entry.getKey()))
				{
					RecipeContainer recipe = recipes.get(entry.getKey());

					if (recipe == null)
						recipe = new RecipeContainerProvided(entry.getKey().getItem());

					int outputAmount = recipe.getOutput().getCount();

					boolean exact = exactRequirements.containsKey(entry.getKey());

					double totalItemsNeededCount = exact ? exactRequirements.get(entry.getKey()) : totalItemsCrafted * entry.getValue();
					int recipeItemsAlreadyThere = orderList.getToBeCreatedItems(recipe.getOutput());
					int totalItemsAlreadyThere = InventoryHelper.getMatchingStacksWithSizeOne(recipe.getOutput(), InventoryHelper.getInventoryStacks(bag, player)).size() + recipeItemsAlreadyThere;
					int recipeCraftingOps = (int) Math.ceil(((exact ? (totalItemsNeededCount - recipeItemsAlreadyThere) : (totalItemsNeededCount * outputAmount) - totalItemsAlreadyThere)) / outputAmount);

					if (recipeCraftingOps > 0 && !(recipe instanceof RecipeContainerProvided))
						orderList.addRecipe(recipe, recipeCraftingOps);

					List<ItemContainer> toRemove = new ArrayList<ItemContainer>();

					for (Entry<ItemContainer, Requirement> h : requirementHierarchy.entrySet())
					{
						Requirement req = h.getValue();
						req.req.remove(entry.getKey());
						if (req.req.isEmpty())
						{
							toRemove.add(h.getKey());
						}
					}

					for (ItemContainer ic : toRemove)
						requirementHierarchy.remove(ic);
				}
			}
		}
		while (!requirementHierarchy.isEmpty());

		orderList.addRecipe(this.recipes.get(ItemContainer.forStack(createdItem)), craftingOps);
		return orderList;
	}

	private void addReqRecursively(Map<ItemContainer, Double> map, ItemContainer forItem, double count, Queue<ItemContainer> stack)
	{
		Requirement req = itemRequirements.get(forItem);
		if (req != null)
		{
			for (Entry<ItemContainer, Double> entry : req.req.entrySet())
			{
				Double i = map.get(entry.getKey());
				if (i == null)
					i = 0.0;
				i += entry.getValue() * count;
				map.put(entry.getKey(), i);

				// if (containsCircle(stack))
				// {
				// tree.blacklistedRecipes.add(this.recipes.get(entry.getKey()));
				// this.invalid = true;
				// return;
				// }

				stack.add(entry.getKey());
				addReqRecursively(map, entry.getKey(), entry.getValue() * count, stack);
			}
		}
	}

	private boolean containsCircle(Queue<ItemContainer> stack)
	{
		if (stack.isEmpty())
			return false;

		Queue<ItemContainer> s = new ArrayDeque();
		s.addAll(stack);
		ItemContainer start = s.remove();

		Stack<ItemContainer> loop = new Stack();

		loop.add(start);

		while (!s.isEmpty())
		{
			ItemContainer ic = s.remove();
			if (ic.equals(start))
			{
				loop.addAll(loop);

				boolean isLoop = true;

				Queue<ItemContainer> sCopy = new ArrayDeque(stack);

				for (ItemContainer c : loop)
				{
					if (sCopy.isEmpty())
					{
						isLoop = false;
						break;
					}

					if (!sCopy.remove().equals(c))
						isLoop = false;
				}

				return isLoop;
			}
			else
				loop.add(ic);
		}

		return false;
	}

	public void merge(RecipeRequirementList other)
	{
		this.recipes.putAll(other.recipes);
		this.itemRequirements.putAll(other.itemRequirements);

		for (Entry<ItemContainer, Double> entry : other.totalRequirements.entrySet())
		{
			Double d = this.totalRequirements.get(entry.getKey());
			if (d == null)
				d = 0.0;
			this.totalRequirements.put(entry.getKey(), d + entry.getValue());
		}
	}

	public static class Requirement
	{
		private Map<ItemContainer, Double> req = new HashMap<ItemContainer, Double>();

		public void addItemRequirement(ItemStack stack, double count)
		{
			ItemContainer cont = ItemContainer.forStack(stack);

			Double i = req.get(cont);
			if (i == null)
				i = 0.0;
			i += count;

			req.put(cont, i);
		}

		@Override
		public Requirement clone()
		{
			Requirement r = new Requirement();
			r.req = Maps.newHashMap(this.req);
			return r;
		}
	}

	public static class CraftingOrderList
	{
		private Map<RecipeContainer, Integer> recipeAmounts = new HashMap<RecipeContainer, Integer>();
		private Queue<RecipeContainer> recipeQueue = new LinkedList<RecipeContainer>();

		public void addRecipe(RecipeContainer recipe, int craftCount)
		{
			recipeAmounts.put(recipe, craftCount);
			recipeQueue.add(recipe);
		}

		public boolean hasNext()
		{
			return !recipeQueue.isEmpty();
		}

		public Tuple<RecipeContainer, Integer> getNextRecipe()
		{
			RecipeContainer recipe = recipeQueue.poll();
			int amount = recipeAmounts.get(recipe);

			return new Tuple(recipe, amount);
		}

		public int getToBeCreatedItems(ItemStack stack)
		{
			int count = 0;
			for (RecipeContainer c : recipeQueue)
			{
				if (ItemStack.areItemsEqual(c.getOutput(), stack))
				{
					count += c.getOutput().getCount() * recipeAmounts.get(c);
				}
			}
			return count;
		}
	}
}
