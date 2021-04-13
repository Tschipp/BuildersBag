package tschipp.buildersbag.client.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.lazy.baubles.api.cap.IBaublesItemHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.client.BuildersBagKeybinds;
import tschipp.buildersbag.common.config.BuildersBagConfig;
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

	@SubscribeEvent
	public static void onMousePressed(MouseEvent event)
	{
		PlayerEntity player = Minecraft.getInstance().player;

		if (!event.isButtonstate())
			return;

		if (Minecraft.getInstance().gameSettings.keyBindPickBlock.isActiveAndMatches(event.getButton() - 100))
		{
			ItemStack main = player.getHeldItemMainhand();
			ItemStack off = player.getHeldItemOffhand();
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
				RayTraceResult ray = player.rayTrace(6.5, 0);
				if (ray.typeOfHit == Type.BLOCK)
				{
					IBlockState state = player.world.getBlockState(ray.getBlockPos());

					Tuple<String, IBlockState> orestage = StageHelper.getOreStage(state);

					if (!StageHelper.hasStage(player, orestage.getFirst()))
					{
						state = orestage.getSecond();
					}

					ItemStack pickBlock = state.getBlock().getPickBlock(state, ray, player.world, ray.getBlockPos(), player);

					if (!StageHelper.hasStage(player, StageHelper.getItemStage(pickBlock)))
					{
						return;
					}

					if (!pickBlock.isEmpty() && pickBlock.getItem() instanceof BlockItem)
					{
						IBagCap bag = CapHelper.getBagCap(stack);
						bag.getSelectedInventory().setStackInSlot(0, pickBlock.copy());

						if (BuildersBagConfig.Settings.playPickBlockSounds)
							player.playSound(SoundEvents.BLOCK_NOTE_HAT, 0.5f, 0.1f);
						
						BuildersBag.network.sendToServer(new SyncBagCapServer(bag, hand));

						event.setCanceled(true);
					}
				}
			}
		}

	}

	@SubscribeEvent
	public static void onKeyPress(InputEvent.KeyInputEvent event)
	{
		PlayerEntity player = Minecraft.getInstance().player;

		if (ModList.get().isLoaded("baubles") && BuildersBagKeybinds.openBaubleBag.isPressed() && FMLClientHandler.instance().getClient().inGameHasFocus)
		{
			IBaublesItemHandler baubles = BaubleHelper.getBaubles(player);
			for (int i = 0; i < baubles.getSlots(); i++)
			{
				if (baubles.getStackInSlot(i).getItem() instanceof BuildersBagItem)
				{
					BuildersBag.network.sendToServer(new OpenBaubleBagServer(i));
					player.openGui(BuildersBag.instance, 1, player.world, i, 0, 0);
					return;
				}
			}
		}

		if (Minecraft.getInstance().gameSettings.keyBindPickBlock.isKeyDown())
		{
			ItemStack main = player.getHeldItemMainhand();
			ItemStack off = player.getHeldItemOffhand();
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
				RayTraceResult ray = player.rayTrace(6.5, 0);
				if (ray.typeOfHit == Type.BLOCK)
				{
					IBlockState state = player.world.getBlockState(ray.getBlockPos());

					Tuple<String, IBlockState> orestage = StageHelper.getOreStage(state);

					if (!StageHelper.hasStage(player, orestage.getFirst()))
					{
						state = orestage.getSecond();
					}

					ItemStack pickBlock = state.getBlock().getPickBlock(state, ray, player.world, ray.getBlockPos(), player);

					if (!StageHelper.hasStage(player, StageHelper.getItemStage(pickBlock)))
					{
						return;
					}

					if (!pickBlock.isEmpty() && pickBlock.getItem() instanceof BlockItem)
					{
						IBagCap bag = CapHelper.getBagCap(stack);
						bag.getSelectedInventory().setStackInSlot(0, pickBlock.copy());

						if (BuildersBagConfig.Settings.playPickBlockSounds)
							player.playSound(SoundEvents.BLOCK_NOTE_HAT, 0.5f, 0.1f);

						BuildersBag.network.sendToServer(new SyncBagCapServer(bag, hand));

						try
						{
							unpressKey.invoke(Minecraft.getInstance().gameSettings.keyBindPickBlock);
						}
						catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}

	}
}
