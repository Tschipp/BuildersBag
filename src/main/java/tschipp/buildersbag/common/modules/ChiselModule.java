package tschipp.buildersbag.common.modules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import team.chisel.api.IChiselItem;
import team.chisel.api.carving.CarvingUtils;
import team.chisel.api.carving.ICarvingGroup;
import team.chisel.api.carving.ICarvingVariation;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.helper.ItemHelper;
import tschipp.buildersbag.common.inventory.ItemHandlerWithPredicate;

public class ChiselModule extends AbstractBagModule
{
	private ItemHandlerWithPredicate handler = new ItemHandlerWithPredicate(1, (stack, slot) -> stack.getItem() instanceof IChiselItem);
	private static final ItemStack DISPLAY = new ItemStack(Item.getByNameOrId("chisel:chisel_iron"));

	public ChiselModule()
	{
		super("buildersbag:chisel");
	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag, EntityPlayer player)
	{
		NonNullList<ItemStack> providedSacks = BagHelper.getAllAvailableStacksExcept(bag, player, this);
		NonNullList<ItemStack> list = NonNullList.create();

		ItemStack chisel = handler.getStackInSlot(0);

		if (chisel.isEmpty() || !validTinkersChisel(chisel))
			return list;

		Set<ICarvingGroup> groups = new HashSet<ICarvingGroup>();

		for (ItemStack stack : providedSacks)
		{
			if (!stack.isEmpty())
			{
				ICarvingGroup group = CarvingUtils.getChiselRegistry().getGroup(stack);
				if (group != null)
					groups.add(group);
			}
		}

		for (ICarvingGroup group : groups)
		{
			for (ICarvingVariation variation : group)
			{
				list.add(variation.getStack());
			}
		}

		return list;
	}

	@Override
	public ItemStackHandler getInventory()
	{
		return handler;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound tag = super.serializeNBT();
		tag.setTag("Inventory", handler.serializeNBT());
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		super.deserializeNBT(nbt);
		handler.deserializeNBT(nbt.getCompoundTag("Inventory"));
	}

	@Override
	public boolean doesntUseOwnInventory()
	{
		return false;
	}

	@Override
	public ItemStack getDisplayItem()
	{
		return DISPLAY;
	}

	@Override
	public NonNullList<ItemStack> createStackWithCount(ItemStack stack, int count, IBagCap bag, EntityPlayer player)
	{
		NonNullList list = NonNullList.create();

		ICarvingGroup group = CarvingUtils.getChiselRegistry().getGroup(stack);
		if (group == null)
			return list;

		ItemStack chisel = handler.getStackInSlot(0);

		if (chisel.isEmpty() || !validTinkersChisel(chisel))
			return list;

		NonNullList<ItemStack> availableBlocks = InventoryHelper.getInventoryStacks(bag, player);

		NonNullList<ItemStack> providedVariants = NonNullList.create();

		for (ICarvingVariation variant : group)
		{
			while (providedVariants.size() < count)
			{
				ItemStack available = ItemStack.EMPTY;
				if (!(available = ItemHelper.containsStack(variant.getStack(), availableBlocks)).isEmpty())
				{
					available.shrink(1);
					providedVariants.add(available);
				}
				else
					break;
			}
		}

		for (ICarvingVariation variant : group)
		{
			if (providedVariants.size() < count)
			{
				NonNullList<ItemStack> provided = BagHelper.getOrProvideStackWithCount(variant.getStack(), count - providedVariants.size(), bag, player, this);
				providedVariants.addAll(provided);
			}
		}

		if (!providedVariants.isEmpty())
		{
			if (!player.world.isRemote)
			{
				for (int i = 0; i < providedVariants.size(); i++)
				{
					if (!validTinkersChisel(chisel) || chisel.attemptDamageItem(1, new Random(), (EntityPlayerMP) player))
					{
						list.add(stack.copy());

						if (!chisel.getItem().getRegistryName().toString().equals("tcomplement:chisel"))
							chisel.shrink(1);

						for (int j = i; j < providedVariants.size(); j++)
						{
							BagHelper.addStack(providedVariants.get(j), bag, player);
						}

						break;
					}
					else
						list.add(stack.copy());
				}
			}

			return list;
		}

		return NonNullList.create();
	}

