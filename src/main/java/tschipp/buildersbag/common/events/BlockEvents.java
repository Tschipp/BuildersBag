package tschipp.buildersbag.common.events;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.cache.BagCache;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.compat.linear.LinearCompatManager;
import tschipp.buildersbag.network.client.SetHeldItemClient;
import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;
import tschipp.buildersbag.network.client.SyncEnderchestToClient;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class BlockEvents
{

	@SubscribeEvent
	public static void onBlockPlace(PlayerInteractEvent.RightClickBlock event)
	{
		PlayerEntity player = event.getPlayerEntity();
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
				NonNullList<Triple<Integer, Boolean, ItemStack>> bags = InventoryHelper.getBagsInInventory(player);
				
				for (Triple<Integer, Boolean, ItemStack> triple : bags)
				{
					ItemStack bag = triple.getRight();
					IBagCap bagCap = CapHelper.getBagCap(bag);
					
					for (IBagModule module : BagHelper.getSortedModules(bagCap))
					{
						if (module.isEnabled() && module.isSupplier() && (Loader.isModLoaded("linear") ? !LinearCompatManager.isDragging(player) : true))
						{

							ItemStack provided = module.createStack(placementItem, bagCap, player);
							if (!provided.isEmpty())
							{
								ItemStack s = placementItem.copy();

								placementItem.grow(1);
								
								if (!player.world.isRemote)
								{
									BuildersBag.network.sendTo(new SyncBagCapInventoryClient(bagCap, triple.getLeft(), triple.getMiddle()), (ServerPlayerEntity) player);
									BuildersBag.network.sendTo(new SyncEnderchestToClient(player), (ServerPlayerEntity) player);	
								}
								return;
							}
							else if(world.isRemote)
								placementItem.grow(1);

						}
					}
				}
			}
		}

	}
	
	@SubscribeEvent
	public static void onLogin(EntityJoinWorldEvent event)
	{
		Entity e = event.getEntity();
		World world = event.getWorld();
		if(e instanceof PlayerEntity && !world.isRemote)
		{
			ServerPlayerEntity player = (ServerPlayerEntity) e;
			BuildersBag.network.sendTo(new SyncEnderchestToClient(player), (ServerPlayerEntity) player);
		}
	}

	@SubscribeEvent
	public static void onExitWorld(WorldEvent.Unload event)
	{
		BagCache.clearCache();
	}
}
