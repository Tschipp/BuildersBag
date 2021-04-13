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
import com.lazy.baubles.api.BaublesAPI;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.BuildersBagRegistry;
import tschipp.buildersbag.common.cache.BagCache;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.compat.baubles.BaubleHelper;
import tschipp.buildersbag.network.client.SyncBagCapClient;
import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;

public class ContainerBag extends Container
{
	public ItemStack bag;
	public IBagCap bagCap;
	private PlayerEntity player;
	public Hand hand;
	private ItemStackHandler inv;

	public boolean isBauble = false;
	public int slot = -1;

	private List<Slot> inventoryBagSlots = new ArrayList<Slot>();
	private Slot selectedBlockSlot;

	public int invSize;
	public ITextComponent name;
	public ImmutableMap<IBagModule, Triple<Integer, Integer, Boolean>> modules;

	public int leftOffset = 0;

	public ContainerBag(int windowID, PlayerEntity player, ItemStack bag, Hand hand)
	{
		this(windowID, player, bag);
		this.hand = hand;
	}

	public ContainerBag(int windowID, PlayerEntity player, ItemStack bag, int baubleSlot)
	{
		this(windowID, player, bag);
		this.slot = baubleSlot;
		this.isBauble = true;
	}

	private ContainerBag(int windowID, PlayerEntity player, ItemStack bag)
	{
		super(BuildersBagRegistry.BAG_CONTAINER_TYPE, windowID);

		this.player = player;
		this.bag = bag;
		this.bagCap = CapHelper.getBagCap(bag);
		this.inv = bagCap.getBlockInventory();
		this.invSize = inv.getSlots();
		this.slot = InventoryHelper.getSlotForStack(player, bag);

		this.leftOffset = Math.max(InventoryHelper.getBagExtraLeft(CapHelper.getBagCap(bag)), InventoryHelper.getBagExtraRight(CapHelper.getBagCap(bag)));

		if (bag.hasDisplayName())
			name = bag.getDisplayName();
		else
			name = new TranslationTextComponent("buildersbag.name");

		setupInventories();
	}

