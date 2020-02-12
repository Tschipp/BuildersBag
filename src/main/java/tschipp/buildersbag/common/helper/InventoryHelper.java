package tschipp.buildersbag.common.helper;

import static tschipp.buildersbag.common.helper.InventoryHelper.BOTTOM_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.HOTBAR_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.INV_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.TOP_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.getBagRows;

public class InventoryHelper
{
	public static final int TOP_OFFSET = 15;
	public static final int LEFT_OFFSET = 7;
	public static final int INV_OFFSET = 14;
	public static final int HOTBAR_OFFSET = 4;
	public static final int BOTTOM_OFFSET = 7;
	public static final int RIGHT_OFFSET = 7;

	
	public static int getSlotWidth(int slotAmount)
	{
		return slotAmount * 18 + LEFT_OFFSET + RIGHT_OFFSET;
	}
	
	public static int getBagRows(int slotAmount)
	{
		return (int) Math.ceil(slotAmount/9.0);
	}
	
	public static int getLastRowExtra(int slotAmount)
	{
		return slotAmount % 9;
	}
	
	public static int getMaxModules(int slotAmount)
	{
		return getTotalHeight(slotAmount) / 33;
	}
	
	public static int getTotalHeight(int slotAmount)
	{
		return TOP_OFFSET + (getBagRows(slotAmount) + 4) * 18 + HOTBAR_OFFSET + INV_OFFSET + BOTTOM_OFFSET; 
	}
	
	public static int getTotalWidth()
	{
		return 176;
	}
	
}
