package tschipp.buildersbag.client.selectionwheel;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tschipp.buildersbag.BuildersBag;

@EventBusSubscriber(modid = BuildersBag.MODID, value = Dist.CLIENT)
public class SelectionWheelEvents
{
//	@SubscribeEvent
//	public static void onRenderScreen(RenderGameOverlayEvent.Post event) TODO
//	{
//		Minecraft mc = Minecraft.getInstance();
//
//		if (!SelectionWheel.open && BuildersBagKeybinds.isMenuKeyPressed())
//		{
//			ItemStack main = mc.player.getMainHandItem();
//			ItemStack off = mc.player.getMainHandItem();
//
//			if (main.getItem() instanceof BuildersBagItem)
//				SelectionWheel.setBag(main);
//			else if (off.getItem() instanceof BuildersBagItem)
//				SelectionWheel.setBag(off);
//
//			SelectionWheel.startAnimation();
//			mc.mouseHandler.releaseMouse();;
//		}
//		else if (SelectionWheel.open && !BuildersBagKeybinds.isMenuKeyPressed())
//		{
//			SelectionWheel.finishAnimation();
//			mc.mouseHandler.grabMouse();
//		}
//
//		if (SelectionWheel.shouldDraw)
//		{
//			SelectionWheel.draw(event.getPartialTicks(), event.getResolution());
//		}
//	}

//	@SubscribeEvent
//	public static void onMouseInput(MouseEvent event)
//	{
//		if (SelectionWheel.open)
//		{
//			if (event.getDwheel() > 0 || event.getDwheel() < 0)
//			{
//				SelectionWheel.onScroll(event.getDwheel());
//			}
//
//			if (event.isButtonstate() && (event.getButton() == 0 || event.getButton() == 1))
//			{
//				SelectionWheel.onClick(event.getButton() == 0 ? false : true);
//			}
//
//			event.setCanceled(true);
//		}
//
//		if (event.getDwheel() > 0 || event.getDwheel() < 0 && Minecraft.getInstance().player != null)
//		{
//			PlayerEntity player = Minecraft.getInstance().player;
//
//			if (player.isShiftKeyDown())
//			{
//				ItemStack main = player.getMainHandItem();
//				ItemStack off = player.getMainHandItem();
//
//				ItemStack bag = ItemStack.EMPTY;
//
//				if (main.getItem() instanceof BuildersBagItem)
//					bag = main;
//				else if (off.getItem() instanceof BuildersBagItem)
//					bag = off;
//
//				if (!bag.isEmpty())
//				{
//					IBagCap cap = CapHelper.getBagCap(bag);
//					int index = -1;
//
//					if (cap.getPalette().size() > 1)
//					{
//						for (int i = 0; i < cap.getPalette().size(); i++)
//						{
//							if (ItemStack.matches(cap.getPalette().get(i), cap.getSelectedInventory().getStackInSlot(0)))
//							{
//								index = i;
//								break;
//							}
//						}
//						boolean addlater = index == -1;
//						
//						int sign = event.getDwheel() > 0 ? 1 : -1;
//						
//						index = (index + sign) % cap.getPalette().size();
//						if(index < 0)
//							index = cap.getPalette().size() - 1;
//						ItemStack nextStack = cap.getPalette().get(index);
//						
//						if(addlater)
//						{
//							cap.getPalette().add(cap.getSelectedInventory().getStackInSlot(0));
//							BuildersBag.network.sendToServer(new ModifyPaletteServer(cap.getUUID(), cap.getSelectedInventory().getStackInSlot(0), true));
//						}
//						
//						cap.getSelectedInventory().setStackInSlot(0, nextStack.copy());
//						BuildersBag.network.sendToServer(new SetSelectedBlockServer(cap.getUUID(), nextStack));
//						if (Configs.Settings.playPickBlockSounds.get())
//							player.playSound(SoundEvents.BLOCK_NOTE_HAT, 0.3f, 0.1f);
//						event.setCanceled(true);
//					}
//				}
//			}
//		}
//	}

}
