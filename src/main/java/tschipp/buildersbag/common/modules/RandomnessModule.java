package tschipp.buildersbag.common.modules;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.common.caps.IBagCap;
import tschipp.buildersbag.common.helper.InventoryHelper;

public class RandomnessModule extends AbstractBagModule
{

	private static final ItemStack DISPLAY = new ItemStack(Blocks.COBBLESTONE);
	
	public RandomnessModule()
	{
		super("buildersbag:random");
	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		
		return list;
	}

	@Override
	public ItemStack getBlock(IBagCap bag)
	{
		Random rand = new Random();
		NonNullList<ItemStack> list = InventoryHelper.getAllAvailableStacks(bag);
		
		ItemStack stack =  list.get(rand.nextInt(list.size()));		
		return stack;
	}

	@Override
	public boolean isDominating()
	{
		return true;
	}
	
	@Override
	public boolean doesntUseOwnInventory()
	{
		return true;
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
	public ItemStack getDisplayItem()
	{
		return DISPLAY;
	}

	@Override
	public ItemStack createStack(ItemStack stack, IBagCap bag, EntityPlayer player)
	{
		return ItemStack.EMPTY;
	}

}
