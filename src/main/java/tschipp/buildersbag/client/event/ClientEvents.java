package tschipp.buildersbag.client.event;

import java.lang.reflect.Method;

import com.lazy.baubles.api.cap.IBaublesItemHandler;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.client.BuildersBagKeybinds;
import tschipp.buildersbag.client.KeyboardCallbackWrapper.KeyPressedEvent;
import tschipp.buildersbag.common.config.Configs;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.compat.baubles.BaubleHelper;
import tschipp.buildersbag.compat.gamestages.StageHelper;
import tschipp.buildersbag.network.server.OpenBaubleBagServer;
import tschipp.buildersbag.network.server.SyncBagCapServer;

@EventBusSubscriber(modid = BuildersBag.MODID, value = Dist.CLIENT)
public class ClientEvents
{
	private static final Method unpressKey;

	static
	{
		unpressKey = ObfuscationReflectionHelper.findMethod(KeyBinding.class, "func_74505_d");
		unpressKey.setAccessible(true);
	}

//	@SubscribeEvent
//	public static void onMousePressed(MouseEvent event)
//	{
//		PlayerEntity player = Minecraft.getInstance().player;
//
//		if (!event.isButtonstate())
//			return;
//
//		if (Minecraft.getInstance().options.keyPickItem.isDown()
//		{
//			ItemStack main = player.getMainHandItem();
//			ItemStack off = player.getOffhandItem();
//			ItemStack stack = ItemStack.EMPTY;
//			Hand hand = null;
//
//			if (main.getItem() instanceof BuildersBagItem)
//			{
//				stack = main;
//				hand = Hand.MAIN_HAND;
//			}
//			else if (off.getItem() instanceof BuildersBagItem)
//			{
//				stack = off;
//				hand = Hand.OFF_HAND;
//			}
//
//			if (!stack.isEmpty())
//			{
//				RayTraceResult ray = player.rayTrace(6.5, 0);
//				if (ray.typeOfHit == Type.BLOCK)
//				{
//					BlockState state = player.level.getBlockState(ray.getBlockPos());
//
//					Tuple<String, BlockState> orestage = StageHelper.getOreStage(state);
//
//					if (!StageHelper.hasStage(player, orestage.getFirst()))
//					{
//						state = orestage.getSecond();
//					}
//
//					ItemStack pickBlock = state.getBlock().getPickBlock(state, ray, player.level, ray.getBlockPos(), player);
//
//					if (!StageHelper.hasStage(player, StageHelper.getItemStage(pickBlock)))
//					{
//						return;
//					}
//
//					if (!pickBlock.isEmpty() && pickBlock.getItem() instanceof BlockItem)
//					{
//						IBagCap bag = CapHelper.getBagCap(stack);
//						bag.getSelectedInventory().setStackInSlot(0, pickBlock.copy());
//
//						if (Configs.Settings.playPickBlockSounds.get())
//							player.playSound(SoundEvents.NOTE_BLOCK_HAT, 0.5f, 0.1f);
//						
//						BuildersBag.network.sendToServer(new SyncBagCapServer(bag, hand));
//
//						event.setCanceled(true);
//					}
//				}
//			}
//		}
//
//	}

	@SubscribeEvent
	public static void onKeyPress(KeyPressedEvent event)
	{
		if(event.action != 1)
			return;
		
		PlayerEntity player = Minecraft.getInstance().player;

		if (ModList.get().isLoaded("baubles") && BuildersBagKeybinds.openBaubleBag.consumeClick() && Minecraft.getInstance().mouseHandler.isMouseGrabbed())
		{
			IBaublesItemHandler baubles = BaubleHelper.getBaubles(player);
			for (int i = 0; i < baubles.getSlots(); i++)
			{
				if (baubles.getStackInSlot(i).getItem() instanceof BuildersBagItem)
				{
					BuildersBag.network.sendToServer(new OpenBaubleBagServer(i));
//					player.openGui(BuildersBag.instance, 1, player.level, i, 0, 0);
					return;
				}
			}
		}

		if (Minecraft.getInstance().options.keyPickItem.matches(event.key, event.scancode))
		{
			ItemStack main = player.getMainHandItem();
			ItemStack off = player.getOffhandItem();
			ItemStack stack = ItemStack.EMPTY;
			Hand hand = null;

			if (main.getItem() instanceof BuildersBagItem)
			{
				stack = main;
				hand = Hand.MAIN_HAND;
			}
			else if (off.getItem() instanceof BuildersBagItem)
			{
				stack = off;
				hand = Hand.OFF_HAND;
			}

			if (!stack.isEmpty())
			{
				RayTraceResult ray = player.pick(6.5, 0, false);
				if (ray.getType() == Type.BLOCK)
				{
					BlockState state = player.level.getBlockState(((BlockRayTraceResult)ray).getBlockPos());

//					Tuple<String, BlockState> orestage = StageHelper.getOreStage(state); TODO Orstages
//
//					if (!StageHelper.hasStage(player, orestage.getFirst()))
//					{
//						state = orestage.getSecond();
//					}

					ItemStack pickBlock = state.getBlock().getPickBlock(state, ray, player.level, ((BlockRayTraceResult)ray).getBlockPos(), player);

					if (!StageHelper.hasStage(player, StageHelper.getItemStage(pickBlock)))
					{
						return;
					}

					if (!pickBlock.isEmpty() && pickBlock.getItem() instanceof BlockItem)
					{
						IBagCap bag = CapHelper.getBagCap(stack);
						bag.getSelectedInventory().setStackInSlot(0, pickBlock.copy());

						if (Configs.Settings.playPickBlockSounds.get())
							player.playSound(SoundEvents.NOTE_BLOCK_HAT, 0.5f, 0.1f);

						BuildersBag.network.sendToServer(new SyncBagCapServer(bag, hand));

//						try
//						{
//							unpressKey.invoke(Minecraft.getInstance().options.keyPickItem);
//						}
//						catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
//						{
//							e.printStackTrace();
//						}
						event.setCanceled(true);
					}
				}
			}
		}

	}
}
