package tschipp.buildersbag.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;

public interface IBagModule extends INBTSerializable<NBTTagCompound>
{

	public NonNullList<ItemStack> provideStacks();
	
	public void consume(ItemStack stack);
	
	public void toggle();
	
	public boolean isToggleable();
	
	public boolean isEnabled();
	
	public boolean isExpanded();
	
	public void setExpanded(boolean bool);
	
	public String[] getModDependencies();
	
	public ItemStackHandler getInventory();
	
}