	@Override
	public NonNullList<ItemStack> getCompactedStacks(NonNullList<ItemStack> toCompact, EntityPlayer player)
	{
		if (!isEnabled())
			return toCompact;

		ItemStack chisel = handler.getStackInSlot(0);
		if (chisel.isEmpty() || !validTinkersChisel(chisel))
			return toCompact;

		NonNullList<ItemStack> compacted = NonNullList.create();

		Map<ICarvingGroup, Map<ICarvingVariation, Integer>> variations = new HashMap<ICarvingGroup, Map<ICarvingVariation, Integer>>();

		for (ItemStack stack : toCompact)
		{
			ICarvingVariation vari = CarvingUtils.getChiselRegistry().getVariation(stack);
			ICarvingGroup group = CarvingUtils.getChiselRegistry().getGroup(stack);
			if (vari == null)
			{
				compacted.add(stack);
				continue;
			}

			if (variations.get(group) != null)
			{
				if (variations.get(group).get(vari) != null)
				{
					variations.get(group).put(vari, variations.get(group).get(vari) + stack.getCount());
				}
				else
				{
					variations.get(group).put(vari, stack.getCount());
				}
			}
			else
			{
				Map<ICarvingVariation, Integer> map = new HashMap<ICarvingVariation, Integer>();
				map.put(vari, stack.getCount());
				variations.put(group, map);
			}
		}

		for (Entry<ICarvingGroup, Map<ICarvingVariation, Integer>> entry : variations.entrySet())
		{
			Map<ICarvingVariation, Integer> map = entry.getValue();

			ICarvingVariation maxVari = null;
			int maxCount = 0;
			int totalCount = 0;

			for (Entry<ICarvingVariation, Integer> mapEntry : map.entrySet())
			{
				if (mapEntry.getValue() > maxCount)
				{
					maxCount = mapEntry.getValue();
					maxVari = mapEntry.getKey();
				}

				totalCount += mapEntry.getValue();
			}

			int usedBlocks = 0;
			boolean chiselEmpty = false;

			if (maxVari != null && totalCount > 0)
			{
				int stackCount = totalCount / 64;
				for (int i = 0; i < stackCount; i++)
				{
					if (!chisel.isEmpty() && validTinkersChisel(chisel))
					{
						ItemStack s = maxVari.getStack();
						s.setCount(64);
						compacted.add(s);

						if (!player.world.isRemote)
						{
							if (chisel.attemptDamageItem(1, new Random(), (EntityPlayerMP) player))
								chisel.shrink(1);
						}

						usedBlocks += 64;
					}
					else
						chiselEmpty = true;
				}

				if (!chisel.isEmpty() && validTinkersChisel(chisel))
				{
					ItemStack s = maxVari.getStack();
					s.setCount(totalCount % 64);

					if (!s.isEmpty())
					{
						compacted.add(s);
						usedBlocks += s.getCount();

						if (!player.world.isRemote)
						{
							if (chisel.attemptDamageItem(1, new Random(), (EntityPlayerMP) player))
								chisel.shrink(1);
						}
					}
				}
				else
					chiselEmpty = true;

				if (chiselEmpty) // Chisel was destroyed during the process, so we need to readd the other materials.
				{
					for (Entry<ICarvingVariation, Integer> mapEntry : map.entrySet())
					{
						int amount = mapEntry.getValue();
						if (amount <= usedBlocks)
						{
							mapEntry.setValue(0);
							usedBlocks -= amount;
						}
						else
						{
							mapEntry.setValue(amount - usedBlocks);
							amount -= usedBlocks;
							usedBlocks = 0;

							int leftoverStackCountCount = amount / 64;
							for (int i = 0; i < leftoverStackCountCount; i++)
							{
								ItemStack s = mapEntry.getKey().getStack();
								s.setCount(64);
								compacted.add(s);
							}

							ItemStack s = mapEntry.getKey().getStack();
							s.setCount(amount % 64);
							if (!s.isEmpty())
								compacted.add(s);
						}
					}
				}
			}
		}

		return compacted;
	}

	public static boolean validTinkersChisel(ItemStack stack)
	{
		if (stack.getItem().getRegistryName().toString().equals("tcomplement:chisel"))
		{
			return stack.getItemDamage() != stack.getMaxDamage();
		}
		else
			return true;
	}

}
