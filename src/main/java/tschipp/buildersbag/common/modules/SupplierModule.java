package tschipp.buildersbag.common.modules;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.common.caps.IBagCap;
import tschipp.buildersbag.common.helper.InventoryHelper;

public class SupplierModule extends AbstractBagModule
{

	private static final ItemStack DISPLAY = new ItemStack(Blocks.CHEST);

	public SupplierModule()
	{
		super("buildersbag:supplier");
	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag)
	{
		return NonNullList.create();
	}

	@Override
	public ItemStack createStack(ItemStack stack, IBagCap bag, EntityPlayer player)
	{
		return InventoryHelper.getOrProvideStack(stack, bag, player, this);
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
	public boolean isSupplier()
	{
		return true;
	}
}
