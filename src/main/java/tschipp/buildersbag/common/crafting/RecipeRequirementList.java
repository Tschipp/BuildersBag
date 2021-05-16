package tschipp.buildersbag.common.crafting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.datastructures.ItemContainer;
import tschipp.buildersbag.common.helper.InventoryHelper;

public class RecipeRequirementList
{
	private RecipeTree tree;

	private Map<ItemContainer, Double> totalRequirements = new HashMap<ItemContainer, Double>();
	private Map<ItemContainer, Requirement> itemRequirements = new HashMap<ItemContainer, Requirement>();
	private Map<ItemContainer, RecipeContainer> recipes = new HashMap<ItemContainer, RecipeContainer>();

	private ItemStack createdItem;
	private int creationCount;
	private RecipeContainer creationRecipe;

	private Map<ItemContainer, Set<ItemContainer>> validIngredientReplacements = new HashMap<ItemContainer, Set<ItemContainer>>();

	public RecipeRequirementList(RecipeTree tree, ItemStack createdItem, int creationCount, RecipeContainer creationRecipe)
	{
		this.tree = tree;
		this.createdItem = createdItem;
		this.creationCount = creationCount;
		this.creationRecipe = creationRecipe;
	}

	public void addItemRequirement(String forStack, String reqString, double count, boolean root)
	{
		ItemContainer cont = ItemContainer.forIngredient(forStack);

		Requirement requirement = itemRequirements.get(cont);
		if (requirement == null)
			requirement = new Requirement();

		ItemContainer req = ItemContainer.forIngredient(reqString);

		requirement.addItemRequirement(req, count);

		itemRequirements.put(cont, requirement);

		if (root)
		{
			cont = req;
			Double i = totalRequirements.get(cont);
			if (i == null)
				i = 0.0;
			i += count;
			totalRequirements.put(cont, i);
		}
	}

	public void addOreReplacement(String ore, String replacement)
	{
		ItemContainer oreCont = ItemContainer.forIngredient(ore);
		Set<ItemContainer> ores = validIngredientReplacements.get(oreCont);
		if (ores == null)
			ores = new HashSet<ItemContainer>();
		ores.add(ItemContainer.forIngredient(replacement));
		validIngredientReplacements.put(oreCont, ores);
	}

	public void removeItemRequirement(String forStack)
	{
		itemRequirements.remove(ItemContainer.forIngredient(forStack));
	}

