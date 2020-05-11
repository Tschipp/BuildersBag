package tschipp.buildersbag.common.events;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.compat.linear.LinearCompatManager;
import tschipp.buildersbag.network.SyncBagCapInventoryClient;
import tschipp.buildersbag.network.SyncEnderchestToClient;

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

								if (!player.world.isRemote)
								{
									BuildersBag.network.sendTo(new SyncBagCapInventoryClient(bagCap, InventoryHelper.getSlotForStack(player, bag)), (EntityPlayerMP) player);
									BuildersBag.network.sendTo(new SyncEnderchestToClient(player), (EntityPlayerMP) player);
								}
								return;
							}
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
		if(e instanceof EntityPlayer && !world.isRemote)
		{
			EntityPlayerMP player = (EntityPlayerMP) e;
			BuildersBag.network.sendTo(new SyncEnderchestToClient(player), (EntityPlayerMP) player);
		}
	}

}
