package tschipp.buildersbag.common.events;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.compat.buildinggadgets.BagProviderCapProvider;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class CapabilityEvents
{

	@SubscribeEvent
	public static void onAttachCaps(AttachCapabilitiesEvent<ItemStack> event)
	{
		ItemStack stack = event.getObject();

		if (Loader.isModLoaded("buildinggadgets"))
		{
			if (stack.getItem() instanceof BuildersBagItem)
			{
				event.addCapability(new ResourceLocation(BuildersBag.MODID, "fake_bag_provider"), new BagProviderCapProvider(stack));
			}
		}

//		if (stack.getItem() instanceof BuildersBagItem)
//			event.addCapability(new ResourceLocation(BuildersBag.MODID, "builders_bag"), new BagCapProvider(((BuildersBagItem)stack.getItem()).getTier()));

	}

}
