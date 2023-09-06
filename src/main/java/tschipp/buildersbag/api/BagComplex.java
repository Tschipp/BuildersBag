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
		if (!valid)
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
		for (IBagModule bm : bagCap.getModules())
		{
			if (bm.getType() == type)
				return bm;
		}
		return null;
	}

	public int take(Item item, int amount, PlayerEntity player, boolean reAddExtra)
	{
		amount = Math.max(amount, 0);

		BagInventory inv = getInventory();
		int available = inv.removePhysical(item, amount);

		if (available < amount)
		{
			available += inv.tryCreating(item, amount - available, player);
		}

		boolean anyLeft = inv.has(item); // Only notify if there's really no
											// items of this type left
		if (!anyLeft && available - amount <= 0)
			notifyItemRemoved(item);

		if (reAddExtra && available - amount > 0)
		{
			add(item, available - amount, player);
			available = amount;
		}

		if (!inv.hasPhysical(item, 1) && inv.hasCraftable(item))
			checkPotentialCycles(item);

		runInventoryTests();

		return available;
	}

	public int take(Item item, int amount, PlayerEntity player)
	{
		return take(item, amount, player, true);
	}

	public void add(Item item, int amount, PlayerEntity player)
	{
		BagInventory inv = getInventory();
		boolean notify = !inv.has(item); // Only notify if this item didn't
											// exist in the bag before

		inv.addPhysical(item, amount, BagHelper.handleExcess(player));

		if (notify)
			notifyItemAdded(item);

		runInventoryTests();
	}

	public void notifyItemAdded(Item item)
	{
		if (BuildersBag.TESTING)
			BuildersBag.LOGGER.info("Notifying " + item + " as added");
		for (IBagModule bm : bagCap.getModules())
		{
			bm.getType().getListener().notifyAdded(item, this);
			runInventoryTests();
		}
	}

	public void notifyItemAddedWithCheck(Item item)
	{
		if (!pendingAdditions.contains(item))
		{
			pendingAdditions.add(item);
			notifyItemAdded(item);
			pendingAdditions.remove(item);
		}
	}

	public void notifyItemRemoved(Item item)
	{
		if (getInventory().hasPhysical(item, 1))
			return;

		if (BuildersBag.TESTING)
			BuildersBag.LOGGER.info("Notifying " + item + " as removed");
		getInventory().pendingCreations.remove(item);
		for (IBagModule bm : bagCap.getModules())
		{
			bm.getType().getListener().notifyRemoved(item, this);
		}
		runInventoryTests();
	}

	public void checkPotentialCycles(Item item)
	{
		// BuildersBag.LOGGER.info("Notifying " + item + " as added");
		for (IBagModule bm : bagCap.getModules())
		{
			CreateableItemsManager cr = bm.getCreateableItemsManager();
			if (cr != null)
				cr.removePotentialCycles(this, item);
			runInventoryTests();
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

	public void runInventoryTests()
	{
		if (BuildersBag.TESTING)
		{
			Testing.assertInventory(bagCap);
		}
	}

	public void runCreateableTests()
	{
		if (BuildersBag.TESTING)
		{
			Testing.assertCreatable(bagCap);
		}
	}
}