	public ContainerBag(int windowID, PlayerInventory inv, PacketBuffer data)
	{
		super(BuildersBagRegistry.BAG_CONTAINER_TYPE, windowID);
		CompoundNBT tag = data.readCompoundTag();
		PlayerEntity player = inv.player;
		
		if(tag.contains("isBauble"))
		{
			this.slot = tag.getInt("slot");
			this.isBauble = true;
			this.bag = BaublesAPI.getBauble(slot);
		}
		else
		{
			this.hand = tag.getBoolean("hand") ? Hand.MAIN_HAND : Hand.OFF_HAND;
			this.bag = player.getHeldItem(hand);
			this.slot = InventoryHelper.getSlotForStack(player, bag);
		}
		
		this.player = player;
		this.bagCap = CapHelper.getBagCap(bag);
		this.inv = bagCap.getBlockInventory();
		this.invSize = this.inv.getSlots();

		this.leftOffset = Math.max(InventoryHelper.getBagExtraLeft(CapHelper.getBagCap(bag)), InventoryHelper.getBagExtraRight(CapHelper.getBagCap(bag)));

		if (bag.hasDisplayName())
			name = bag.getDisplayName();
		else
			name = new TranslationTextComponent("buildersbag.name");
		
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
					inventoryBagSlots.add(addSlot(new SlotItemHandler(inv, slotIndex++, x + j * 18 + leftOffset, y + i * 18)));
				else
					break;
			}
		}

		selectedBlockSlot = addSlot(new SelectedBlockSlot(bagCap.getSelectedInventory(), 0, 80 + leftOffset, -24));
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
				addSlot(new Slot(player.inventory, slotIndex++, x + j * 18 + leftOffset, y + i * 18));
			}
		}

		slotIndex = 0;

		y += HOTBAR_OFFSET + 3 * 18;

		for (int j = 0; j < 9; j++)
		{
			addSlot(new Slot(player.inventory, slotIndex++, x + j * 18 + leftOffset, y));
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
					addSlot(new ToggleableSlot(handler, slotIndex++, x + j * 18 + leftOffset, y).setSlotEnabled(module.isExpanded()));
				}
			}

			x = getTotalWidth() + 40;
			y += 34;
			rightModuleCount++;

		}

		y = 8;
		x = -40;

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
					addSlot(new ToggleableSlot(handler, slotIndex++, x - j * 18 - 16 + leftOffset, y).setSlotEnabled(module.isExpanded()));
				}
			}
			else
				x = -41;

			y += 34;
		}

		this.modules = builder.build();
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
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

		}
		else if (index >= 9 * 4 && index < 9 * (4 + rows))
		{
			// Item is in bag inventory
			if (!this.mergeItemStack(stack, 0, 36, false))
				return ItemStack.EMPTY;
		}
		else
		{
			// Item is somewhere else in bag
			if (!this.mergeItemStack(stack, 0, 36, false))
				return ItemStack.EMPTY;
		}

		return itemstack;
	}

	@Override
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
				}
				else if (i >= endIndex)
				{
					break;
				}

				Slot slot = this.inventorySlots.get(i);
				ItemStack itemstack = slot.getStack();
				if (!itemstack.isEmpty() && areItemsAndTagsEqual(stack, itemstack) && (slot instanceof ToggleableSlot ? ((ToggleableSlot) slot).isSlotEnabled() : true))
				{
					int j = itemstack.getCount() + stack.getCount();
					int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
					if (j <= maxSize)
					{
						stack.setCount(0);
						itemstack.setCount(j);
						slot.onSlotChanged();
						flag = true;
					}
					else if (itemstack.getCount() < maxSize)
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
				}
				else
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
			}
			else
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
				}
				else if (i >= endIndex)
				{
					break;
				}

				Slot slot1 = this.inventorySlots.get(i);
				ItemStack itemstack1 = slot1.getStack();
				if (itemstack1.isEmpty() && slot1.isItemValid(stack) && (slot1 instanceof ToggleableSlot ? ((ToggleableSlot) slot1).isSlotEnabled() : true))
				{
					if (stack.getCount() > slot1.getSlotStackLimit())
					{
						slot1.putStack(stack.split(slot1.getSlotStackLimit()));
					}
					else
					{
						slot1.putStack(stack.split(stack.getCount()));
					}

					slot1.onSlotChanged();
					flag = true;
					break;
				}

				if (reverseDirection)
				{
					--i;
				}
				else
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

	public void updateModule(String name, CompoundNBT nbt)
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

		setupInventories();
	}

	public void sync()
	{
		if (!player.world.isRemote)
		{
			if (isBauble)
				BuildersBag.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SyncBagCapInventoryClient(bagCap, slot, true));
			else
				BuildersBag.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SyncBagCapClient(bagCap, hand));
		}
	}

	@Override
	public boolean canInteractWith(PlayerEntity player)
	{
		if (ModList.get().isLoaded("baubles"))
		{
			return isBauble ? BaubleHelper.getBauble(player, this.slot) == bag : !player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand) == bag;
		}
		return !player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand) == bag;
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player)
	{
		if (slotId == selectedBlockSlot.slotNumber)
		{
			ItemStack mouseItem = player.inventory.getItemStack().copy();
			if (selectedBlockSlot.isItemValid(mouseItem) || mouseItem.isEmpty())
			{
				mouseItem.setCount(1);
				selectedBlockSlot.putStack(mouseItem);
			}
			sync();

			return ItemStack.EMPTY;
		}

		ItemStack ret = super.slotClick(slotId, dragType, clickTypeIn, player);

		sync();

		return ret;
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn)
	{
		super.onContainerClosed(playerIn);

		sync();

		BagCache.clearBagCache(bag);
	}

	public static class BagContainerFactory implements IContainerFactory<ContainerBag>
	{

		@Override
		public ContainerBag create(int windowId, PlayerInventory inv, PacketBuffer data)
		{
			return new ContainerBag(windowId, inv, data);
		}

	}

}
