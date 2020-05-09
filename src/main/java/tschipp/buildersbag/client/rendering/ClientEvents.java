package tschipp.buildersbag.client.rendering;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.BuildersBagRegistry;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class ClientEvents
{
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onModelBake(ModelBakeEvent event)
	{
		for (Item item : BuildersBagRegistry.items)
		{
			IBakedModel model = event.getModelRegistry().getObject(new ModelResourceLocation(item.getRegistryName(), "inventory"));
			if (model != null)
			{
				BagModel newModel = new BagModel(model);
				event.getModelRegistry().putObject(new ModelResourceLocation(item.getRegistryName(), "inventory"), newModel);
			}
		}
	}

}
