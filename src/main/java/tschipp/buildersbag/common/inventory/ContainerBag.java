package tschipp.buildersbag.common.inventory;

import static tschipp.buildersbag.common.helper.InventoryHelper.HOTBAR_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.INV_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.LEFT_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.TOP_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.getBagRows;
import static tschipp.buildersbag.common.helper.InventoryHelper.getMaxModules;
import static tschipp.buildersbag.common.helper.InventoryHelper.getTotalWidth;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.caps.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;

public class ContainerBag extends Container
{
	private ItemStack bag;
	private IBagCap bagCap;
	private EntityPlayer player;
	private EnumHand hand;
	private ItemStackHandler inv;

	public int invSize;
	public String name;
	public ImmutableMap<IBagModule, Triple<Integer, Integer, Boolean>> modules;
	
	
	public ContainerBag(EntityPlayer player, ItemStack bag, EnumHand hand)
	{
		this.player = player;
		this.bag = bag;
		this.hand = hand;
		this.bagCap = CapHelper.getBagCap(bag);
		this.inv = bagCap.getBlockInventory();
		this.invSize = inv.getSlots();
		
		if(bag.hasDisplayName())
			name = bag.getDisplayName();
		else
			name = "Builder's Bag";

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
					addSlotToContainer(new SlotItemHandler(inv, slotIndex++, x + j * 18, y + i * 18));
				else
					return;
			}
		}
	}

	private void setupPlayerInventory()
	{
		int x = 0;
		int y = 0;

		int slotIndex = 9;
		
		x += LEFT_OFFSET + 1;
		y += TOP_OFFSET + 1 + getBagRows(invSize)* 18 + INV_OFFSET;
		
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(player.inventory, slotIndex++, x + j * 18, y + i * 18));
			}
		}
		
		slotIndex = 0;
		
		y += HOTBAR_OFFSET + 3 * 18;
		
		for (int j = 0; j < 9; j++)
		{
			addSlotToContainer(new Slot(player.inventory, slotIndex++, x + j * 18, y));
		}
	}
	
	private void setupModuleInventories()
	{
		Builder<IBagModule, Triple<Integer, Integer, Boolean>> builder = ImmutableMap.builder();
		
		int moduleCount = getMaxModules(invSize);
		int x = getTotalWidth() + 40;
		int y = 7;
		
		int rightModuleCount = 0;
		
		int processedModules = 0;
		
		for(int i = 0; i < bagCap.getModules().length; i++)
		{
			if(rightModuleCount == moduleCount)
				break;
			 
			processedModules = i + 1;
			
			IBagModule module = bagCap.getModules()[i];
			if(module.getInventory() != null)
			{
				int slotIndex = 0;
				ItemStackHandler handler = module.getInventory();
				for(int j = 0; j < handler.getSlots(); j++)
				{
					addSlotToContainer(new ToggleableSlot(handler, slotIndex++, x + j * 18, y).setEnabled(false));
				}
			}
			else
				continue;
			
			builder.put(module, new ImmutableTriple<Integer, Integer, Boolean>(x - 40, y - 8, true));
			
			x = getTotalWidth() + 40;
			y += 34;
			rightModuleCount++;
			
		}
		
		
		
		y = 7;
		x = -41;
		
		for(int i = processedModules; i < bagCap.getModules().length; i++)
		{
			IBagModule module = bagCap.getModules()[i];
			if(module.getInventory() != null)
			{
				int slotIndex = 0;
				ItemStackHandler handler = module.getInventory();
				for(int j = 0; j < handler.getSlots(); j++)
				{
					addSlotToContainer(new ToggleableSlot(handler, slotIndex++, x - j * 18, y).setEnabled(false));
				}
			}
			else
				continue;
			
			builder.put(module, new ImmutableTriple<Integer, Integer, Boolean>(x + 40, y - 8, false));
			
			x = -41;
			y += 34;
		}
		
		this.modules = builder.build();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return true;
	}

}
