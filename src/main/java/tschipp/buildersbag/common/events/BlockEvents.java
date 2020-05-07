package tschipp.buildersbag.common.events;

import com.creativemd.littletiles.common.event.ActionEvent;
import com.creativemd.littletiles.common.event.ActionEvent.ActionType;

import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.iterators.ChiselIterator;
import mod.chiselsandbits.chiseledblock.iterators.ChiselTypeIterator;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ActingPlayer;
import mod.chiselsandbits.helpers.BitOperation;
import mod.chiselsandbits.helpers.ChiselModeManager;
import mod.chiselsandbits.helpers.ContinousBits;
import mod.chiselsandbits.helpers.IContinuousInventory;
import mod.chiselsandbits.helpers.VoxelRegionSrc;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.modes.ChiselMode;
import mod.chiselsandbits.modes.IToolMode;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.CustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.compat.linear.LinearCompatManager;
import tschipp.linear.api.LinearHooks;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class BlockEvents
{

	@SubscribeEvent
	public static void onBlockPlace(PlayerInteractEvent.RightClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		EnumFacing facing = event.getFace();

		if (player instanceof FakePlayer)
			return;

		ItemStack placementItem = player.getHeldItem(event.getHand());

		if (placementItem.getCount() == 1 && placementItem.getItem() instanceof ItemBlock && !player.isCreative())
		{
			Block block = Block.getBlockFromItem(placementItem.getItem());
			boolean canPlace = world.mayPlace(block, world.getBlockState(pos).getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing), false, facing, player);
			boolean canEdit = player.canPlayerEdit(pos, facing, placementItem);
			boolean b = canEdit && canPlace;

			if (b)
			{
				NonNullList<ItemStack> bags = InventoryHelper.getBagsInInventory(player);

				for (ItemStack bag : bags)
				{
					IBagCap bagCap = CapHelper.getBagCap(bag);
					for (IBagModule module : bagCap.getModules())
					{
						if (module.isEnabled() && module.isSupplier() && (Loader.isModLoaded("linear") ? !LinearCompatManager.isDragging(player) : true))
						{
							ItemStack provided = module.createStack(placementItem, bagCap, player);
							if (!provided.isEmpty())
							{
								placementItem.grow(1);
								return;
							}
						}
					}
				}
			}
		}

	}

}
