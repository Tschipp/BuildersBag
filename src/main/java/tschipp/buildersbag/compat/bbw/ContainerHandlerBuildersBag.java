//package tschipp.buildersbag.compat.bbw;
//
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.entity.player.ServerPlayerEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.NonNullList;
//import tschipp.buildersbag.BuildersBag;
//import tschipp.buildersbag.api.IBagCap;
//import tschipp.buildersbag.common.cache.BagCache;
//import tschipp.buildersbag.common.helper.BagHelper;
//import tschipp.buildersbag.common.helper.CapHelper;
//import tschipp.buildersbag.common.helper.InventoryHelper;
//import tschipp.buildersbag.common.item.BuildersBagItem;
//import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;
//
//public class ContainerHandlerBuildersBag implements IContainerHandler
//{
//	private ItemStack lastFailureStack = ItemStack.EMPTY;
//	private boolean hasFailed = false;
//	private long lastFailureTime = 0;
//	
//	@Override
//	public int countItems(PlayerEntity player, ItemStack stack, ItemStack inventoryStack) TODO
//	{
////		String nbt = inventoryStack.serializeNBT().toString();
////		if(nbt.equals(lastTag) && ItemStack.areItemsEqual(lastSelected, stack))
////			return lastCount;
//		
//		IBagCap bag = CapHelper.getBagCap(inventoryStack);
//		NonNullList<ItemStack> provided = null;
//
//		long time = System.currentTimeMillis();
//		
//		if(player.world.isRemote)
//		{
//			BagCache.startSimulation(inventoryStack);
//			provided = BagHelper.getOrProvideStackWithCount(stack, 500, bag, player, null);
//			BagCache.stopSimulation(inventoryStack);
//		}
//		else
////			provided = InventoryHelper.simulateProvideStackWithCount(stack, 500, inventoryStack, player, null);
//			return 500;
////		
//
////		System.out.println(System.currentTimeMillis() - time + "ms needed to get value");
//		
////		lastTag = nbt;
////		lastCount = provided.size();
////		lastSelected = stack.copy();
//		
//		
//		
//		return provided.size();
//	}
//
//	@Override
//	public boolean matches(PlayerEntity player, ItemStack stack, ItemStack inventoryStack)
//	{
//		if(inventoryStack.getItem() instanceof BuildersBagItem)
//		{
//			IBagCap bag = CapHelper.getBagCap(inventoryStack);
//			if(bag.hasModuleAndEnabled("buildersbag:supplier"))
//				return true;
//		}
//		return false;
//	}
//
//	@Override
//	public int useItems(PlayerEntity player, ItemStack stack, ItemStack inventoryStack, int count)
//	{
//		if(hasFailed && System.currentTimeMillis() - lastFailureTime <= 1000 && ItemStack.areItemsEqual(stack, lastFailureStack))
//		{
//			return count;
//		}
//		else
//		{
//			hasFailed = false;
//			lastFailureTime = 0;
//			lastFailureStack = ItemStack.EMPTY;
//		}
//		
//		IBagCap bag = CapHelper.getBagCap(inventoryStack);
//		NonNullList<ItemStack> provided = BagHelper.getOrProvideStackWithCount(stack, count, bag, player, null);
////		BuildersBag.network.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayerEntity) player), new SyncBagCapInventoryClient(bag, InventoryHelper.getSlotForStack(player, inventoryStack)));
//		
//		if(provided.size() == 0)
//		{
//			lastFailureStack = stack;
//			lastFailureTime = System.currentTimeMillis();
//			hasFailed = true;
//		}
//		
//		return count - provided.size();
//	}
//
//}
