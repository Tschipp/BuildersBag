package tschipp.buildersbag.common.modules;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.AbstractBagModule;

public class CraftingModule extends AbstractBagModule
{

	protected CraftingModule()
	{
		super("buildersbag:crafting");
	}

	@Override
	public NonNullList<ItemStack> provideStacks()
	{
		return null;
	}

	@Override
	public void consume(ItemStack stack)
	{
		
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
	public boolean isToggleable()
	{
		return true;
	}
}
