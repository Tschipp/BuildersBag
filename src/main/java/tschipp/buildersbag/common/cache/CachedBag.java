package tschipp.buildersbag.common.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.ItemContainer;
import tschipp.buildersbag.api.Tuple;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.network.server.RequestCacheUpdateServer;

public class CachedBag
{
	private PlayerEntity player;
	private ItemStack bag;
	private IBagCap bagCap;
	private Map<ItemContainer, CachedAmount> cachedStacks = new HashMap<ItemContainer, CachedAmount>();
	private boolean isSimulating = false;
	private Map<ItemContainer, CachedAmount> cacheCopy = new HashMap<ItemContainer, CachedAmount>();
	private Set<ItemContainer> pendingRequests = new HashSet<ItemContainer>();
	private Map<ItemContainer, CachedAmount> dirtyClientCache = new HashMap<ItemContainer, CachedAmount>();

	public CachedBag(PlayerEntity player, ItemStack bag)
	{
		this.bagCap = CapHelper.getBagCap(bag);
		this.bag = bag;
		this.player = player;
	}

	public void clearCache()
	{
		cachedStacks.clear();
		cacheCopy.clear();
		pendingRequests.clear();
		dirtyClientCache.clear();
	}

	public void updatePlayer(PlayerEntity player)
	{
		this.player = player;
	}

	public boolean hasItemCached(ItemStack forStack)
	{
		return cachedStacks.containsKey(ItemContainer.forStack(forStack));
	}

	public int getCachedAmount(ItemStack forStack, int preferredAmount)
	{
		ItemContainer ic = ItemContainer.forStack(forStack);
		CachedAmount amount = cachedStacks.get(ic);
		if (amount == null)
		{
			if (dirtyClientCache.containsKey(ic))
				return dirtyClientCache.get(ic).value;
			
			requestCacheUpdate(forStack, preferredAmount);
			
			CachedAmount c = new CachedAmount();
			c.value = 100;

			dirtyClientCache.put(ic, c);
			return 100;
		}

		if (amount.value < preferredAmount)
			requestCacheUpdate(forStack, preferredAmount);
		
		return amount.value;
	}

	public void modifyCachedAmount(ItemStack forStack, int delta)
	{
		CachedAmount amount = cachedStacks.get(ItemContainer.forStack(forStack));
		if (amount == null)
			return;

		amount.value += delta;

		if (amount.value <= 0)
		{
			amount.value = 0;
		}
	}

	public void requestCacheUpdate(ItemStack forStack, int preferredAmount)
	{
		if (!pendingRequests.contains(ItemContainer.forStack(forStack)))
		{
			pendingRequests.add(ItemContainer.forStack(forStack));

			if (player.level.isClientSide)
			{
				Tuple<Boolean, Integer> slot = InventoryHelper.getSlotForStackWithBaubles(player, bag);
				BuildersBag.network.sendToServer(new RequestCacheUpdateServer(slot.getSecond(), slot.getFirst(), forStack, preferredAmount));
				return;
			}
			else
			{
				BagCache.updateCachedBagStack(bag, player, forStack, preferredAmount);
			}
		}
	}

	public void updateCachedAmount(ItemStack stack, int amount)
	{

		ItemContainer cont = ItemContainer.forStack(stack);

		pendingRequests.remove(cont);
		dirtyClientCache.remove(cont);

		CachedAmount cache = cachedStacks.get(cont);


		if (cache == null)
			cache = new CachedAmount();

		cache.value = amount;

		cache.timestamp = System.currentTimeMillis();
		cachedStacks.put(cont, cache);
		
		
		BagHelper.clearTreeBlacklist();
	}

	public boolean isSimulating()
	{
		return isSimulating;
	}

	public void startSimulation()
	{
		if (isSimulating)
			BuildersBag.LOGGER.warn("Already simulating!");
		this.isSimulating = true;

		cacheCopy = Maps.newHashMap();
		for (Entry<ItemContainer, CachedAmount> s : cachedStacks.entrySet())
		{
			cacheCopy.put(s.getKey(), s.getValue().copy());
		}
	}

	public void stopSimulating()
	{
		if (!isSimulating)
			BuildersBag.LOGGER.warn("Not simulating!");

		this.isSimulating = false;

		cachedStacks = cacheCopy;
	}

	public void removeOldCaches()
	{
		long time = System.currentTimeMillis();
		
		List<ItemContainer> toRemove = new ArrayList<ItemContainer>();
		for(Entry<ItemContainer, CachedAmount> entry : cachedStacks.entrySet())
		{
			if(time - entry.getValue().timestamp >= 300000) //Refresh cache after 5 minutes
			{
				toRemove.add(entry.getKey());
			}
		}
		
		toRemove.forEach(cachedStacks::remove);
	}
	
	private static class CachedAmount
	{
		private int value;
		private long timestamp;

		public CachedAmount copy()
		{
			CachedAmount c = new CachedAmount();
			c.value = value;
			c.timestamp = timestamp;

			return c;
		}
	}

}
