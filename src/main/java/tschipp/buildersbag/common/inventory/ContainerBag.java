package tschipp.buildersbag.common.inventory;

import static tschipp.buildersbag.common.helper.InventoryHelper.INV_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.LEFT_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.TOP_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.HOTBAR_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.getBagRows;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import tschipp.buildersbag.common.caps.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;

public class ContainerBag extends Container
{
	private ItemStack bag;
	private IBagCap bagCap;
	private EntityPlayer player;
	private EnumHand hand;
	private int invSize;
	private ItemStackHandler inv;
	private int slotIndex = 0;

	public ContainerBag(EntityPlayer player, ItemStack bag, EnumHand hand)
	{
		this.player = player;
		this.bag = bag;
		this.hand = hand;
		this.bagCap = CapHelper.getBagCap(bag);
		this.inv = bagCap.getBlockInventory();
		this.invSize = inv.getSlots();

		setupInventories();
	}

	private void setupInventories()
	{
		setupPlayerInventory();
//		setupBagInventory();
//		setupModuleInventories();
	}

	

	private void setupBagInventory()
	{
		int x = 0;
		int y = 0;

		x += LEFT_OFFSET + 1;
		y += TOP_OFFSET + 1;

		for (int i = 0; i < getBagRows(invSize); i++)
		{
			for (int j = 0; j < 9; j++)
			{
				if (slotIndex < invSize)
					addSlotToContainer(new SlotItemHandler(inv, slotIndex++, x + j * 19, y + i * 19));
				else
					return;
			}
		}
	}

	private void setupPlayerInventory()
	{
		int x = 0;
		int y = 0;

		slotIndex = 9;
		
		x += LEFT_OFFSET + 1;
		y += TOP_OFFSET + 1 + getBagRows(invSize)* 18 + INV_OFFSET;
		
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(player.inventory, slotIndex++, x + j * 19, y + i * 19));
			}
		}
		
		slotIndex = 0;
		
		y += HOTBAR_OFFSET + 3 * 18;
		
		for (int j = 0; j < 9; j++)
		{
			addSlotToContainer(new Slot(player.inventory, slotIndex++, x + j * 19, y));
		}
	}
	
	private void setupModuleInventories()
	{
		
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return true;
	}

}
