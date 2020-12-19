package tschipp.buildersbag.common.modules;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;

public class SupplierModule extends AbstractBagModule
{

	private static final ItemStack DISPLAY = new ItemStack(Blocks.CHEST);

	public SupplierModule()
	{
		super("buildersbag:supplier");
	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag, PlayerEntity player)
	{
		return NonNullList.create();
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
	public boolean isSupplier()
	{
		return true;
	}

	@Override
	public NonNullList<ItemStack> createStackWithCount(ItemStack stack, int count, IBagCap bag, PlayerEntity player)
	{
		return BagHelper.getOrProvideStackWithCount(stack, count, bag, player, this);
	}
}
