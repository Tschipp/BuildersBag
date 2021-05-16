//package tschipp.buildersbag.compat.linear;
//
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.util.math.RayTraceResult.Type;
//import tschipp.buildersbag.common.item.BuildersBagItem;
//
//public class LinearCompatManager TODO
//{
//
//	public static void register()
//	{
//		LinearHooks.registerDraggable(BuildersBagItem.class);
//	}
//	
//	public static boolean doDragCheck(PlayerEntity player)
//	{
//		return LinearHooks.isBuildingEnabled(player) ? LinearHelper.getLookRay(player).typeOfHit == Type.MISS && !LinearHooks.isDragging(player) : true;
//	}
//	
//	public static boolean isDragging(PlayerEntity player)
//	{
//		return LinearHooks.isDragging(player);
//	}
//
//}
