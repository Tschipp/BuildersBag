package tschipp.buildersbag.client.rendering;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import tschipp.buildersbag.common.BuildersBagRegistry;

public class ItemRendering
{
	public static void regItemRenders()
	{
		register(BuildersBagRegistry.tier1);
		register(BuildersBagRegistry.tier2);
		register(BuildersBagRegistry.tier3);
		register(BuildersBagRegistry.tier4);
		register(BuildersBagRegistry.tier5);
	}
	
	
	public static void register(Item item)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
}
