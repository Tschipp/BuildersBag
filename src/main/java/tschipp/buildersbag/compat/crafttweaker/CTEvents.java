package tschipp.buildersbag.compat.crafttweaker;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.crafting.CraftingHandler;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class CTEvents
{
	
	@Method(modid = "crafttweaker")
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onActionApply(ActionApplyEvent.Post event)
	{
		CraftingHandler.generateRecipes();
	}

}
