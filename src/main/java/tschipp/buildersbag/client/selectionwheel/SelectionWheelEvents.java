package tschipp.buildersbag.client.selectionwheel;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.client.BuildersBagKeybinds;
import tschipp.buildersbag.common.item.BuildersBagItem;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class SelectionWheelEvents
{
	@SubscribeEvent
	public static void onRenderScreen(RenderGameOverlayEvent.Post event)
	{
		Minecraft mc = Minecraft.getMinecraft();

		if (!SelectionWheel.open && BuildersBagKeybinds.isMenuKeyPressed())
		{
			ItemStack main = mc.player.getHeldItemMainhand();
			ItemStack off = mc.player.getHeldItemMainhand();
			
			if (main.getItem() instanceof BuildersBagItem)
				SelectionWheel.setBag(main);
			else if(off.getItem() instanceof BuildersBagItem)
				SelectionWheel.setBag(off);
			
			SelectionWheel.startAnimation();
			mc.setIngameNotInFocus();
		}
		else if (SelectionWheel.open && !BuildersBagKeybinds.isMenuKeyPressed())
		{
			SelectionWheel.finishAnimation();
			mc.setIngameFocus();
		}

		if (SelectionWheel.shouldDraw)
		{
			SelectionWheel.draw(event.getPartialTicks(), event.getResolution());
		}
	}

	@SubscribeEvent
	public static void onMouseInput(MouseEvent event)
	{
		if (SelectionWheel.open)
		{
			if (event.getDwheel() > 0 || event.getDwheel() < 0)
			{
				SelectionWheel.onScroll(event.getDwheel());
				event.setCanceled(true);
			}

			if (event.isButtonstate() && (event.getButton() == 0 || event.getButton() == 1))
			{
				SelectionWheel.onClick(event.getButton() == 0 ? false : true);
				event.setCanceled(true);
			}
		}
	}

}
