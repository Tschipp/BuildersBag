package tschipp.buildersbag.common.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.common.caps.IBagCap;
import tschipp.buildersbag.common.crafting.CraftingHandler;
import tschipp.buildersbag.common.crafting.RecipeContainer;
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
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag)
	{
		return NonNullList.create();
	}
	
	@Override
	public String[] getModDependencies()
	{
		return new String[0];
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
		List<RecipeContainer> recipes = CraftingHandler.getRecipes(stack);
		if(recipes.isEmpty())
			return ItemStack.EMPTY;

		
		NonNullList<ItemStack> availableStacks = InventoryHelper.getAllAvailableStacksExcept(bag, this);
		
		for(RecipeContainer container : recipes)
		{
			String stage = container.getStage();
			if(!stage.isEmpty() && !StageHelper.hasStage(player, stage))
				continue;
			
			boolean containsAll = true;
						
			for(Ingredient ing : container.getIngredients())
			{
				boolean hasMatchingStack = false;
				
				if(ing.getMatchingStacks().length == 0)
					continue;
				
				for(ItemStack ingStack : ing.getMatchingStacks())
				{
					ItemStack provided = InventoryHelper.getOrProvideStack(ingStack, bag, player, null);
					if(!provided.isEmpty())
					{
						InventoryHelper.addStack(provided, bag, player);
						hasMatchingStack = true;
						continue;
					}
				}
				
				if(!hasMatchingStack)
				{
					containsAll = false;
					break;
				}
			}
			
			if(!containsAll)
				continue;
			
			List<ItemStack> consumed = new ArrayList<ItemStack>();
			
			top:
			for(Ingredient ing : container.getIngredients())
			{
				if(ing.getMatchingStacks().length == 0)
					continue;
				
				for(ItemStack ingStack : ing.getMatchingStacks())
				{
					ItemStack provided = InventoryHelper.getOrProvideStack(ingStack, bag, player, null);
					if(!provided.isEmpty())
					{
						consumed.add(provided.splitStack(1));
						continue top;
					}
				}
				
				for(ItemStack s : consumed)
					InventoryHelper.addStack(s, bag, player);
				
				return ItemStack.EMPTY;
			}
			
			ItemStack output = container.getOutput();
			ItemStack split = output.splitStack(1);
			InventoryHelper.addStack(output, bag, player);
			
			return split;
		}
		
		
		return ItemStack.EMPTY;
	}

	
}
