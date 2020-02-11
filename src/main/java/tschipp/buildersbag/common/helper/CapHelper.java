package tschipp.buildersbag.common.helper;

import net.minecraft.item.ItemStack;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.caps.IBagCap;

public class CapHelper
{
	public static IBagCap getBagCap(ItemStack stack)
	{
		if(stack.hasCapability(BagCapProvider.BAG_CAPABILITY, null))
		{
			return stack.getCapability(BagCapProvider.BAG_CAPABILITY, null);
		}
		return null;
	}
}
