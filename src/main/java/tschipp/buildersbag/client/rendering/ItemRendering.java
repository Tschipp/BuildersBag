package tschipp.buildersbag.client.rendering;

import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class ItemRendering
{
	public static void regItemRenders()
	{
//		register(BuildersBagRegistry.TIER_1);
//		register(BuildersBagRegistry.TIER_2);
//		register(BuildersBagRegistry.TIER_3);
//		register(BuildersBagRegistry.TIER_4);
//		register(BuildersBagRegistry.TIER_5);
//		
		ResourceLocation loc = new ModelResourceLocation("buildersbag:gears", "inventory");
		ModelLoader.addSpecialModel(loc);
	}
	
	
//	public static void register(Item item) No longer needed, remove
//	{
//		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
//	}
}
