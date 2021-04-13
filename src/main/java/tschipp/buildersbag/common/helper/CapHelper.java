package tschipp.buildersbag.common.helper;

import net.minecraft.item.ItemStack;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.caps.BagCapProvider;

public class CapHelper
{
	public static IBagCap getBagCap(ItemStack stack)
	{
		return stack.getCapability(BagCapProvider.BAG_CAPABILITY).orElse(null);
	}
	
	public static boolean areCapsEqual(IBagCap one, IBagCap other)
	{
		return one.getUUID().equals(other.getUUID());
	}
}
