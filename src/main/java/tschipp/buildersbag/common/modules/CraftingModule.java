package tschipp.buildersbag.common.modules;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.ModulePriority;
import tschipp.buildersbag.common.crafting.CraftingHandler;
import tschipp.buildersbag.common.crafting.RecipeContainer;
import tschipp.buildersbag.common.crafting.RecipeRequirementList;
import tschipp.buildersbag.common.crafting.RecipeRequirementList.CraftingOrderList;
import tschipp.buildersbag.common.crafting.RecipeRequirementList.CraftingStep;
import tschipp.buildersbag.common.crafting.RecipeTree;
import tschipp.buildersbag.common.data.ItemContainer;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.helper.ItemHelper;

public class CraftingModule extends AbstractBagModule
{

	private static final ItemStack DISPLAY = new ItemStack(Blocks.CRAFTING_TABLE);

	public CraftingModule()
	{
		super("buildersbag:crafting");
	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag, EntityPlayer player)
	{
		return CraftingHandler.getPossibleBlocks(InventoryHelper.getInventoryStacks(bag, player), true);
	}

	@Override
	public ItemStackHandler getInventory()
	{
		return null;
	}

	@Override
	public boolean doesntUseOwnInventory()
	{
		return true;
	}

	@Override
	public ItemStack getDisplayItem()
	{
		return DISPLAY;
	}

