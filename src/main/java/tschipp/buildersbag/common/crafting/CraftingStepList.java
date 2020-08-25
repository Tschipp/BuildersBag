package tschipp.buildersbag.common.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.crafting.CraftingStepList.CraftingStep;
import tschipp.buildersbag.common.data.ItemContainer;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.helper.MapHelper;

public class CraftingStepList implements Iterable<CraftingStep>
{
	public Map<ItemContainer, Integer> excessItemsCreated = new HashMap<ItemContainer, Integer>();
	private Stack<CraftingStep> craftingstack = new Stack<CraftingStep>();

	private RecipeTree tree;
	private EntityPlayer player;
	private IBagCap bag;
	NonNullList<ItemStack> bagInventory = NonNullList.create();
	private RecipeContainer creationRecipe = null;

	public CraftingStepList(EntityPlayer player, IBagCap bag, RecipeTree tree)
	{
		this.player = player;
		this.bag = bag;
		this.tree = tree;

		InventoryHelper.getStacks(bag.getBlockInventory()).forEach(stack -> bagInventory.add(stack.copy()));
	}

	public void blacklist()
	{
		tree.blacklistedRecipes.add(creationRecipe);

//		if (!craftingstack.isEmpty())
//			tree.blacklistedRecipes.add(craftingstack.get(0).recipe);
	}

	public void setCreationRecipe(RecipeContainer cont)
	{
		if (creationRecipe == null)
			creationRecipe = cont;
	}

	public RecipeContainer getCreationRecipe()
	{
		return creationRecipe;
	}

	public CraftingStep addCraftingStep(RecipeContainer recipe)
	{
		CraftingStep step = new CraftingStep(recipe);
		craftingstack.add(step);
		return step;
	}

	public CraftingStep addCraftingStepAt(RecipeContainer recipe, int index)
	{
		CraftingStep step = new CraftingStep(recipe);
		craftingstack.add(index, step);
		return step;
	}

	public void moveCraftingStep(CraftingStep step, int index)
	{
		removeCraftingStep(step);
		craftingstack.add(index, step);
	}

	public void removeCraftingStep(CraftingStep step)
	{
		craftingstack.remove(step);
	}

	public CraftingStepList copy()
	{
		CraftingStepList list = new CraftingStepList(player, bag, tree);
		MapHelper.merge(this.excessItemsCreated, list.excessItemsCreated);
		list.bagInventory.clear();
		this.bagInventory.forEach(stack -> list.bagInventory.add(stack.copy()));
		return list;
	}

	public void merge(CraftingStepList other)
	{
		this.excessItemsCreated = other.excessItemsCreated;
		this.craftingstack.addAll(0, other.craftingstack);
		this.bagInventory = other.bagInventory;
	}

	public boolean doesCreateLast(ItemContainer cont)
	{
		if (this.craftingstack.isEmpty())
			return false;

		return ItemContainer.forStack(this.craftingstack.get(0).recipe.getOutput()).equals(cont);
	}

	public int getAlreadyCreated(ItemContainer cont, int maxNeeded, @Nullable CraftingStep addToStep)
	{
		int alreadyCreated = 0;
		if (cont.isIngredient())
		{
			for (ItemContainer item : cont.getItems())
			{
				alreadyCreated += getAlreadyCreated(item, maxNeeded - alreadyCreated, addToStep);
			}
		}
		else
		{
			ItemStack s = cont.getItem();
			alreadyCreated += InventoryHelper.removeMatchingStacksWithSizeOne(s, maxNeeded, bagInventory).size();
			alreadyCreated += MapHelper.removeAtMost(excessItemsCreated, cont, maxNeeded - alreadyCreated).intValue();

			if (addToStep != null)
				addToStep.addIngredient(cont, alreadyCreated);
		}

		return alreadyCreated;
	}

	public void addExcess(ItemContainer cont, int excess)
	{
		MapHelper.add(excessItemsCreated, cont, excess);
	}

	public boolean findCycle()
	{
		Map<RecipeContainer, Integer> visitationCount = new HashMap<RecipeContainer, Integer>();
		for (CraftingStep step : this)
		{
			if (step.recipeAmount == 0)
				MapHelper.add(visitationCount, step.recipe, 1);
			if(visitationCount.containsKey(step.getRecipe()) && visitationCount.get(step.recipe) >= 2)
			{
				tree.blacklistedRecipes.add(step.recipe);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public Iterator<CraftingStep> iterator()
	{
		List<CraftingStep> list = new ArrayList<CraftingStep>();
		for (int i = craftingstack.size() - 1; i >= 0; i--)
			list.add(craftingstack.get(i));
		return list.iterator();
	}

	public int getInsertionIndex(Map<ItemContainer, Integer> ingredientsUsed, CraftingStep current)
	{
		int limit = craftingstack.indexOf(current);
		int min = limit;
		for (int i = 0; i < min; i++)
		{
			CraftingStep step = craftingstack.get(i);
			ItemContainer cont = ItemContainer.forStack(step.recipe.getOutput());
			if (ingredientsUsed.containsKey(cont) && i < min)
			{
				min = i;
			}
		}

		return min;
	}

	public static class CraftingStep
	{
		private RecipeContainer recipe;
		private int recipeAmount;
		public Map<ItemContainer, Integer> ingredientsToUse = new HashMap<ItemContainer, Integer>();

		public CraftingStep(RecipeContainer recipe)
		{
			this.recipe = recipe;
		}

		public void setRecipeAmount(int amount)
		{
			this.recipeAmount = amount;
		}

		public RecipeContainer getRecipe()
		{
			return recipe;
		}

		public int getRecipeAmount()
		{
			return recipeAmount;
		}

		public void addIngredient(ItemContainer cont, int count)
		{
			MapHelper.add(ingredientsToUse, cont, count);
		}

		public void mergeIngredients(Map<ItemContainer, Integer> map)
		{
			MapHelper.merge(map, ingredientsToUse);
		}

		@Override
		public String toString()
		{
			return "Crafting " + recipeAmount + " * " + recipe;
		}
	}
}
