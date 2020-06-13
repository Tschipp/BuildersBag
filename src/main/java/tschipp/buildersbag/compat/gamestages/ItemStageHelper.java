package tschipp.buildersbag.compat.gamestages;

import net.darkhax.itemstages.ItemStages;
import net.minecraft.item.ItemStack;

public class ItemStageHelper
{

	public static String getItemStage(ItemStack stack)
	{
		String itemstage = ItemStages.getStage(stack);
		if(itemstage == null)
			return "";
		return itemstage;
	}

}
