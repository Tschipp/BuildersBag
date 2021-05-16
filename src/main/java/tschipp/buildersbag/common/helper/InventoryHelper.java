package tschipp.buildersbag.common.helper;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.lazy.baubles.api.cap.IBaublesItemHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.IBlockSource;
import tschipp.buildersbag.api.datastructures.Tuple;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.compat.baubles.BaubleHelper;
import tschipp.buildersbag.compat.blocksourceadapter.BlockSourceAdapterHandler;

public class InventoryHelper
{
	public static final int TOP_OFFSET = 15;
	public static final int LEFT_OFFSET = 7;
	public static final int INV_OFFSET = 14;
	public static final int HOTBAR_OFFSET = 4;
	public static final int BOTTOM_OFFSET = 7;
	public static final int RIGHT_OFFSET = 7;

	public static int getSlotWidth(int slotAmount)
	{
		return slotAmount * 18 + LEFT_OFFSET + RIGHT_OFFSET;
	}

	public static int getBagRows(int slotAmount)
	{
		return (int) Math.ceil(slotAmount / 9.0);
	}

	public static int getLastRowExtra(int slotAmount)
	{
		return slotAmount % 9;
	}

	public static int getMaxModules(int slotAmount)
	{
		return getTotalHeight(slotAmount) / 33;
	}

	public static int getTotalHeight(int slotAmount)
	{
		return TOP_OFFSET + (getBagRows(slotAmount) + 4) * 18 + HOTBAR_OFFSET + INV_OFFSET + BOTTOM_OFFSET;
	}

	public static int getTotalWidth()
	{
		return 176;
	}

	public static int getBagExtraRight(IBagCap cap)
	{
		int maxModules = getMaxModules(cap.getBlockInventory().getSlots());

		int maximum = 0;

		for (int i = 0; i < maxModules && i < cap.getModules().length; i++)
		{
			IBagModule module = cap.getModules()[i];
			ItemStackHandler handler = module.getInventory();
			int base = 18 + 14;
			if (handler != null)
			{
				int slotExtra = handler.getSlots() * 18 + 14;
				if (maximum < slotExtra + base)
					maximum = slotExtra + base;
			}
			else if (maximum < base)
				maximum = base;
		}

		return maximum;
	}

	public static int getBagExtraLeft(IBagCap cap)
	{
		int maxModules = getMaxModules(cap.getBlockInventory().getSlots());
		int maximum = 0;

		for (int i = maxModules; i < cap.getModules().length; i++)
		{
			IBagModule module = cap.getModules()[i];
			ItemStackHandler handler = module.getInventory();
			int base = 18 + 14;
			if (handler != null)
			{
				int slotExtra = handler.getSlots() * 18 + 14;
				if (maximum < slotExtra + base)
					maximum = slotExtra + base;
			}
			else if (maximum < base)
				maximum = base;
		}

		return maximum;
	}

	public static NonNullList<ItemStack> getInventoryStacks(IBagCap bag, PlayerEntity player)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.addAll(getStacks(bag.getBlockInventory()));

		for (ItemStack available : list)
		{
			if (available.getItem() instanceof IBlockSource)
			{
				list.addAll(((IBlockSource) available.getItem()).getCreateableBlocks(available, player));
			}
			else if (BlockSourceAdapterHandler.hasAdapter(available))
			{
				list.addAll(BlockSourceAdapterHandler.getCreateableBlocks(available, player));
			}
		}

