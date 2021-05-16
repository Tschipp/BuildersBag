package tschipp.buildersbag.client.event;

import java.util.Map;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
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
		Map<ResourceLocation, IBakedModel> reg = event.getModelRegistry();
		updateModel(BuildersBagRegistry.TIER_1, reg);
		updateModel(BuildersBagRegistry.TIER_2, reg);
		updateModel(BuildersBagRegistry.TIER_3, reg);
		updateModel(BuildersBagRegistry.TIER_4, reg);
		updateModel(BuildersBagRegistry.TIER_5, reg);

	}

	private static void updateModel(Item item, Map<ResourceLocation, IBakedModel> reg)
	{
		IBakedModel model = reg.get(new ModelResourceLocation(item.getRegistryName(), "inventory"));
		if (model != null)
		{
			BagModel newModel = new BagModel(model);
			reg.put(new ModelResourceLocation(item.getRegistryName(), "inventory"), newModel);
		}
	}

}
