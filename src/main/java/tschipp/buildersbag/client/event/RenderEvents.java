package tschipp.buildersbag.client.event;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.client.rendering.BagModel;
import tschipp.buildersbag.common.BuildersBagRegistry;

@EventBusSubscriber(modid = BuildersBag.MODID, value = Dist.CLIENT)
public class RenderEvents
{
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
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
