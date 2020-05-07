package tschipp.buildersbag.client.rendering;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import tschipp.buildersbag.common.RegistryHandler;

public class ItemRendering
{
	public static void regItemRenders()
	{
		register(RegistryHandler.tier1);
		register(RegistryHandler.tier2);
		register(RegistryHandler.tier3);
		register(RegistryHandler.tier4);
		register(RegistryHandler.tier5);
	}
	
	
	public static void register(Item item)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
}
