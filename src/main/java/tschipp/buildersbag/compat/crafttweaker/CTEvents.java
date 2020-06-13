package tschipp.buildersbag.compat.crafttweaker;

import crafttweaker.mc1120.events.ActionApplyEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
