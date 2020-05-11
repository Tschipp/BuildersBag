package tschipp.buildersbag.common.inventory;

import static tschipp.buildersbag.common.helper.InventoryHelper.HOTBAR_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.INV_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.LEFT_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.TOP_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.getBagRows;
import static tschipp.buildersbag.common.helper.InventoryHelper.getMaxModules;
import static tschipp.buildersbag.common.helper.InventoryHelper.getTotalWidth;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.network.SyncBagCapClient;
import tschipp.buildersbag.network.SyncBagCapInventoryClient;

public class ContainerBag extends Container
{
	public ItemStack bag;
	public IBagCap bagCap;
	private EntityPlayer player;
	public EnumHand hand;
	private ItemStackHandler inv;

	public boolean isBauble = false;
	public int baubleSlot;

	private List<Slot> inventoryBagSlots = new ArrayList<Slot>();

	public int invSize;
	public String name;
	public ImmutableMap<IBagModule, Triple<Integer, Integer, Boolean>> modules;

	public int leftOffset = 0;

	public ContainerBag(EntityPlayer player, ItemStack bag, EnumHand hand)
	{
		this(player, bag);
		this.hand = hand;
	}

	public ContainerBag(EntityPlayer player, ItemStack bag, int baubleSlot)
	{
		this(player, bag);
		this.baubleSlot = baubleSlot;
		this.isBauble = true;
		
		if (!player.world.isRemote)
		{
			if (isBauble)
				BuildersBag.network.sendTo(new SyncBagCapInventoryClient(bagCap, baubleSlot, true), (EntityPlayerMP) player);
			else
				BuildersBag.network.sendTo(new SyncBagCapClient(bagCap, hand), (EntityPlayerMP) player);
		}
	}

	private ContainerBag(EntityPlayer player, ItemStack bag)
	{
		this.player = player;
		this.bag = bag;
		this.bagCap = CapHelper.getBagCap(bag);
		this.inv = bagCap.getBlockInventory();
		this.invSize = inv.getSlots();

		this.leftOffset = Math.max(InventoryHelper.getBagExtraLeft(CapHelper.getBagCap(bag)), InventoryHelper.getBagExtraRight(CapHelper.getBagCap(bag)));

		if (bag.hasDisplayName())
			name = bag.getDisplayName();
		else
			name = I18n.translateToLocal("buildersbag.name");

		setupInventories();
	}

	private void setupInventories()
	{
		setupPlayerInventory();
		setupBagInventory();
		setupModuleInventories();
	}

	private void setupBagInventory()
	{
		int slotIndex = 0;
		int x = 0;
		int y = 0;

		x += LEFT_OFFSET + 1;
		y += TOP_OFFSET + 1;

		for (int i = 0; i < getBagRows(invSize); i++)
		{
			for (int j = 0; j < 9; j++)
			{
				if (slotIndex < invSize)
					inventoryBagSlots.add(addSlotToContainer(new SlotItemHandler(inv, slotIndex++, x + j * 18 + leftOffset, y + i * 18)));
				else
					break;
			}
		}

		addSlotToContainer(new SelectedBlockSlot(bagCap.getSelectedInventory(), 0, 80 + leftOffset, -24));

	}

