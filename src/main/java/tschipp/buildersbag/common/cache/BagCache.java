package tschipp.buildersbag.common.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.data.Tuple;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.network.client.ModifyCacheClient;
import tschipp.buildersbag.network.client.UpdateCacheClient;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class BagCache
{
	public static final int CRITICAL_POINT_START = 30;
	public static final int PROVIDE_AMOUNT = 500;

	private static final Map<String, CachedBag> client_cache = new HashMap<String, CachedBag>();
	private static final Map<String, CachedBag> server_cache = new HashMap<String, CachedBag>();

	public static void clearCache()
	{
		client_cache.clear();
		server_cache.clear();
	}
	
	
	public static void clearBagCache(ItemStack bag)
	{
		Side side = BuildersBag.proxy.getSide();
		Map<String, CachedBag> cache = side == Side.CLIENT ? client_cache : server_cache;
		IBagCap bagCap = CapHelper.getBagCap(bag);

		if(bagCap == null)
			return;
		
		CachedBag cachedBag = cache.get(bagCap.getUUID());
		if (cachedBag != null)
			cachedBag.clearCache();
	}
	

	/**
	 * This should ONLY be called from the server side
	 */
	public static int updateCachedBagStack(ItemStack bag, EntityPlayer player, ItemStack forStack, int preferredAmount)
	{
		IBagCap bagCap = CapHelper.getBagCap(bag);
		int count = BagHelper.simulateProvideStackWithCount(forStack, preferredAmount, bag, player, null).size();

		CachedBag cachedBag = server_cache.get(bagCap.getUUID());
		if (cachedBag == null)
			cachedBag = new CachedBag(player, bag);
		cachedBag.updatePlayer(player);
		cachedBag.updateCachedAmount(forStack, count);

		Tuple<Boolean, Integer> slot = InventoryHelper.getSlotForStackWithBaubles(player, bag);
		
		BuildersBag.network.sendTo(new UpdateCacheClient(slot.getSecond(), slot.getFirst(), forStack, count), (EntityPlayerMP) player);
		return count;
	}

	/**
	 * This should ONLY be called from the client side
	 */
	@SideOnly(Side.CLIENT)
	public static void updateCachedBagStackWithAmount(ItemStack bag, EntityPlayer player, ItemStack forStack, int amount)
	{
		
		IBagCap bagCap = CapHelper.getBagCap(bag);
		CachedBag cachedBag = client_cache.get(bagCap.getUUID());
		if (cachedBag == null)
			cachedBag = new CachedBag(player, bag);
		cachedBag.updatePlayer(player);
		cachedBag.updateCachedAmount(forStack, amount);
	}

	public static void modifyCachedAmount(ItemStack bag, ItemStack forStack, int delta)
	{
		Side side = BuildersBag.proxy.getSide();
		Map<String, CachedBag> cache = side == Side.CLIENT ? client_cache : server_cache;
		IBagCap bagCap = CapHelper.getBagCap(bag);

		CachedBag cachedBag = cache.get(bagCap.getUUID());
		if (cachedBag != null)
			cachedBag.modifyCachedAmount(forStack, delta);
	}

	public static void startSimulation(ItemStack bag)
	{
		Side side = BuildersBag.proxy.getSide();
		Map<String, CachedBag> cache = side == Side.CLIENT ? client_cache : server_cache;
		IBagCap bagCap = CapHelper.getBagCap(bag);

		CachedBag cachedBag = cache.get(bagCap.getUUID());
		if (cachedBag != null)
			cachedBag.startSimulation();
		else
		{
			CachedBag newBag = new CachedBag(null, bag);
			cache.put(bagCap.getUUID(), newBag);
			newBag.startSimulation();
		}

	}

	public static void stopSimulation(ItemStack bag)
	{
		Side side = BuildersBag.proxy.getSide();
		Map<String, CachedBag> cache = side == Side.CLIENT ? client_cache : server_cache;
		IBagCap bagCap = CapHelper.getBagCap(bag);

		CachedBag cachedBag = cache.get(bagCap.getUUID());
		if (cachedBag != null)
			cachedBag.stopSimulating();
	}

	public static int getCachedAmount(ItemStack bag, EntityPlayer player, ItemStack toCheck, int preferredAmount)
	{
		Map<String, CachedBag> cache = player.world.isRemote ? client_cache : server_cache;
		IBagCap bagCap = CapHelper.getBagCap(bag);
		
		CachedBag cachedBag = cache.get(bagCap.getUUID());
		if (cachedBag == null)
		{
			CachedBag newBag = new CachedBag(player, bag);
			newBag.requestCacheUpdate(toCheck, preferredAmount);
			cache.put(bagCap.getUUID(), newBag);
			return 0;
		}

		cachedBag.updatePlayer(player);
		
		return cachedBag.getCachedAmount(toCheck, preferredAmount);
	}

	public static void sendBagModificationToClient(ItemStack bag, ItemStack forStack, int delta, EntityPlayer player)
	{
		Tuple<Boolean, Integer> slot = InventoryHelper.getSlotForStackWithBaubles(player, bag);
		BuildersBag.network.sendTo(new ModifyCacheClient(slot.getSecond(), slot.getFirst(), forStack, delta), (EntityPlayerMP) player);
	}
	
	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event)
	{
		World world = event.world;
		Map<String, CachedBag> cache = world.isRemote ? client_cache : server_cache;

		for(CachedBag entry : cache.values())
		{
			entry.removeOldCaches();
		}
	}
}
