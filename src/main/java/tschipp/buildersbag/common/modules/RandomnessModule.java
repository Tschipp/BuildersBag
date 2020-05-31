package tschipp.buildersbag.common.modules;

import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.BagHelper;

public class RandomnessModule extends AbstractBagModule
{

	private static final ItemStack DISPLAY = new ItemStack(Blocks.COBBLESTONE);
	
	public RandomnessModule()
	{
		super("buildersbag:random");
	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag, EntityPlayer player)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		
		return list;
	}

	@Override
	public ItemStack getBlock(IBagCap bag, EntityPlayer player)
	{
		Random rand = new Random();
		NonNullList<ItemStack> list = BagHelper.getAllAvailableStacks(bag, player);
		
		NonNullList<ItemStack> blocks = NonNullList.create();
		blocks.addAll(list.stream().filter(stack -> stack.getItem() instanceof ItemBlock).collect(Collectors.toList()));
		
		if(blocks.isEmpty())
			return ItemStack.EMPTY;
		
		ItemStack stack =  blocks.get(rand.nextInt(blocks.size()));		
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
	public NonNullList<ItemStack> createStackWithCount(ItemStack stack, int count, IBagCap bag, EntityPlayer player)
	{
		return NonNullList.create();
	}

}
