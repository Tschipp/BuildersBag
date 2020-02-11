package tschipp.buildersbag.api;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;

public interface IBagModule extends INBTSerializable<NBTTagCompound>
{

	public NonNullList<ItemStack> provideStacks();
	
	public void consume(ItemStack stack);
	
	public Container getContainer();
	
	public void toggle();
	
	public boolean isEnabled();
	
	public String[] getModDependencies();
	
	public IItemHandler getInventory();
}
