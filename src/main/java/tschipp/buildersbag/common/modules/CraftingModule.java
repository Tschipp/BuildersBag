package tschipp.buildersbag.common.modules;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.common.caps.IBagCap;

public class CraftingModule extends AbstractBagModule
{

	private static final ItemStack DISPLAY = new ItemStack(Blocks.CRAFTING_TABLE);

	protected CraftingModule()
	{
		super("buildersbag:crafting");
	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag)
	{
		return null;
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
		return ItemStack.EMPTY;
	}

	
}
