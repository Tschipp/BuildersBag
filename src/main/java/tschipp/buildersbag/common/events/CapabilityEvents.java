package tschipp.buildersbag.common.events;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tschipp.buildersbag.BuildersBag;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class CapabilityEvents
{

	@SubscribeEvent
	public static void onAttachCaps(AttachCapabilitiesEvent<ItemStack> event)
	{
		ItemStack stack = event.getObject();

//		if (ModList.get().isLoaded("buildinggadgets"))
//		{
//			if (stack.getItem() instanceof BuildersBagItem)
//			{
//				event.addCapability(new ResourceLocation(BuildersBag.MODID, "fake_bag_provider"), new BagProviderCapProvider(stack));
//			}
//		}

//		if (stack.getItem() instanceof BuildersBagItem)
//			event.addCapability(new ResourceLocation(BuildersBag.MODID, "builders_bag"), new BagCapProvider(((BuildersBagItem)stack.getItem()).getTier()));

	}

}
