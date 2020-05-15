package tschipp.buildersbag.common.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.ModulePriority;
import tschipp.buildersbag.common.crafting.CraftingHandler;
import tschipp.buildersbag.common.crafting.RecipeContainer;
import tschipp.buildersbag.common.crafting.RecipeTreeNew;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.helper.StageHelper;

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

	@Override
	public ItemStack createStack(ItemStack stack, IBagCap bag, EntityPlayer player)
	{
		 return createStackWithRecipeTree(stack, bag, player, null);
	}

	public ItemStack createStackWithRecipeTree(ItemStack stack, IBagCap bag, EntityPlayer player, @Nullable RecipeTreeNew subTree)
	{
		if (subTree == null)
			subTree = CraftingHandler.getSubTree(InventoryHelper.getInventoryStacks(bag, player)); 
		NonNullList<ItemStack> possibleStacks = subTree.getPossibleStacks(false);

		boolean isPossible = false;
		for (ItemStack possible : possibleStacks)
			if (ItemStack.areItemsEqual(possible, stack))
				isPossible = true;

		if (!isPossible)
		{
			return ItemStack.EMPTY;
		}

		List<RecipeContainer> recipes = CraftingHandler.getRecipes(stack);
		if (recipes.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		Collections.shuffle(recipes);

		recipes:
		for (RecipeContainer container : recipes)
		{
			String stage = container.getStage();
			if (!stage.isEmpty() && !StageHelper.hasStage(player, stage))
				continue;

			boolean containsAll = true;

			top: for (Ingredient ing : container.getIngredients())
			{
				boolean hasMatchingStack = false;

				if (ing.getMatchingStacks().length == 0)
					continue;

				for (ItemStack ingStack : ing.getMatchingStacks())
				{
					if(ingStack.isEmpty())
						continue;
					
					ItemStack provided = InventoryHelper.getOrProvideStackWithTree(ingStack, bag, player, null, subTree);
					if (!provided.isEmpty())
					{
						if(ItemStack.areItemsEqual(provided, stack))
						{
							ItemStack split = provided.splitStack(1);
							InventoryHelper.addStack(provided, bag, player);

							return split;
						}			
						
						InventoryHelper.addStack(provided, bag, player);
						hasMatchingStack = true;
						continue top;
					}
				}

				if (!hasMatchingStack)
				{
					containsAll = false;
					break;
				}
			}

			if (!containsAll)
				continue;

			List<ItemStack> consumed = new ArrayList<ItemStack>();

			top: for (Ingredient ing : container.getIngredients())
			{
				if (ing.getMatchingStacks().length == 0)
					continue;

				for (ItemStack ingStack : ing.getMatchingStacks())
				{
					ItemStack provided = InventoryHelper.getOrProvideStackWithTree(ingStack, bag, player, null, subTree);
					if (!provided.isEmpty())
					{
						if(ItemStack.areItemsEqual(provided, stack))
						{
							ItemStack split = provided.splitStack(1);
							InventoryHelper.addStack(provided, bag, player);

							for (ItemStack s : consumed)
								InventoryHelper.addStack(s, bag, player);
							
							return split;
						}		
						
						consumed.add(provided.splitStack(1));
						continue top;
					}
				}

				for (ItemStack s : consumed)
					InventoryHelper.addStack(s, bag, player); // Add back the
																// stacks that
																// were used in
																// crafting.

				continue recipes;
			}

			ItemStack output = container.getOutput();
			ItemStack split = output.splitStack(1);
			InventoryHelper.addStack(output, bag, player);

			return split;
		}

		return ItemStack.EMPTY;
	}
	
	@Override
	public ModulePriority getPriority()
	{
		return ModulePriority.LOW;
	}

}