	public boolean containsItemRequirement(String key)
	{
		return itemRequirements.containsKey(ItemContainer.forIngredient(key));
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

	public void finalizeRequirements(PlayerEntity player, IBagCap bag)
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

	private Map<ItemContainer, Integer> generateExactRequirementList(int amountToCraft, PlayerEntity player, IBagCap bag)
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
				int recipeOps = (int) Math.ceil((minNeeded) / outCount);
				int actualNeeded = recipeOps * outCount;

				exact.put(entry.getKey(), Math.max(actualNeeded - totalItemsAlreadyThere, 0));
			}
		}

		return exact;
	}

	// public CraftingOrderList generateCraftingOrderList(int amountToCraft, PlayerEntity player, IBagCap bag)
	// {
	// CraftingOrderList orderList = new CraftingOrderList();
	//
	// if (invalid)
	// return orderList;
	//
	// int craftingOps = (int) Math.ceil(((double) amountToCraft) / creationCount);
	// int totalItemsCrafted = craftingOps * creationCount;
	//
	// Map<ItemContainer, Integer> exactRequirements = generateExactRequirementList(amountToCraft, player, bag);
	//
	// Map<ItemContainer, Requirement> requirementHierarchy = new HashMap<ItemContainer, Requirement>();
	// for (Entry<ItemContainer, Requirement> e : itemRequirements.entrySet())
	// {
	// if (totalRequirements.containsKey(e.getKey()) || e.getKey().equals(ItemContainer.forStack(createdItem)))
	// requirementHierarchy.put(e.getKey(), e.getValue().clone());
	// }
	//
	// do
	// {
	// for (Entry<ItemContainer, Double> entry : totalRequirements.entrySet())
	// {
	// if (!requirementHierarchy.containsKey(entry.getKey()))
	// {
	// RecipeContainer recipe = recipes.get(entry.getKey());
	//
	// if (recipe == null)
	// recipe = new RecipeContainerProvided(entry.getKey().getItem());
	//
	// int outputAmount = recipe.getOutput().getCount();
	//
	// boolean exact = exactRequirements.containsKey(entry.getKey());
	//
	// double totalItemsNeededCount = exact ? exactRequirements.get(entry.getKey()) : totalItemsCrafted * entry.getValue();
	// int recipeItemsAlreadyThere = orderList.getToBeCreatedItems(recipe.getOutput());
	// int totalItemsAlreadyThere = InventoryHelper.getMatchingStacksWithSizeOne(recipe.getOutput(), InventoryHelper.getInventoryStacks(bag, player)).size() + recipeItemsAlreadyThere;
	// int recipeCraftingOps = (int) Math.ceil(((exact ? (totalItemsNeededCount - recipeItemsAlreadyThere) : (totalItemsNeededCount * outputAmount) - totalItemsAlreadyThere)) / outputAmount);
	//
	// if (recipeCraftingOps > 0 && !(recipe instanceof RecipeContainerProvided))
	// orderList.addRecipe(recipe, recipeCraftingOps);
	//
	// List<ItemContainer> toRemove = new ArrayList<ItemContainer>();
	//
	// for (Entry<ItemContainer, Requirement> h : requirementHierarchy.entrySet())
	// {
	// Requirement req = h.getValue();
	// req.req.remove(entry.getKey());
	// if (req.req.isEmpty())
	// {
	// toRemove.add(h.getKey());
	// }
	// }
	//
	// for (ItemContainer ic : toRemove)
	// requirementHierarchy.remove(ic);
	// }
	// }
	// }
	// while (!requirementHierarchy.isEmpty());
	//
	// orderList.addRecipe(this.recipes.get(ItemContainer.forStack(createdItem)), craftingOps);
	// return orderList;
	// }

	public CraftingOrderList generateCraftingOrderList(int amountToCraft, PlayerEntity player, IBagCap bag)
	{
		CraftingOrderList orderList = new CraftingOrderList();

		int rootCraftingOps = (int) Math.ceil(((double) amountToCraft) / creationCount);
		int totalItemsCrafted = rootCraftingOps * creationCount;

		orderList.rootItemCrafted = totalItemsCrafted;

		Map<ItemContainer, Requirement> requirementHierarchy = new HashMap<ItemContainer, Requirement>();
		for (Entry<ItemContainer, Requirement> e : itemRequirements.entrySet())
		{
			if (totalRequirements.containsKey(e.getKey()) || e.getKey().equals(ItemContainer.forStack(createdItem)))
				requirementHierarchy.put(e.getKey(), e.getValue().clone());
		}

		NonNullList<ItemStack> bagInventory = NonNullList.create();
		InventoryHelper.getInventoryStacks(bag, player).forEach(stack -> bagInventory.add(stack.copy()));

		Map<ItemContainer, Integer> exactRequirements = this.generateExactRequirementList(amountToCraft, player, bag);

		do
		{
			for (Entry<ItemContainer, Double> entry : totalRequirements.entrySet())
			{
				ItemContainer reqCont = entry.getKey();

				if (!requirementHierarchy.containsKey(reqCont))
				{
					double totalItemsNeededCount = totalItemsCrafted * entry.getValue();

					{
//						if(!exactRequirements.containsKey(reqCont))
//							break checkIngredient;
						
						if (reqCont.isIngredient()) // This requirement is an ingredient and we need to resolve it first
						{
							for (ItemContainer replacement : validIngredientReplacements.get(reqCont))
							{
								int recipeItemsAlreadyThere = orderList.removeCreatedCount(ItemContainer.forStack(replacement.getItem()), (int) totalItemsNeededCount);
								int totalItemsAlreadyThere = InventoryHelper.removeMatchingStacksWithSizeOne(replacement.getItem(), (int) Math.ceil(totalItemsNeededCount), bagInventory).size();

								orderList.addItemAmount(replacement, (int) Math.min(totalItemsAlreadyThere, totalItemsNeededCount));
								totalItemsNeededCount -= totalItemsAlreadyThere;

								if (totalItemsNeededCount <= 0)
									break;
							}

							if (totalItemsNeededCount > 0)
							{
								for (ItemContainer replacement : validIngredientReplacements.get(reqCont))
								{
									RecipeRequirementList ingReqList = tree.generateRequirementList(CraftingHandler.getItemString(replacement.getItem()), null, player, bag);

									if (ingReqList == null)
										continue;

									CraftingOrderList ingOrderList = ingReqList.generateCraftingOrderList((int) Math.ceil(totalItemsNeededCount), player, bag);

									orderList.insert(ingOrderList);
									orderList.addItemAmount(replacement, (int) Math.min(totalItemsNeededCount, orderList.rootItemCrafted));

									totalItemsNeededCount -= orderList.rootItemCrafted;

									if (totalItemsNeededCount <= 0)
										break;
								}
							}
						}
						else
						{
							RecipeContainer recipe = recipes.get(reqCont);

							if (recipe == null)
								recipe = new RecipeContainerProvided(reqCont.getItem());

							int outputAmount = recipe.getOutput().getCount();

							int recipeItemsAlreadyThere = orderList.removeCreatedCount(ItemContainer.forStack(recipe.getOutput()), (int) Math.ceil(totalItemsNeededCount));
							int totalItemsAlreadyThere = InventoryHelper.removeMatchingStacksWithSizeOne(recipe.getOutput(), (int) Math.ceil(totalItemsNeededCount), bagInventory).size() + recipeItemsAlreadyThere;
							int recipeCraftingOps = (int) Math.ceil((totalItemsNeededCount - totalItemsAlreadyThere) / outputAmount);

							orderList.addItemAmount(reqCont, recipeCraftingOps * outputAmount + totalItemsAlreadyThere);

							if (recipeCraftingOps > 0 && !(recipe instanceof RecipeContainerProvided))
								orderList.addRecipe(recipe, recipeCraftingOps);

						}
					}

					List<ItemContainer> toRemove = new ArrayList<ItemContainer>();

					for (Entry<ItemContainer, Requirement> h : requirementHierarchy.entrySet())
					{
						Requirement req = h.getValue();
						req.req.remove(reqCont);
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

		ItemContainer created = ItemContainer.forStack(createdItem);
		orderList.addRecipe(this.recipes.get(created), rootCraftingOps);
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

				stack.add(entry.getKey());
				addReqRecursively(map, entry.getKey(), entry.getValue() * count, stack);
			}
		}
	}

	public Map<ItemContainer, Integer> getRecipeIngredientAmounts(CraftingOrderList orderList, CraftingStep step)
	{
		Map<ItemContainer, Integer> ingredientAmount = new HashMap<ItemContainer, Integer>();
		Map<ItemContainer, Integer> totalNeededIngs = orderList.itemAmounts;

		for (Ingredient ing : step.recipe.getIngredients())
		{
			if (ing.getItems().length == 0)
				continue;

			String ingStr = CraftingHandler.getIngredientString(ing);
			ItemContainer cont = ItemContainer.forIngredient(ingStr);

			if (totalNeededIngs.containsKey(cont))
			{
				Integer i = ingredientAmount.get(cont);
				if (i == null)
					i = 0;
				i += step.craftCount;
				ingredientAmount.put(cont, i);

				Integer j = totalNeededIngs.get(cont);
				j -= step.craftCount;
				if (j <= 0)
					totalNeededIngs.remove(cont);
				else
					totalNeededIngs.put(cont, j);
			}
			else
			{
				for (ItemContainer replacement : validIngredientReplacements.get(cont))
				{
					if (totalNeededIngs.containsKey(replacement))
					{
						Integer j = totalNeededIngs.get(replacement);
						Integer i = ingredientAmount.get(replacement);
						if (i == null)
							i = 0;

						int toAdd = Math.min(j, step.craftCount);

						i += toAdd;
						ingredientAmount.put(replacement, i);

						j -= toAdd;
						if (j <= 0)
							totalNeededIngs.remove(replacement);
						else
							totalNeededIngs.put(replacement, j);
					}
				}
			}
		}

		return ingredientAmount;
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

		public void addItemRequirement(ItemContainer stack, double count)
		{
			Double i = req.get(stack);
			if (i == null)
				i = 0.0;
			i += count;

			req.put(stack, i);
		}

		@Override
		public Requirement clone()
		{
			Requirement r = new Requirement();
			r.req = Maps.newHashMap(this.req);
			return r;
		}
	}

	public static class CraftingOrderList implements Iterable<CraftingStep>
	{
		private int rootItemCrafted = 0;
		private Map<ItemContainer, Integer> itemAmounts = new HashMap<ItemContainer, Integer>();
		private Queue<CraftingStep> recipeQueue = new LinkedList<CraftingStep>();
		private Map<ItemContainer, Integer> createdItems = new HashMap<ItemContainer, Integer>();

		public void addRecipe(RecipeContainer recipe, int craftCount)
		{
			recipeQueue.add(new CraftingStep(recipe, craftCount));
			ItemStack out = recipe.getOutput();
			addCreatedItem(ItemContainer.forStack(out), out.getCount() * craftCount);
		}

		public void addItemAmount(ItemContainer item, int amount)
		{
			Integer i = itemAmounts.get(item);
			if (i == null)
				i = 0;
			i += amount;
			itemAmounts.put(item, i);
		}

		private void addCreatedItem(ItemContainer item, int amount)
		{
			Integer i = createdItems.get(item);
			if (i == null)
				i = 0;
			i += amount;
			createdItems.put(item, i);
		}

		public boolean hasNext()
		{
			return !recipeQueue.isEmpty();
		}

		public int removeCreatedCount(ItemContainer stack, int max)
		{
			Integer count = createdItems.get(stack);
			if (count == null)
				return 0;

			int remove = Math.min(count, max);
			count -= remove;
			createdItems.put(stack, count);

			return remove;
		}

		public void insert(CraftingOrderList other)
		{
			recipeQueue.addAll(other.recipeQueue);
			for (Entry<ItemContainer, Integer> entry : other.itemAmounts.entrySet())
			{
				this.addItemAmount(entry.getKey(), entry.getValue());
			}
		}

		@Override
		public Iterator<CraftingStep> iterator()
		{
			return recipeQueue.iterator();
		}

	}

	public static class CraftingStep
	{
		private RecipeContainer recipe;
		private int craftCount;

		public CraftingStep(RecipeContainer recipe, int craftCount)
		{
			this.recipe = recipe;
			this.craftCount = craftCount;
		}

		public RecipeContainer getRecipe()
		{
			return recipe;
		}

		public int getCraftCount()
		{
			return craftCount;
		}

	}
}