		return list;
	}

	public static NonNullList<ItemStack> getStacks(IItemHandler handler)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		for (int i = 0; i < handler.getSlots(); i++)
		{
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty())
				list.add(stack);
		}
		return list;
	}

	public static NonNullList<Triple<Integer, Boolean, ItemStack>> getBagsInInventory(PlayerEntity player)
	{
		NonNullList<Triple<Integer, Boolean, ItemStack>> list = NonNullList.create();

		for (int i = 0; i < player.inventory.items.size(); i++)
		{
			ItemStack s = player.inventory.items.get(i);
			if (s.getItem() instanceof BuildersBagItem)
			{
				list.add(new ImmutableTriple(i, false, s));
			}
		}

		for (int i = 0; i < player.inventory.offhand.size(); i++)
		{
			ItemStack s = player.inventory.offhand.get(i);
			if (s.getItem() instanceof BuildersBagItem)
			{
				list.add(new ImmutableTriple(i + 40, false, s));
			}
		}

		if (ModList.get().isLoaded("baubles"))
		{
			IBaublesItemHandler baubles = BaubleHelper.getBaubles(player);
			for (int i = 0; i < baubles.getSlots(); i++)
			{
				ItemStack s = baubles.getStackInSlot(i);
				if (s.getItem() instanceof BuildersBagItem)
					list.add(new ImmutableTriple(i, true, s));
			}
		}

		return list;
	}

	public static NonNullList<ItemStack> removeMatchingStacksWithSizeOne(ItemStack stack, int count, NonNullList<ItemStack> fromStacks)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		stack = stack.copy();
		stack.setCount(1);

		for (ItemStack s : fromStacks)
		{
			if (count <= 0)
				break;

			if (ItemStack.isSame(s, stack))
			{
				int reduce = Math.min(s.getCount(), count);
				list.addAll(ItemHelper.listOf(stack, reduce));
				count -= reduce;
				s.shrink(reduce);
			}
		}

		return list;
	}

	public static NonNullList<ItemStack> getMatchingStacksWithSizeOne(ItemStack stack, NonNullList<ItemStack> fromStacks)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		for (ItemStack s : fromStacks)
		{
			if (ItemStack.isSame(s, stack))
			{
				s = s.copy();

				int count = s.getCount();
				for (int i = 0; i < count; i++)
				{
					list.add(s.split(1));
				}
			}
		}
		return list;
	}

	public static NonNullList<ItemStack> getMatchingStacksWithSizeOne(ItemStack stack, int maxCount, NonNullList<ItemStack> fromStacks)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		int added = 0;
		for (ItemStack s : fromStacks)
		{
			if (ItemStack.isSame(s, stack))
			{
				s = s.copy();

				int count = s.getCount();
				for (int i = 0; i < count; i++)
				{
					if (added < maxCount)
					{
						list.add(s.split(1));
						added++;
					}
					else
						return list;
				}
			}
		}
		return list;
	}

	public static int getSlotForStack(PlayerEntity player, ItemStack stack)
	{
		for (int i = 0; i < player.inventory.getContainerSize(); i++)
		{
			if (player.inventory.getItem(i) == stack)
				return i;
		}

		return -1;
	}

	public static ItemStack getStackInSlot(PlayerEntity player, int slot, boolean isBauble)
	{
		ItemStack stack = ItemStack.EMPTY;

		if (isBauble && ModList.get().isLoaded("baubles"))
		{
			BaubleHelper.getBauble(player, slot);
		}
		else
			stack = player.inventory.getItem(slot);

		return stack;

	}

	public static Tuple<Boolean, Integer> getSlotForStackWithBaubles(PlayerEntity player, ItemStack stack)
	{
		for (int i = 0; i < player.inventory.getContainerSize(); i++)
		{
			ItemStack inSlot = player.inventory.getItem(i);

			if (inSlot.getItem() instanceof BuildersBagItem && stack.getItem() instanceof BuildersBagItem)
			{
				if (CapHelper.areCapsEqual(CapHelper.getBagCap(stack), CapHelper.getBagCap(inSlot)))
					return new Tuple(false, i);
			}
			else if (ItemStack.tagMatches(inSlot, stack))
				return new Tuple(false, i);
		}

		if (ModList.get().isLoaded("baubles"))
		{
			ItemStack inSlot = BaubleHelper.getBauble(player, 3);
			if (inSlot.getItem() instanceof BuildersBagItem && stack.getItem() instanceof BuildersBagItem)
			{
				if (CapHelper.areCapsEqual(CapHelper.getBagCap(stack), CapHelper.getBagCap(inSlot)))
					return new Tuple(true, 3);
			}
			else if (ItemStack.tagMatches(inSlot, stack))
				return new Tuple(true, 3);
		}

		return new Tuple(false, -1);
	}

}
