package tschipp.buildersbag.common.modules;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.api.BagModuleType;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.datastructures.CreateableItemsManager;
import tschipp.buildersbag.common.BuildersBagRegistry;

public class RandomnessModule extends AbstractBagModule
{

	private static final ItemStack DISPLAY = new ItemStack(Blocks.COBBLESTONE);

	@Override
	public ItemStack getBlock(IBagCap bag, PlayerEntity player)
	{
		Random rand = new Random();
		List<Item> items = bag.getComplex().getAllAvailableItems().stream().filter(item -> item instanceof BlockItem).collect(Collectors.toList());
		
		if(items.isEmpty())
			return ItemStack.EMPTY;
		
		ItemStack stack =  new ItemStack(items.get(rand.nextInt(items.size())));		
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
	public NonNullList<ItemStack> createStackWithCount(ItemStack stack, int count, IBagCap bag, PlayerEntity player)
	{
		return NonNullList.create();
	}

	@Override
	public CreateableItemsManager getCreateableItemsManager()
	{
		return null;
	}

	@Override
	public BagModuleType<? extends IBagModule> getType()
	{
		return BuildersBagRegistry.MODULE_RANDOM;
	}

}
