package tschipp.buildersbag.common.modules;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
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
	public Container getContainer()
	{
		return null;
	}

	@Override
	public String[] getModDependencies()
	{
		return new String[0];
	}

	@Override
	public IItemHandler getInventory()
	{
		return null;
	}
}
