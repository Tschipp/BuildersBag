package tschipp.buildersbag.api;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public interface IBagCap
{
	
	public ItemStackHandler getBlockInventory();
	
	public IBagModule[] getModules();
	
	public void setBlockInventory(ItemStackHandler handler);
	
	public void setSelectedInventory(ItemStackHandler handler);

	public ItemStackHandler getSelectedInventory();
	
	public void setModules(IBagModule[] modules);
	
	public List<ItemStack> getPalette();
	
	public void setPalette(List<ItemStack> list);
	
	public boolean hasModuleAndEnabled(String name);
	
	public void transferDataFromCap(IBagCap from);

	public void reInit(int tier);
	
	public String getUUID();
	
	public void setUUID(String uuid);
	
	public IBagCap copy();
}
