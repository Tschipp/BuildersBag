package tschipp.buildersbag.common.helper;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.caps.IBagCap;
import tschipp.buildersbag.common.item.BuildersBagItem;

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

	public static NonNullList<ItemStack> getAllAvailableStacks(IBagCap bag)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.addAll(getStacks(bag.getBlockInventory()));
		for (IBagModule module : bag.getModules())
		{
			if (module.isEnabled())
				list.addAll(module.getPossibleStacks(bag));
		}
		return list;
	}

	public static NonNullList<ItemStack> getAllAvailableStacksExcept(IBagCap bag, IBagModule exclude)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.addAll(getStacks(bag.getBlockInventory()));
		for (IBagModule module : bag.getModules())
		{
			if (module.isEnabled() && module != exclude)
				list.addAll(module.getPossibleStacks(bag));
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

	public static ItemStack containsStack(ItemStack stack, NonNullList<ItemStack> stacks)
	{
		for (ItemStack s : stacks)
		{
			if (ItemStack.areItemsEqual(stack, s))
				return s;
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack getOrProvideStack(ItemStack stack, IBagCap bag, EntityPlayer player, @Nullable IBagModule exclude)
	{
		ItemStack foundStack = ItemStack.EMPTY;
		if (!(foundStack = containsStack(stack, getStacks(bag.getBlockInventory()))).isEmpty())
		{
			return foundStack.splitStack(1);
		} else
		{
			for (IBagModule module : bag.getModules())
			{
				if (module.isEnabled() && !module.isSupplier() && (exclude == null ? true : exclude != module))
				{
					ItemStack provided = module.createStack(stack, bag, player);
					if (ItemStack.areItemsEqual(stack, provided))
					{
						return provided;
					}
				}
			}
		}

		return ItemStack.EMPTY;
	}

	public static void addStack(ItemStack stack, IBagCap cap, EntityPlayer player)
	{
		if(player.isCreative())
			return;
		
		ItemStackHandler handler = cap.getBlockInventory();
		for (int i = 0; i < handler.getSlots(); i++)
		{
			if (handler.insertItem(i, stack, true) != stack)
			{
				ItemStack rest = handler.insertItem(i, stack, false);
				if (!rest.isEmpty())
					addStack(rest, cap, player);
				return;
			}
		}

		if (!player.addItemStackToInventory(stack))
		{
			player.dropItem(stack, false);
		}
	}
	
	public static NonNullList<ItemStack> getBagsInInventory(EntityPlayer player)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		
		for(ItemStack s : player.inventory.mainInventory)
		{
			if(s.getItem() instanceof BuildersBagItem)
			{
				list.add(s);
			}
		}
		
		for(ItemStack s : player.inventory.offHandInventory)
		{
			if(s.getItem() instanceof BuildersBagItem)
			{
				list.add(s);
			}
		}
		
		return list;
	}

}
