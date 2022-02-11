//package tschipp.buildersbag.compat.linear;
//
//import java.util.Random;
//
//import org.apache.commons.lang3.tuple.Triple;
//
//import net.minecraft.block.Block;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.entity.player.ServerPlayerEntity;
//import net.minecraft.item.BlockItem;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.Direction;
//import net.minecraft.util.Hand;
//import net.minecraft.util.NonNullList;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.RayTraceResult;
//import net.minecraft.world.World;
//import net.minecraftforge.common.util.FakePlayer;
//import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
//import tschipp.buildersbag.BuildersBag;
//import tschipp.buildersbag.api.IBagCap;
//import tschipp.buildersbag.api.IBagModule;
//import tschipp.buildersbag.client.rendering.BagItemStackRenderer;
//import tschipp.buildersbag.common.cache.BagCache;
//import tschipp.buildersbag.common.helper.BagHelper;
//import tschipp.buildersbag.common.helper.CapHelper;
//import tschipp.buildersbag.common.helper.InventoryHelper;
//import tschipp.buildersbag.common.item.BuildersBagItem;
//import tschipp.buildersbag.network.client.SyncBagCapClient;
//import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;
//
//@EventBusSubscriber(modid = BuildersBag.MODID) TODO
//public class LinearEvents
//{
//	// private static String lastTag = "";
//	private static int lastCount = 0;
//	private static int lastRequested = 0;
//	private static ItemStack lastSelected = ItemStack.EMPTY;
//	private static Random rand = new Random();
//
//	@Method(modid = "linear")
//	@SubscribeEvent(priority = EventPriority.LOW)
//	public static void onLinearRequest(LinearRequestEvent event)
//	{
//		ItemStack stack = event.getItemStack();
//		PlayerEntity player = event.getPlayer();
//		int requested = event.getRequestedBlocks();
//
//		requested -= event.getProvidedBlocks();
//
//		NonNullList<Triple<Integer, Boolean, ItemStack>> bags = InventoryHelper.getBagsInInventory(player);
//
//		int providedBlocks = 0;
//
//		if (player.isCreative())
//		{
//			event.setProvidedBlocks(event.getProvidedBlocks() + providedBlocks);
//			return;
//		}
//
//		for (Triple<Integer, Boolean, ItemStack> triple : bags)
//		{
//			ItemStack bag = triple.getRight();
//			
//			if (requested <= 0)
//				break;
//
//			if (player.level.isRemote)
//				BagCache.startSimulation(bag);
//
//			IBagCap bagCap = CapHelper.getBagCap(bag);
//			if (stack.getItem() instanceof BlockItem)
//			{
//				if (bagCap.hasModuleAndEnabled("buildersbag:supplier"))
//				{
//					NonNullList<ItemStack> provided = BagHelper.getOrProvideStackWithCount(stack, requested + (player.level.isRemote ? 1 : 0), bagCap, player, null);
//					if (!provided.isEmpty())
//						providedBlocks += provided.size() - (player.level.isRemote ? 1 : 0);
//
//					requested -= provided.size();
//
//					if (!player.level.isRemote)
//						for (ItemStack prov : provided)
//							BagHelper.addStack(prov, bagCap, player);
//				}
//			}
//
//			if (player.level.isRemote)
//				BagCache.stopSimulation(bag);
//
//		}
//
//		if (stack.getItem() instanceof BuildersBagItem)
//		{
//			IBagCap bagCap = CapHelper.getBagCap(stack);
//
//			ItemStack placementStack = ItemStack.EMPTY;
//			for (IBagModule module : BagHelper.getSortedModules(bagCap))
//			{
//				if (module.isEnabled() && module.isDominating())
//				{
//					placementStack = module.getBlock(bagCap, player);
//					break;
//				}
//			}
//
//			if (placementStack.isEmpty())
//			{
//				placementStack = bagCap.getSelectedInventory().getStackInSlot(0).copy();
//			}
//
//			if (placementStack.isEmpty() || !(placementStack.getItem() instanceof BlockItem))
//				return;
//
//			if (player.level.isRemote)
//				BagCache.startSimulation(stack);
//
//			NonNullList<ItemStack> provided = NonNullList.create();
//			int newlyProvided = 0;
//
//			if (bagCap.hasModuleAndEnabled("buildersbag:random"))
//			{
//				if (player.level.isRemote)
//				{
//					newlyProvided += BagHelper.getAllAvailableStacksCount(bagCap, player);
//					providedBlocks += newlyProvided;
//				}
//				else
//					provided = BagHelper.getOrProvideStackWithCountDominating(requested, bagCap, player);
//			}
//			else
//				provided = BagHelper.getOrProvideStackWithCount(placementStack, requested + (player.level.isRemote ? requested + 5 : 0), bagCap, player, null);
//			
//			if (!provided.isEmpty())
//				providedBlocks += provided.size();
//
//			if (!player.level.isRemote)
//				for (ItemStack prov : provided)
//					BagHelper.addStack(prov, bagCap, player);
//
//			requested -= (provided.size() + newlyProvided);
//
//			if (player.level.isRemote)
//				BagCache.stopSimulation(stack);
//		}
//
//		event.setProvidedBlocks(event.getProvidedBlocks() + providedBlocks);
//
//		// lastCount = providedBlocks;
//		// lastSelected = stack;
//	}
//
//	@Method(modid = "linear")
//	@SubscribeEvent(priority = EventPriority.HIGH)
//	public static void onLinearPlace(LinearPlaceBlockEvent event)
//	{
//		PlayerEntity player = event.getPlayer();
//		ItemStack stack = event.getItemStack();
//		World world = event.getWorld();
//		BlockPos pos = event.getPos();
//		Hand hand = LinearHelper.getHand(player);
//		Direction facing = LinearHelper.getFacing(player);
//		float[] hit = LinearHelper.getHitCoords(player);
//
//		NonNullList<Triple<Integer, Boolean, ItemStack>> bags = InventoryHelper.getBagsInInventory(player);
//
//		if (!world.isRemote)
//		{
//			if (stack.getItem() instanceof BlockItem && !player.isCreative())
//			{
//				for (Triple<Integer, Boolean, ItemStack> triple : bags)
//				{
//					ItemStack bagStack = triple.getRight();
//					IBagCap bag = CapHelper.getBagCap(bagStack);
//
//					if (bag.hasModuleAndEnabled("buildersbag:supplier"))
//					{
//						ItemStack result = BagHelper.getOrProvideStack(stack, bag, player, null);
//						if (!result.isEmpty())
//							stack.grow(1);
//
//						BuildersBag.network.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayerEntity) player), new SyncBagCapInventoryClient(bag, triple.getLeft(), triple.getMiddle()));
//						BuildersBag.network.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayerEntity) player), new SyncEnderchestToClient(player));
//
//						return;
//					}
//				}
//
//			}
//			else if (stack.getItem() instanceof BuildersBagItem)
//			{
//				IBagCap bag = CapHelper.getBagCap(stack);
//
//				boolean done = false;
//				int i = 0;
//
//				while (!done && i < 15)
//				{
//					ItemStack placementStack = ItemStack.EMPTY;
//
//					for (IBagModule module : BagHelper.getSortedModules(bag))
//					{
//						if (module.isEnabled() && module.isDominating())
//						{
//							placementStack = module.getBlock(bag, player);
//							break;
//						}
//					}
//
//					i++;
//
//					if (placementStack.isEmpty())
//					{
//						placementStack = bag.getSelectedInventory().getStackInSlot(0).copy();
//					}
//
//					if (placementStack.isEmpty() || !(placementStack.getItem() instanceof BlockItem))
//						return;
//
//					Block block = Block.getBlockFromItem(placementStack.getItem());
//					boolean canPlace = world.mayPlace(block, world.getBlockState(pos).getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing), false, facing, player);
//					boolean canEdit = player.canPlayerEdit(pos, facing, placementStack);
//
//					if (!canEdit || !canPlace)
//						continue;
//
//					ItemStack result = player.isCreative() ? placementStack.copy() : BagHelper.getOrProvideStack(placementStack, bag, player, null);
//
//					if (!result.isEmpty())
//					{
//						done = true;
//
//						FakePlayer fake = new FakePlayer((ServerWorld) world, player.getGameProfile());
//						fake.rotationPitch = player.rotationPitch;
//						fake.rotationYaw = player.rotationYaw;
//						fake.setPosition(0, 0, 0);
//
//						fake.setHeldItem(hand, result.copy());
//
//						fake.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
//
//						result.onItemUse(fake, world, pos, hand, facing, hit[0], hit[1], hit[2]);
//
//						BuildersBag.network.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayerEntity) player), new SyncBagCapClient(bag, hand));
//					}
//				}
//			}
//
//			BuildersBag.network.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayerEntity) player), new SyncEnderchestToClient(player));
//
//		}
//
//	}
//
//	@Method(modid = "linear")
//	@SubscribeEvent(priority = EventPriority.HIGH)
//	public static void onlinearRequestState(LinearBlockStateEvent event)
//	{
//		PlayerEntity player = event.getPlayer();
//		ItemStack stack = event.getStack();
//		World world = player.level;
//
//		if (stack.getItem() instanceof BuildersBagItem)
//		{
//			IBagCap bag = CapHelper.getBagCap(stack);
//			ItemStack placementStack = ItemStack.EMPTY;
//
//			for (int i = 0; i < 10; i++)
//			{
//				for (IBagModule module : BagHelper.getSortedModules(bag))
//				{
//					if (module.isEnabled() && module.isDominating())
//					{
//						placementStack = module.getBlock(bag, player);
//						break;
//					}
//				}
//
//				if (placementStack.isEmpty())
//				{
//					placementStack = bag.getSelectedInventory().getStackInSlot(0).copy();
//				}
//
//				if (placementStack.isEmpty() || !(placementStack.getItem() instanceof BlockItem))
//					continue;
//				else
//					break;
//			}
//
//			Block block = Block.getBlockFromItem(placementStack.getItem());
//
//			if (block != null)
//			{
//				event.setState(block.getStateFromMeta(placementStack.getMetadata()));
//			}
//
//		}
//	}
//
//	@OnlyIn(Dist.CLIENT)
//	@Method(modid = "linear")
//	@SubscribeEvent(priority = EventPriority.HIGH)
//	public static void onlinearRequestClientState(LinearRenderBlockStateEvent event)
//	{
//		PlayerEntity player = event.getPlayer();
//		ItemStack stack = event.getStack();
//		World world = player.level;
//		Hand hand = event.getHand();
//
//		if (stack.getItem() instanceof BuildersBagItem)
//		{
//			IBagCap bag = CapHelper.getBagCap(stack);
//			ItemStack placementStack = ItemStack.EMPTY;
//
//			for (int i = 0; i < 10; i++)
//			{
//				for (IBagModule module : BagHelper.getSortedModules(bag))
//				{
//					if (module.isEnabled() && module.isDominating())
//					{
//						if (module.getName().equals("buildersbag:random"))
//						{
//							NonNullList<ItemStack> available = BagHelper.getAllAvailableStacks(bag, player);
//							if (!available.isEmpty())
//							{
//								placementStack = available.get(BagItemStackRenderer.listIndex % available.size());
//							}
//						}
//						else
//							placementStack = module.getBlock(bag, player);
//						break;
//					}
//				}
//
//				if (placementStack.isEmpty())
//				{
//					placementStack = bag.getSelectedInventory().getStackInSlot(0).copy();
//				}
//
//				if (placementStack.isEmpty() || !(placementStack.getItem() instanceof BlockItem))
//					continue;
//				else
//					break;
//			}
//
//			RayTraceResult ray = LinearHelper.getLookRay(player);
//			if (ray != null)
//			{
//				Block block = Block.getBlockFromItem(placementStack.getItem());
//				EntityArmorStand stand = new EntityArmorStand(world);
//				stand.setHeldItem(hand, placementStack.copy());
//				stand.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
//				stand.rotationPitch = player.rotationPitch;
//				stand.rotationYaw = player.rotationYaw;
//
//				float[] hit = LinearHelper.getHitCoords(player);
//				BlockState state = block.getStateForPlacement(player.level, LinearHelper.getLookPos(player, LinearHelper.canPlaceInMidair(player)), ray.sideHit, hit[0], hit[1], hit[2], placementStack.getMetadata(), stand, hand);
//
//				if (state != null)
//					event.setState(state);
//
//			}
//
//		}
//	}
//}
