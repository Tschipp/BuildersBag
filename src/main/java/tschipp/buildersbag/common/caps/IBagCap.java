package tschipp.buildersbag.common.caps;

import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.IBagModule;

public interface IBagCap
{
	
	public ItemStackHandler getBlockInventory();
	
	public IBagModule[] getModules();
	
	public void setBlockInventory(ItemStackHandler handler);
	
	public void setSelectedInventory(ItemStackHandler handler);

	public ItemStackHandler getSelectedInventory();
	
	public void setModules(IBagModule[] modules);
	
}