	private void setupPlayerInventory()
	{
		int x = 0;
		int y = 0;

		int slotIndex = 9;

		x += LEFT_OFFSET + 1;
		y += TOP_OFFSET + 1 + getBagRows(invSize) * 18 + INV_OFFSET;

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(player.inventory, slotIndex++, x + j * 18 + leftOffset, y + i * 18));
			}
		}

		slotIndex = 0;

		y += HOTBAR_OFFSET + 3 * 18;

		for (int j = 0; j < 9; j++)
		{
			addSlotToContainer(new Slot(player.inventory, slotIndex++, x + j * 18 + leftOffset, y));
		}
	}

	private void setupModuleInventories()
	{
		Builder<IBagModule, Triple<Integer, Integer, Boolean>> builder = ImmutableMap.builder();

		int moduleCount = getMaxModules(invSize);
		int x = getTotalWidth() + 40;
		int y = 8;

		int rightModuleCount = 0;

		int processedModules = 0;

		for (int i = 0; i < bagCap.getModules().length; i++)
		{
			if (rightModuleCount == moduleCount)
				break;

			processedModules++;

			IBagModule module = bagCap.getModules()[i];

			builder.put(module, new ImmutableTriple<Integer, Integer, Boolean>(x - 40 + leftOffset, y - 8, true));

			if (module.getInventory() != null)
			{
				int slotIndex = 0;
				ItemStackHandler handler = module.getInventory();
				for (int j = 0; j < handler.getSlots(); j++)
				{
					addSlotToContainer(new ToggleableSlot(handler, slotIndex++, x + j * 18 + leftOffset, y).setEnabled(module.isExpanded()));
				}
			}

			x = getTotalWidth() + 40;
			y += 34;
			rightModuleCount++;

		}

		y = 8;
		x = -41;

		for (int i = processedModules; i < bagCap.getModules().length; i++)
		{
			IBagModule module = bagCap.getModules()[i];

			builder.put(module, new ImmutableTriple<Integer, Integer, Boolean>(x + 8 + leftOffset, y - 8, false));

			if (module.getInventory() != null)
			{
				int slotIndex = 0;
				ItemStackHandler handler = module.getInventory();
				for (int j = 0; j < handler.getSlots(); j++)
				{
					addSlotToContainer(new ToggleableSlot(handler, slotIndex++, x - j * 18 - 16 + leftOffset, y).setEnabled(module.isExpanded()));
				}
			} else

				x = -41;
			y += 34;
		}

		this.modules = builder.build();
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		int rows = InventoryHelper.getBagRows(this.invSize);

		ItemStack stack = slot.getStack();

		if (index < 9 * 4)
		{
			// Item is in inventory
			if (!this.mergeItemStack(stack, 36, 36 + invSize, false))
				if (!this.mergeItemStack(stack, 36 + invSize + 1, this.inventorySlots.size(), false))
					return ItemStack.EMPTY;

		} else if (index >= 9 * 4 && index < 9 * (4 + rows))
		{
			// Item is in bag inventory
			if (!this.mergeItemStack(stack, 0, 36, false))
				return ItemStack.EMPTY;
		} else
		{
			// Item is somewhere else in bag
			if (!this.mergeItemStack(stack, 0, 36, false))
				return ItemStack.EMPTY;
		}

		return itemstack;
	}

	protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection)
	{
		boolean flag = false;
		int i = startIndex;

		if (reverseDirection)
		{
			i = endIndex - 1;
		}

		if (stack.isStackable())
		{
			while (!stack.isEmpty())
			{
				if (reverseDirection)
				{
					if (i < startIndex)
					{
						break;
					}
				} else if (i >= endIndex)
				{
					break;
				}

				Slot slot = this.inventorySlots.get(i);
				ItemStack itemstack = slot.getStack();

				if (!slot.isEnabled())
				{
					if (reverseDirection)
						i--;
					else
						i++;
					continue;
				}

				if (!itemstack.isEmpty() && itemstack.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, itemstack))
				{
					int j = itemstack.getCount() + stack.getCount();
					int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

					if (j <= maxSize)
					{
						stack.setCount(0);
						itemstack.setCount(j);
						slot.onSlotChanged();
						flag = true;
					} else if (itemstack.getCount() < maxSize)
					{
						stack.shrink(maxSize - itemstack.getCount());
						itemstack.setCount(maxSize);
						slot.onSlotChanged();
						flag = true;
					}
				}

				if (reverseDirection)
				{
					--i;
				} else
				{
					++i;
				}
			}
		}

		if (!stack.isEmpty())
		{
			if (reverseDirection)
			{
				i = endIndex - 1;
			} else
			{
				i = startIndex;
			}

			while (true)
			{
				if (reverseDirection)
				{
					if (i < startIndex)
					{
						break;
					}
				} else if (i >= endIndex)
				{
					break;
				}

				Slot slot1 = this.inventorySlots.get(i);

				if (!slot1.isEnabled())
				{
					if (reverseDirection)
						i--;
					else
						i++;
					continue;
				}

				ItemStack itemstack1 = slot1.getStack();

				if (itemstack1.isEmpty() && slot1.isItemValid(stack))
				{
					if (stack.getCount() > slot1.getSlotStackLimit())
					{
						slot1.putStack(stack.splitStack(slot1.getSlotStackLimit()));
					} else
					{
						slot1.putStack(stack.splitStack(stack.getCount()));
					}

					slot1.onSlotChanged();
					flag = true;
					break;
				}

				if (reverseDirection)
				{
					--i;
				} else
				{
					++i;
				}
			}
		}

		return flag;
	}

	@Override
	public boolean canDragIntoSlot(Slot slot)
	{
		return !(slot instanceof SelectedBlockSlot);
	}

	public void updateModule(String name, NBTTagCompound nbt)
	{
		modules.forEach((module, triple) -> {
			if (module.getName().equals(name))
			{
				module.deserializeNBT(nbt);
			}
		});

		update();
	}

	public void update()
	{
		inventorySlots.clear();
		inventoryItemStacks.clear();

		setupInventories();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		if (Loader.isModLoaded("baubles"))
		{
			return isBauble ? BaublesApi.getBaubles(player).getStackInSlot(this.baubleSlot) == bag : player.getHeldItem(hand) == bag;
		}
		return player.getHeldItem(hand) == bag;
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
	{
		ItemStack ret = super.slotClick(slotId, dragType, clickTypeIn, player);

		if (!player.world.isRemote)
		{
			if (isBauble)
				BuildersBag.network.sendTo(new SyncBagCapInventoryClient(bagCap, baubleSlot, true), (EntityPlayerMP) player);
			else
				BuildersBag.network.sendTo(new SyncBagCapClient(bagCap, hand), (EntityPlayerMP) player);
		}

		return ret;
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn)
	{
		super.onContainerClosed(playerIn);

		if (!player.world.isRemote)
		{
			if (isBauble)
				BuildersBag.network.sendTo(new SyncBagCapInventoryClient(bagCap, baubleSlot, true), (EntityPlayerMP) player);
			else
				BuildersBag.network.sendTo(new SyncBagCapClient(bagCap, hand), (EntityPlayerMP) player);
		}
	}

}
