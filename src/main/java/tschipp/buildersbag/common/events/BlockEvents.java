package tschipp.buildersbag.common.events;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.BagComplex;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.cache.BagCache;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class BlockEvents
{

	@SubscribeEvent
	public static void onBlockPlace(PlayerInteractEvent.RightClickBlock event)
	{
		PlayerEntity player = event.getPlayer();
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		Direction facing = event.getFace();

		if (player instanceof FakePlayer)
			return;

		ItemStack placementItem = player.getItemInHand(event.getHand());

		if (placementItem.getCount() == 1 && placementItem.getItem() instanceof BlockItem && !player.isCreative())
		{
			Block block = Block.byItem(placementItem.getItem());
			
			boolean canPlace = block.defaultBlockState().canSurvive(world, world.getBlockState(pos).getMaterial().isReplaceable() ? pos : pos.relative(facing));
			boolean canEdit = player.mayUseItemAt(pos, facing, placementItem);
			boolean b = canEdit && canPlace;

			if (b)
			{
				NonNullList<Triple<Integer, Boolean, ItemStack>> bags = InventoryHelper.getBagsInInventory(player);
				
				for (Triple<Integer, Boolean, ItemStack> triple : bags)
				{
					ItemStack bag = triple.getRight();
					IBagCap bagCap = CapHelper.getBagCap(bag);
					BagComplex complex = bagCap.getComplex();
					
					for (IBagModule module : BagHelper.getSortedModules(bagCap))
					{
						if (module.isEnabled() && module.isSupplier() /*&& (ModList.get().isLoaded("linear") ? !LinearCompatManager.isDragging(player) : true)*/) //TODO: Linear
						{
							if (complex.take(placementItem.getItem(), 1, player) > 0)
							{
								placementItem.grow(1);
								
								if (!player.level.isClientSide)
								{
									BuildersBag.network.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayerEntity) player), new SyncBagCapInventoryClient(bagCap, triple.getLeft(), triple.getMiddle()));	
								}
								return;
							}
							else if(world.isClientSide)
								placementItem.grow(1);

						}
					}
				}
			}
		}

	}

	@SubscribeEvent
	public static void onExitWorld(WorldEvent.Unload event)
	{
		BagCache.clearCache();
	}
}
