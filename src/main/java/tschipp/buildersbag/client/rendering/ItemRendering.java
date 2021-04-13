package tschipp.buildersbag.client.rendering;

import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import tschipp.buildersbag.common.BuildersBagRegistry;

public class ItemRendering
{
	public static void regItemRenders()
	{
		register(BuildersBagRegistry.TIER_1);
		register(BuildersBagRegistry.TIER_2);
		register(BuildersBagRegistry.TIER_3);
		register(BuildersBagRegistry.TIER_4);
		register(BuildersBagRegistry.TIER_5);
		
		ResourceLocation loc = new ModelResourceLocation("buildersbag:gears", "inventory");
		ModelLoader.registerItemVariants(Items.AIR, loc);
//		Minecraft.getInstance().getRenderItem().getItemModelMesher().register(item, definition);
	}
	
	
	public static void register(Item item)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
}
