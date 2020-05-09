package tschipp.buildersbag.api;

import net.minecraftforge.items.ItemStackHandler;

public interface IBagCap
{
	
	public ItemStackHandler getBlockInventory();
	
	public IBagModule[] getModules();
	
	public void setBlockInventory(ItemStackHandler handler);
	
	public void setSelectedInventory(ItemStackHandler handler);

	public ItemStackHandler getSelectedInventory();
	
	public void setModules(IBagModule[] modules);
	
	public boolean hasModuleAndEnabled(String name);
	
	public void transferDataFromCap(IBagCap from);

	public void reInit(int tier);
}
