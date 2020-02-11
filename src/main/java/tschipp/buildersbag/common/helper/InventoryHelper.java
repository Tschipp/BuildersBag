package tschipp.buildersbag.common.helper;

public class InventoryHelper
{
	public static final int TOP_OFFSET = 7;
	public static final int LEFT_OFFSET = 7;
	public static final int INV_OFFSET = 14;
	public static final int HOTBAR_OFFSET = 4;

	
	public static int getBagRows(int slotAmount)
	{
		return (int) Math.ceil(slotAmount/9.0);
	}
	
	public static int getLastRowExtra(int slotAmount)
	{
		return slotAmount % 9;
	}
	
}
