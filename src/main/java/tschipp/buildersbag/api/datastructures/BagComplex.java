package tschipp.buildersbag.api.datastructures;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import tschipp.buildersbag.api.BagModuleType;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.helper.BagHelper;

public class BagComplex
{
	private boolean valid = false;
	private BagInventory inventory;
	private IBagCap bagCap;
	private Map<BagModuleType<? extends IBagModule>, IBagModule> modules;
	
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
		this.inventory = new BagInventory(bagCap.getBlockInventory(), this);
		this.valid = true;
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
		return modules.get(type);
	}
	
	public int take(Item item, int amount, PlayerEntity player)
	{
		BagInventory inv = getInventory();
		int available = inv.removePhysical(item, amount);
		
		boolean notify = inv.has(item); //Only notify if there's really no items of this type left
		if(!notify)
			notifyItemRemoved(item);
		
		return available;
	}
	
	public void add(Item item, int amount, PlayerEntity player)
	{
		BagInventory inv = getInventory();
		boolean notify = inv.has(item); //Only notify if this item didn't exist in the bag before
		
		inv.addPhysical(item, amount, BagHelper.handleExcess(player));
		
		if(notify)
			notifyItemAdded(item);
	}
	
	/*
	 * Implement get() method, which first tries to take from physical inventory, and then falls back to creating new items if possible. 
	 */
	
	
	public void notifyItemAdded(Item item)
	{
		for(BagModuleType<?> type : modules.keySet())
		{
			type.getListener().notifyAdded(item, this);
		}
	}
	
	public void notifyItemAddedWithCheck(Item item)
	{
		if(!getInventory().has(item))
			notifyItemAdded(item);
	}
	
	public void notifyItemRemoved(Item item)
	{
		for(BagModuleType<?> type : modules.keySet())
		{
			type.getListener().notifyRemoved(item, this);
		}
	}
}
