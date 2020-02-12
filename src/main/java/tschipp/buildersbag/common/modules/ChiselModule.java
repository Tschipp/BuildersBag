package tschipp.buildersbag.common.modules;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import team.chisel.api.IChiselItem;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.common.inventory.ItemHandlerWithPredicate;

public class ChiselModule extends AbstractBagModule
{

	private ItemHandlerWithPredicate handler = new ItemHandlerWithPredicate(1, (stack, slot) -> stack.getItem() instanceof IChiselItem);
	
	public ChiselModule()
	{
		super("buildersbag:chisel");
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
		return null;
	}

	@Override
	public ItemStackHandler getInventory()
	{
		return handler;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound tag = super.serializeNBT();
		tag.setTag("Inventory", handler.serializeNBT());
		return tag;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		super.deserializeNBT(nbt);
		handler.deserializeNBT(nbt.getCompoundTag("Inventory"));
	}

	@Override
	public boolean isToggleable()
	{
		return false;
	}
}
