package tschipp.buildersbag.compat.linear;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.client.rendering.BagItemStackRenderer;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.network.SyncBagCapClient;
import tschipp.buildersbag.network.SyncBagCapInventoryClient;
import tschipp.buildersbag.network.SyncEnderchestToClient;
import tschipp.linear.api.LinearBlockStateEvent;
import tschipp.linear.api.LinearPlaceBlockEvent;
import tschipp.linear.api.LinearRenderBlockStateEvent;
import tschipp.linear.api.LinearRequestEvent;
import tschipp.linear.common.helper.LinearHelper;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class LinearEvents
{
//	private static String lastTag = "";
	private static int lastCount = 0;
	private static int lastRequested = 0;
	private static ItemStack lastSelected = ItemStack.EMPTY;
	private static Random rand = new Random();

	@Method(modid = "linear")
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onLinearRequest(LinearRequestEvent event)
	{
		ItemStack stack = event.getItemStack();
		EntityPlayer player = event.getPlayer();
		int requested = event.getRequestedBlocks();

		requested -= event.getProvidedBlocks();

		if(player.world.isRemote && ItemStack.areItemsEqual(lastSelected, stack))
		{
			if(lastRequested == requested)
			{
				event.setProvidedBlocks(event.getProvidedBlocks() + lastCount);
				return;
			}
			if(rand.nextDouble() < 0.90 && requested - lastRequested == 1)
			{
				event.setProvidedBlocks(event.getProvidedBlocks() + lastCount + 1);
				return;
			}
		}
		
		lastRequested = requested;
		
		NonNullList<ItemStack> bags = InventoryHelper.getBagsInInventory(player);

		int providedBlocks = 0;

		if (player.isCreative())
		{
			event.setProvidedBlocks(event.getProvidedBlocks() + providedBlocks);
			return;
		}

		for (ItemStack bag : bags)
		{
			if (requested <= 0)
				break;

			IBagCap bagCap = CapHelper.getBagCap(bag);
			if (stack.getItem() instanceof ItemBlock)
			{
				if (bagCap.hasModuleAndEnabled("buildersbag:supplier"))
				{
					NonNullList<ItemStack> provided = InventoryHelper.getOrProvideStackWithCount(stack, requested, bagCap, player, null);
					if (!provided.isEmpty())
						providedBlocks += provided.size();

					requested -= provided.size();

					if (!player.world.isRemote)
						for (ItemStack prov : provided)
							InventoryHelper.addStack(prov, bagCap, player);
				}
			}
		}

		if (stack.getItem() instanceof BuildersBagItem)
		{
			IBagCap bagCap = CapHelper.getBagCap(stack);

			ItemStack placementStack = ItemStack.EMPTY;
			for (IBagModule module : bagCap.getModules())
			{
				if (module.isEnabled() && module.isDominating())
				{
					placementStack = module.getBlock(bagCap, player);
					break;
				}
			}

			if (placementStack.isEmpty())
			{
				placementStack = bagCap.getSelectedInventory().getStackInSlot(0).copy();
			}

			if (placementStack.isEmpty() || !(placementStack.getItem() instanceof ItemBlock))
				return;

			NonNullList<ItemStack> provided = NonNullList.create();
			int newlyProvided = 0;

			if (bagCap.hasModuleAndEnabled("buildersbag:random"))
			{
				if (player.world.isRemote)
				{
					newlyProvided += InventoryHelper.getAllAvailableStacksCount(bagCap, player);
					providedBlocks += newlyProvided;
				} else
					provided = InventoryHelper.getOrProvideStackWithCountDominating(requested, bagCap, player);
			} else
				provided = InventoryHelper.getOrProvideStackWithCount(placementStack, requested, bagCap, player, null);

			if (!provided.isEmpty())
				providedBlocks += provided.size();

			if (!player.world.isRemote)
				for (ItemStack prov : provided)
					InventoryHelper.addStack(prov, bagCap, player);

			requested -= (provided.size() + newlyProvided);
		}

		event.setProvidedBlocks(event.getProvidedBlocks() + providedBlocks);
		
		lastCount = providedBlocks;
		lastSelected = stack;
	}

	@Method(modid = "linear")
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLinearPlace(LinearPlaceBlockEvent event)
	{
		EntityPlayer player = event.getPlayer();
		ItemStack stack = event.getItemStack();
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		EnumHand hand = LinearHelper.getHand(player);
		EnumFacing facing = LinearHelper.getFacing(player);
		float[] hit = LinearHelper.getHitCoords(player);

		NonNullList<ItemStack> bags = InventoryHelper.getBagsInInventory(player);

		if (!world.isRemote)
		{
			if (stack.getItem() instanceof ItemBlock && !player.isCreative())
			{
				for (ItemStack bagStack : bags)
				{
					IBagCap bag = CapHelper.getBagCap(bagStack);

					if (bag.hasModuleAndEnabled("buildersbag:supplier"))
					{
						ItemStack result = InventoryHelper.getOrProvideStack(stack, bag, player, null);
						if (!result.isEmpty())
							stack.grow(1);
						
						BuildersBag.network.sendTo(new SyncBagCapInventoryClient(bag, InventoryHelper.getSlotForStack(player, bagStack)), (EntityPlayerMP) player);
						BuildersBag.network.sendTo(new SyncEnderchestToClient(player), (EntityPlayerMP) player);

						return;
					}
				}
				
				

			} else if (stack.getItem() instanceof BuildersBagItem)
			{
				IBagCap bag = CapHelper.getBagCap(stack);

				boolean done = false;
				int i = 0;

				while (!done && i < 15)
				{
					ItemStack placementStack = ItemStack.EMPTY;

					for (IBagModule module : bag.getModules())
					{
						if (module.isEnabled() && module.isDominating())
						{
							placementStack = module.getBlock(bag, player);
							break;
						}
					}

					i++;

					if (placementStack.isEmpty())
					{
						placementStack = bag.getSelectedInventory().getStackInSlot(0).copy();
					}

					if (placementStack.isEmpty() || !(placementStack.getItem() instanceof ItemBlock))
						return;

					Block block = Block.getBlockFromItem(placementStack.getItem());
					boolean canPlace = world.mayPlace(block, world.getBlockState(pos).getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing), false, facing, player);
					boolean canEdit = player.canPlayerEdit(pos, facing, placementStack);

					if (!canEdit || !canPlace)
						continue;

					ItemStack result = player.isCreative() ? placementStack.copy() : InventoryHelper.getOrProvideStack(placementStack, bag, player, null);

					if (!result.isEmpty())
					{
						done = true;

						FakePlayer fake = new FakePlayer((WorldServer) world, player.getGameProfile());
						fake.rotationPitch = player.rotationPitch;
						fake.rotationYaw = player.rotationYaw;
						fake.setPosition(0, 0, 0);

						fake.setHeldItem(hand, result.copy());

						fake.setPosition(player.posX, player.posY, player.posZ);

						result.onItemUse(fake, world, pos, hand, facing, hit[0], hit[1], hit[2]);

						BuildersBag.network.sendTo(new SyncBagCapClient(bag, hand), (EntityPlayerMP) player);
					}
				}
			}

			BuildersBag.network.sendTo(new SyncEnderchestToClient(player), (EntityPlayerMP) player);

		}

	}

	@Method(modid = "linear")
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onlinearRequestState(LinearBlockStateEvent event)
	{
		EntityPlayer player = event.getPlayer();
		ItemStack stack = event.getStack();
		World world = player.world;

		if (stack.getItem() instanceof BuildersBagItem)
		{
			IBagCap bag = CapHelper.getBagCap(stack);
			ItemStack placementStack = ItemStack.EMPTY;

			for (int i = 0; i < 10; i++)
			{
				for (IBagModule module : bag.getModules())
				{
					if (module.isEnabled() && module.isDominating())
					{
						placementStack = module.getBlock(bag, player);
						break;
					}
				}

				if (placementStack.isEmpty())
				{
					placementStack = bag.getSelectedInventory().getStackInSlot(0).copy();
				}

				if (placementStack.isEmpty() || !(placementStack.getItem() instanceof ItemBlock))
					continue;
				else
					break;
			}

			Block block = Block.getBlockFromItem(placementStack.getItem());

			if (block != null)
			{
				event.setState(block.getStateFromMeta(placementStack.getMetadata()));
			}

		}
	}

	@SideOnly(Side.CLIENT)
	@Method(modid = "linear")
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onlinearRequestClientState(LinearRenderBlockStateEvent event)
	{
		EntityPlayer player = event.getPlayer();
		ItemStack stack = event.getStack();
		World world = player.world;
		EnumHand hand = event.getHand();

		if (stack.getItem() instanceof BuildersBagItem)
		{
			IBagCap bag = CapHelper.getBagCap(stack);
			ItemStack placementStack = ItemStack.EMPTY;

			for (int i = 0; i < 10; i++)
			{
				for (IBagModule module : bag.getModules())
				{
					if (module.isEnabled() && module.isDominating())
					{
						if (module.getName().equals("buildersbag:random"))
						{
							NonNullList<ItemStack> available = InventoryHelper.getAllAvailableStacks(bag, player);
							if (!available.isEmpty())
							{
								placementStack = available.get(BagItemStackRenderer.listIndex % available.size());
							}
						} else
							placementStack = module.getBlock(bag, player);
						break;
					}
				}

				if (placementStack.isEmpty())
				{
					placementStack = bag.getSelectedInventory().getStackInSlot(0).copy();
				}

				if (placementStack.isEmpty() || !(placementStack.getItem() instanceof ItemBlock))
					continue;
				else
					break;
			}

			RayTraceResult ray = LinearHelper.getLookRay(player);
			if (ray != null)
			{
				Block block = Block.getBlockFromItem(placementStack.getItem());
				EntityArmorStand stand = new EntityArmorStand(world);
				stand.setHeldItem(hand, placementStack.copy());
				stand.setPosition(player.posX, player.posY, player.posZ);
				stand.rotationPitch = player.rotationPitch;
				stand.rotationYaw = player.rotationYaw;

				float[] hit = LinearHelper.getHitCoords(player);
				IBlockState state = block.getStateForPlacement(player.world, LinearHelper.getLookPos(player, LinearHelper.canPlaceInMidair(player)), ray.sideHit, hit[0], hit[1], hit[2], stack.getMetadata(), stand, hand);

				if (state != null)
					event.setState(state);

			}

		}
	}
}
