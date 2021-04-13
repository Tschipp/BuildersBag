package tschipp.buildersbag.client.selectionwheel;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.client.BuildersBagKeybinds;
import tschipp.buildersbag.common.config.BuildersBagConfig;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.network.server.ModifyPaletteServer;
import tschipp.buildersbag.network.server.SetSelectedBlockServer;

@EventBusSubscriber(modid = BuildersBag.MODID, value = Dist.CLIENT)
public class SelectionWheelEvents
{
	@SubscribeEvent
	public static void onRenderScreen(RenderGameOverlayEvent.Post event)
	{
		Minecraft mc = Minecraft.getInstance();

		if (!SelectionWheel.open && BuildersBagKeybinds.isMenuKeyPressed())
		{
			ItemStack main = mc.player.getHeldItemMainhand();
			ItemStack off = mc.player.getHeldItemMainhand();

			if (main.getItem() instanceof BuildersBagItem)
				SelectionWheel.setBag(main);
			else if (off.getItem() instanceof BuildersBagItem)
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
			}

			if (event.isButtonstate() && (event.getButton() == 0 || event.getButton() == 1))
			{
				SelectionWheel.onClick(event.getButton() == 0 ? false : true);
			}

			event.setCanceled(true);
		}

		if (event.getDwheel() > 0 || event.getDwheel() < 0 && Minecraft.getInstance().player != null)
		{
			PlayerEntity player = Minecraft.getInstance().player;

			if (player.isSneaking())
			{
				ItemStack main = player.getHeldItemMainhand();
				ItemStack off = player.getHeldItemMainhand();

				ItemStack bag = ItemStack.EMPTY;

				if (main.getItem() instanceof BuildersBagItem)
					bag = main;
				else if (off.getItem() instanceof BuildersBagItem)
					bag = off;

				if (!bag.isEmpty())
				{
					IBagCap cap = CapHelper.getBagCap(bag);
					int index = -1;

					if (cap.getPalette().size() > 1)
					{
						for (int i = 0; i < cap.getPalette().size(); i++)
						{
							if (ItemStack.areItemStacksEqual(cap.getPalette().get(i), cap.getSelectedInventory().getStackInSlot(0)))
							{
								index = i;
								break;
							}
						}
						boolean addlater = index == -1;
						
						int sign = event.getDwheel() > 0 ? 1 : -1;
						
						index = (index + sign) % cap.getPalette().size();
						if(index < 0)
							index = cap.getPalette().size() - 1;
						ItemStack nextStack = cap.getPalette().get(index);
						
						if(addlater)
						{
							cap.getPalette().add(cap.getSelectedInventory().getStackInSlot(0));
							BuildersBag.network.sendToServer(new ModifyPaletteServer(cap.getUUID(), cap.getSelectedInventory().getStackInSlot(0), true));
						}
						
						cap.getSelectedInventory().setStackInSlot(0, nextStack.copy());
						BuildersBag.network.sendToServer(new SetSelectedBlockServer(cap.getUUID(), nextStack));
						if (BuildersBagConfig.Settings.playPickBlockSounds)
							player.playSound(SoundEvents.BLOCK_NOTE_HAT, 0.3f, 0.1f);
						event.setCanceled(true);
					}
				}
			}
		}
	}

}
