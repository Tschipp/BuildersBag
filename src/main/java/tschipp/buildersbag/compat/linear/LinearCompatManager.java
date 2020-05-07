package tschipp.buildersbag.compat.linear;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult.Type;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.linear.api.LinearHooks;
import tschipp.linear.common.helper.LinearHelper;

public class LinearCompatManager
{

	public static void register()
	{
		LinearHooks.registerDraggable(BuildersBagItem.class);
	}
	
	public static boolean doDragCheck(EntityPlayer player)
	{
		return LinearHooks.isBuildingEnabled(player) ? LinearHelper.getLookRay(player).typeOfHit == Type.MISS && !LinearHooks.isDragging(player) : true;
	}
	
	public static boolean isDragging(EntityPlayer player)
	{
		return LinearHooks.isDragging(player);
	}

}
