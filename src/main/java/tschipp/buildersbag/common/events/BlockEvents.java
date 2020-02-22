package tschipp.buildersbag.common.events;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.caps.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;

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
			boolean canPlace = world.mayPlace(block, pos.offset(facing), false, facing, player);
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
						if (module.isEnabled() && module.isSupplier())
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
