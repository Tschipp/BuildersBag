package tschipp.buildersbag.client.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import baubles.api.BaublesApi;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.client.BuildersBagKeybinds;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.network.client.SyncBagCapServer;
import tschipp.buildersbag.network.server.OpenBaubleBagServer;

@EventBusSubscriber(modid = BuildersBag.MODID, value = Side.CLIENT)
public class ClientEvents
{
	private static final Method unpressKey;
	
	static {
		unpressKey = ReflectionHelper.findMethod(KeyBinding.class, "unpressKey", "func_74505_d");
		unpressKey.setAccessible(true);
	}
	
	@SubscribeEvent
	public static void onMousePressed(MouseEvent event)
	{
		EntityPlayer player = Minecraft.getMinecraft().player;

		if (Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isActiveAndMatches(event.getButton() - 100))
		{
			ItemStack main = player.getHeldItemMainhand();
			ItemStack off = player.getHeldItemOffhand();
			ItemStack stack = ItemStack.EMPTY;
			EnumHand hand = null;

			if (main.getItem() instanceof BuildersBagItem)
			{
				stack = main;
				hand = EnumHand.MAIN_HAND;
			} else if (off.getItem() instanceof BuildersBagItem)
			{
				stack = off;
				hand = EnumHand.OFF_HAND;
			}

			if (!stack.isEmpty())
			{
				RayTraceResult ray = player.rayTrace(6.5, 0);
				if (ray.typeOfHit == Type.BLOCK)
				{
					IBlockState state = player.world.getBlockState(ray.getBlockPos());

					ItemStack pickBlock = state.getBlock().getPickBlock(state, ray, player.world, ray.getBlockPos(), player);

					if (!pickBlock.isEmpty() && pickBlock.getItem() instanceof ItemBlock)
					{
						IBagCap bag = CapHelper.getBagCap(stack);
						bag.getSelectedInventory().setStackInSlot(0, pickBlock.copy());

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
		EntityPlayer player = Minecraft.getMinecraft().player;

		if (Loader.isModLoaded("baubles") && BuildersBagKeybinds.openBaubleBag.isPressed() && FMLClientHandler.instance().getClient().inGameHasFocus)
		{
			IInventory baubles = BaublesApi.getBaubles(player);
			for (int i = 0; i < baubles.getSizeInventory(); i++)
			{
				if (baubles.getStackInSlot(i).getItem() instanceof BuildersBagItem)
				{
					BuildersBag.network.sendToServer(new OpenBaubleBagServer(i));
					player.openGui(BuildersBag.instance, 1, player.world, i, 0, 0);
					return;
				}
			}
		}
		
		if (Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isKeyDown())
		{
			ItemStack main = player.getHeldItemMainhand();
			ItemStack off = player.getHeldItemOffhand();
			ItemStack stack = ItemStack.EMPTY;
			EnumHand hand = null;

			if (main.getItem() instanceof BuildersBagItem)
			{
				stack = main;
				hand = EnumHand.MAIN_HAND;
			} else if (off.getItem() instanceof BuildersBagItem)
			{
				stack = off;
				hand = EnumHand.OFF_HAND;
			}

			if (!stack.isEmpty())
			{
				RayTraceResult ray = player.rayTrace(6.5, 0);
				if (ray.typeOfHit == Type.BLOCK)
				{
					IBlockState state = player.world.getBlockState(ray.getBlockPos());

					ItemStack pickBlock = state.getBlock().getPickBlock(state, ray, player.world, ray.getBlockPos(), player);

					if (!pickBlock.isEmpty() && pickBlock.getItem() instanceof ItemBlock)
					{
						IBagCap bag = CapHelper.getBagCap(stack);
						bag.getSelectedInventory().setStackInSlot(0, pickBlock.copy());

						BuildersBag.network.sendToServer(new SyncBagCapServer(bag, hand));

						try
						{
							unpressKey.invoke(Minecraft.getMinecraft().gameSettings.keyBindPickBlock);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}

	}
}