	public NonNullList<ItemStack> createStackWithRecipeTree(ItemStack stack, int count, IBagCap bag, EntityPlayer player, @Nullable RecipeTree subTree, @Nonnull ItemStack root)
	{
		System.out.println("Crafting: Trying to create " + count + " of " + stack);

		boolean newTree = subTree == null;

		if (subTree == null)
			subTree = CraftingHandler.getSubTree(InventoryHelper.getInventoryStacks(bag, player));

		if (root.isEmpty())
			root = stack;

		if (newTree)
			BagHelper.updateTreeCache(subTree, root);

		NonNullList<ItemStack> possibleStacks = subTree.getPossibleStacks(false);

		boolean isPossible = false;
		for (ItemStack possible : possibleStacks)
			if (ItemStack.areItemsEqual(possible, stack))
				isPossible = true;

		if (!isPossible)
		{
			return NonNullList.create();
		}

		NonNullList<ItemStack> list = NonNullList.create();

		RecipeRequirementList recipeList = subTree.generateRequirementList(CraftingHandler.getItemString(stack), null, player, bag);

		int attempt = 0;

		while (attempt < 10 && recipeList != null && list.size() < count)
		{
			attempt++;

			CraftingOrderList craftingList = recipeList.generateCraftingOrderList(count, player, bag);

			NonNullList<ItemStack> providedRecipeIngredients = NonNullList.create();

			for (CraftingStep step : craftingList)
			{
				RecipeContainer recipe = step.getRecipe();
				int totalCraftAmount = step.getCraftCount();

				Map<ItemContainer, Integer> ingredientAmount = recipeList.getRecipeIngredientAmounts(craftingList, step);

				for (Entry<ItemContainer, Integer> ingEntry : ingredientAmount.entrySet())
				{
					int amountNeeded = ingEntry.getValue();

					ItemStack matchingStack = ingEntry.getKey().getItem();
					if (!matchingStack.isEmpty())
					{
						if (amountNeeded <= 0)
							continue;

						int x = amountNeeded;

						NonNullList<ItemStack> providedCopy = ItemHelper.copy(providedRecipeIngredients);

						for (int i = 0; i < x; i++)
						{
							ItemStack providedStack = ItemHelper.containsStack(matchingStack, providedCopy);
							if (!providedStack.isEmpty())
							{
								providedCopy.remove(providedStack);
								amountNeeded--;
							}
							else
								continue;
						}

						if (amountNeeded <= 0)
							continue;

						NonNullList<ItemStack> provided = BagHelper.getOrProvideStackWithTree(matchingStack, amountNeeded, bag, player, this, subTree, root);
						amountNeeded -= provided.size();
						providedRecipeIngredients.addAll(provided);
					}

				}

				for (int i = 0; i < totalCraftAmount; i++)
				{
					boolean hasRecipeRequirements = true;
					NonNullList<ItemStack> removedIngredients = NonNullList.create();

					for (Entry<ItemContainer, Integer> ingEntry : ingredientAmount.entrySet())
					{
						int amountNeeded = ingEntry.getValue() / totalCraftAmount;
						boolean hasIng = false;

						ItemStack matchingStack = ingEntry.getKey().getItem();
						if (!matchingStack.isEmpty())
						{
							if(amountNeeded <= 0)
								hasIng = true;
								
							int x = amountNeeded;
							for (int j = 0; j < x; j++)
							{
								ItemStack provided = ItemHelper.containsStack(matchingStack, providedRecipeIngredients);
								if (!provided.isEmpty())
								{
									amountNeeded--;
									providedRecipeIngredients.remove(provided);
									removedIngredients.add(provided);

									if (amountNeeded <= 0)
									{
										hasIng = true;
										break;
									}
								}
							}
						}

						if (!hasIng)
							hasRecipeRequirements = false;
					}

					if (hasRecipeRequirements)
					{
						ItemStack out = recipe.getOutput();
						int outcount = out.getCount();
						out.setCount(1);

						if (ItemStack.areItemsEqual(recipe.getOutput(), root))
							list.addAll(ItemHelper.listOf(out, outcount));
						else
							providedRecipeIngredients.addAll(ItemHelper.listOf(out, outcount));
					}
					else
					{
						for (ItemStack s : removedIngredients)
							BagHelper.addStack(s, bag, player);
						recipeList.blacklist(recipe);
					}

				}

			}

			if (!providedRecipeIngredients.isEmpty())
			{
				for (ItemStack s : providedRecipeIngredients)
					BagHelper.addStack(s, bag, player);
			}

			recipeList = subTree.generateRequirementList(CraftingHandler.getItemString(stack), null, player, bag);

		}

		// List<RecipeContainer> recipes = CraftingHandler.getRecipes(stack);
		// if (recipes.isEmpty())
		// {
		// return ItemStack.EMPTY;
		// }
		//
		//// Collections.shuffle(recipes);
		//
		// recipes:
		// for (RecipeContainer container : recipes)
		// {
		// String stage = container.getStage();
		// if (!stage.isEmpty() && !StageHelper.hasStage(player, stage))
		// continue;
		//
		// boolean containsAll = true;
		//
		// top: for (Ingredient ing : container.getIngredients())
		// {
		// boolean hasMatchingStack = false;
		//
		// if (ing.getMatchingStacks().length == 0)
		// continue;
		//
		// for (ItemStack ingStack : ing.getMatchingStacks())
		// {
		// if(ingStack.isEmpty())
		// continue;
		//
		// ItemStack provided = BagHelper.getOrProvideStackWithTree(ingStack, count, bag, player, null, subTree, root);
		// if (!provided.isEmpty())
		// {
		//// if(ItemStack.areItemsEqual(provided, stack) || ItemStack.areItemsEqual(provided, root))
		//// {
		//// ItemStack split = provided.splitStack(1);
		//// InventoryHelper.addStack(provided, bag, player);
		////
		//// return split;
		//// }
		//
		// BagHelper.addStack(provided, bag, player);
		// hasMatchingStack = true;
		// continue top;
		// }
		// }
		//
		// if (!hasMatchingStack)
		// {
		// containsAll = false;
		// break;
		// }
		// }
		//
		// if (!containsAll)
		// continue;
		//
		// List<ItemStack> consumed = new ArrayList<ItemStack>();
		//
		// top: for (Ingredient ing : container.getIngredients())
		// {
		// if (ing.getMatchingStacks().length == 0)
		// continue;
		//
		// for (ItemStack ingStack : ing.getMatchingStacks())
		// {
		// ItemStack provided = BagHelper.getOrProvideStackWithTree(ingStack, count, bag, player, null, subTree, root);
		// if (!provided.isEmpty())
		// {
		// if(ItemStack.areItemsEqual(provided, stack) || ItemStack.areItemsEqual(provided, root))
		// {
		// ItemStack split = provided.splitStack(1);
		//// System.out.println("Adding " + provided);
		// BagHelper.addStack(provided, bag, player);
		//
		// for (ItemStack s : consumed)
		// {
		//// System.out.println("Adding " + s);
		// BagHelper.addStack(s, bag, player);
		// }
		//
		// return split;
		// }
		//
		// consumed.add(provided.splitStack(1));
		// continue top;
		// }
		// }
		//
		// for (ItemStack s : consumed)
		// BagHelper.addStack(s, bag, player); // Add back the stacks that were used in crafting.
		//
		// continue recipes;
		// }
		//
		// ItemStack output = container.getOutput();
		// ItemStack split = output.splitStack(1);
		// BagHelper.addStack(output, bag, player);
		//// System.out.println("Adding " + output);
		//
		// System.out.println(System.currentTimeMillis() - time + "ms needed to craft " + split);
		//
		// return split;
		// }

		while (list.size() > count)

		{
			BagHelper.addStack(list.remove(list.size() - 1), bag, player);
		}

		return list;
	}

	@Override
	public ModulePriority getPriority()
	{
		return ModulePriority.LOW;
	}

	@Override
	public NonNullList<ItemStack> createStackWithCount(ItemStack stack, int count, IBagCap bag, EntityPlayer player)
	{
		return createStackWithRecipeTree(stack, count, bag, player, null, ItemStack.EMPTY);
	}

}
