package tschipp.buildersbag.common.helper;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.config.BuildersBagConfig;
import tschipp.buildersbag.common.config.Configs;
import tschipp.buildersbag.common.item.BuildersBagItem;

public class InventoryHelper
{
	private static final Map<String, Integer> recursion_depth = new HashMap<String, Integer>();

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

	public static IBagModule getModule(String name, IBagCap cap)
	{
		for (IBagModule mod : cap.getModules())
		{
			if (mod != null && mod.getName().equals(name))
				return mod;
		}

		return null;
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
			} else if (maximum < base)
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
			} else if (maximum < base)
				maximum = base;
		}

		return maximum;
	}

	public static int getAllAvailableStacksCount(IBagCap bag)
	{
		NonNullList<ItemStack> list = getAllAvailableStacks(bag);

		int count = 0;

		for (ItemStack s : list)
			count += s.getCount();

		return count;
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

	private static boolean incrementRecursionDepth(EntityPlayer player)
	{
		Integer i = recursion_depth.get(player.getUniqueID().toString());
		if (i == null)
			i = new Integer(0);

		i++;
		
		recursion_depth.put(player.getUniqueID().toString(), i);
		
		if (i > BuildersBagConfig.Settings.maximumRecursionDepth)
		{
			return false;
		}

		return true;
	}

	public static void resetRecursionDepth(EntityPlayer player)
	{
		recursion_depth.put(player.getUniqueID().toString(), 0);
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
					if (incrementRecursionDepth(player))
					{
						ItemStack provided = module.createStack(stack, bag, player);
						if (ItemStack.areItemsEqual(stack, provided))
						{
							resetRecursionDepth(player);
							return provided;
						}
					}
					else
						return ItemStack.EMPTY;
				}

			}
		}

		return ItemStack.EMPTY;
	}

	/*
	 * Returns true if stack can be provided.
	 */
	public static boolean simulateProvideStack(ItemStack stack, ItemStack bag, EntityPlayer player, @Nullable IBagModule exclude)
	{
		return !getOrProvideStack(stack, CapHelper.getBagCap(bag.copy()), new FakePlayer((WorldServer) player.world, player.getGameProfile()), exclude).isEmpty();
	}

	/*
	 * Tries to provide the given amount of stacks. If it can't, it will give
	 * you those that it managed to make.
	 */
	public static NonNullList<ItemStack> getOrProvideStackWithCount(ItemStack stack, int count, IBagCap bag, EntityPlayer player, @Nullable IBagModule exclude)
	{
		NonNullList<ItemStack> provided = NonNullList.create();

		for (int i = 0; i < count; i++)
		{
			ItemStack s = getOrProvideStack(stack, bag, player, exclude);
			resetRecursionDepth(player);
			if (s.isEmpty())
				break;

			provided.add(s);
		}

		return provided;
	}

	public static NonNullList<ItemStack> getOrProvideStackWithCountDominating(int count, IBagCap bag, EntityPlayer player)
	{
		NonNullList<ItemStack> provided = NonNullList.create();

		IBagModule dominatingModule = null;

		for (IBagModule module : bag.getModules())
		{
			if (module.isEnabled() && module.isDominating())
			{
				dominatingModule = module;
				break;
			}
		}

		if (dominatingModule != null)
		{
			for (int i = 0; i < count; i++)
			{
				ItemStack s = InventoryHelper.getOrProvideStack(dominatingModule.getBlock(bag), bag, player, null);
				if (s.isEmpty())
					break;

				provided.add(s);
			}
		}

		return provided;
	}

	public static NonNullList<ItemStack> simulateProvideStackWithCount(ItemStack stack, int count, ItemStack bag, EntityPlayer player, @Nullable IBagModule exclude)
	{
		return getOrProvideStackWithCount(stack, count, CapHelper.getBagCap(bag.copy()), new FakePlayer((WorldServer) player.world, player.getGameProfile()), exclude);
	}

	public static void addStack(ItemStack stack, IBagCap cap, EntityPlayer player)
	{
		if (player.isCreative())
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

	public static void addOrDropStack(ItemStack stack, IBagCap cap, EntityPlayer player)
	{
		if (player.isCreative())
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

	public static void addStackToPlayerInvOrDrop(ItemStack stack, EntityPlayer player)
	{
		if (player.isCreative())
			return;

		if (!player.world.isRemote)
		{
			EntityItem eItem = new EntityItem(player.world, player.posX, player.posY, player.posZ, stack);
			player.world.spawnEntity(eItem);
		}
	}

	public static NonNullList<ItemStack> getBagsInInventory(EntityPlayer player)
	{
		NonNullList<ItemStack> list = NonNullList.create();

		for (ItemStack s : player.inventory.mainInventory)
		{
			if (s.getItem() instanceof BuildersBagItem)
			{
				list.add(s);
			}
		}

		for (ItemStack s : player.inventory.offHandInventory)
		{
			if (s.getItem() instanceof BuildersBagItem)
			{
				list.add(s);
			}
		}

		return list;
	}

}
