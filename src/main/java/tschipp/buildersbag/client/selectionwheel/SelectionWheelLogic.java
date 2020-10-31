package tschipp.buildersbag.client.selectionwheel;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

public class SelectionWheelLogic
{

	static class SelectionPage
	{
		List<ItemStack> items = new ArrayList<ItemStack>(9);
		int index;
	
		@Override
		public String toString()
		{
			return items.toString();
		}
	}

}
