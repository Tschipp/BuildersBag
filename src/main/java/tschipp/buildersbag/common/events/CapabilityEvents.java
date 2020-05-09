package tschipp.buildersbag.common.events;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.item.BuildersBagItem;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class CapabilityEvents
{

	@SubscribeEvent
	public static void onAttachCaps(AttachCapabilitiesEvent<ItemStack> event)
	{
//		ItemStack stack = event.getObject();
//		if(stack.getItem() instanceof BuildersBagItem)
//		{
//			event.addCapability(new ResourceLocation(BuildersBag.MODID, "bag"), new BagCapProvider(((BuildersBagItem)stack.getItem()).getTier()));
//		
//			NBTTagCompound tag = stack.writeToNBT(new NBTTagCompound());
//			if(tag.hasKey("ForgeCaps"))
//			{
//				NBTTagCompound forgeCaps = tag.getCompoundTag("ForgeCaps");
//			}
//		}
	}

}
