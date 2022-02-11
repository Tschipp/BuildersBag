package tschipp.buildersbag.api;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.helper.BagHelper;

public class BagComplex
{
	private boolean valid = false;
	private BagInventory inventory;
	private IBagCap bagCap;
	private Set<Item> pendingAdditions = new HashSet<>();

	public BagComplex(IBagCap cap)
	{
		this.bagCap = cap;
		this.inventory = new BagInventory(cap.getBlockInventory(), this);
	}
	
	public boolean isValid()
	{
		return valid;
	}
	
	public void checkValidation()
	{
		if(!valid)
			validate();
	}

	public void validate()
	{
		this.valid = true;
		this.inventory = new BagInventory(bagCap.getBlockInventory(), this);
		this.inventory.initCreateable();
	}
	
	public void invalidate()
	{
		this.valid = false;
	}
	
	public BagInventory getInventory()
	{
		checkValidation();
		return inventory;
	}
	
	@Nullable
	public IBagModule getModule(BagModuleType<? extends IBagModule> type)
	{
		for(IBagModule bm : bagCap.getModules())
		{
			if(bm.getType() == type)
				return bm;
		}
		return null;
	}
	
	public int take(Item item, int amount, PlayerEntity player, boolean reAddExtra)
	{
		amount = Math.max(amount, 0);
		
		BagInventory inv = getInventory();
		int available = inv.removePhysical(item, amount);
		
		if(available < amount)
		{
			available += inv.tryCreating(item, amount - available, player);
		}	
		
		boolean anyLeft = inv.has(item); //Only notify if there's really no items of this type left, dosn't work yet
		if(!anyLeft && available-amount <= 0)
			notifyItemRemoved(item);
		
		if(reAddExtra && available - amount > 0)
			add(item, available-amount, player);
		
		runTests();
		
		return available;
	}
	
	
	
	public int take(Item item, int amount, PlayerEntity player)
	{
		return take(item, amount, player, true);
	}
	
	public void add(Item item, int amount, PlayerEntity player)
	{
		BagInventory inv = getInventory();
		boolean notify = inv.has(item); //Only notify if this item didn't exist in the bag before
		
		inv.addPhysical(item, amount, BagHelper.handleExcess(player));
		
		if(notify)
			notifyItemAdded(item);
		
		runTests();
	}
	
	/*
	 * Implement get() method, which first tries to take from physical inventory, and then falls back to creating new items if possible. 
	 */
	
	
	public void notifyItemAdded(Item item)
	{
		for(IBagModule bm : bagCap.getModules())
		{
			bm.getType().getListener().notifyAdded(item, this);
			runTests();
		}
	}
	
	public void notifyItemAddedWithCheck(Item item)
	{
		if(!pendingAdditions.contains(item))
		{
			pendingAdditions.add(item);
			notifyItemAdded(item);
			pendingAdditions.remove(item);
		}
	}
	
	//TODO: this doesn't quite work yet, sometimes when a item was created but is no longer creatable, the
	//creatable is not removed. Also, somehow made charcoal from coal block?
	public void notifyItemRemoved(Item item)
	{
		for(IBagModule bm : bagCap.getModules())
		{
			bm.getType().getListener().notifyRemoved(item, this);
			runTests();
		}
	}
	
	public Set<Item> getCreateableItems()
	{
		BagInventory inv = getInventory();
		return inv.createableItems.values().stream().map(holder -> holder.getItem()).collect(Collectors.toSet());
	}
	
	public Set<Item> getPhysicalItems()
	{
		BagInventory inv = getInventory();
		return inv.inventoryItems.values().stream().map(holder -> holder.getItem()).collect(Collectors.toSet());
	}
	
	public Set<Item> getAllAvailableItems()
	{
		Set<Item> all = new HashSet<>();
		all.addAll(getCreateableItems());
		all.addAll(getPhysicalItems());
		return all;
	}
	
	private void runTests()
	{
		if(BuildersBag.TESTING)
		{
			Testing.assertBagCorrectness(bagCap);
		}
	}
}
