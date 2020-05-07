package tschipp.buildersbag.common.helper;

import net.minecraft.item.ItemStack;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.caps.BagCapProvider;

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
