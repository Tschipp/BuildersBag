package tschipp.buildersbag.compat.buildinggadgets;

import com.direwolf20.buildinggadgets.common.integration.IItemAccess;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.cache.BagCache;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;

public class BagProviderItemHandler extends ItemStackHandler implements IItemAccess
{
	private ItemStack bag = ItemStack.EMPTY;

	public BagProviderItemHandler(ItemStack bag)
	{
		this.bag = bag;
	}

	@Override
	public int extractItems(ItemStack toExtract, int count, EntityPlayer player)
	{
		long time = System.currentTimeMillis();
				
		IBagCap cap = CapHelper.getBagCap(bag);
		
		if(!cap.hasModuleAndEnabled("buildersbag:supplier"))
			return 0;
		
		NonNullList<ItemStack> provided = BagHelper.getOrProvideStackWithCount(toExtract, count, cap, player, null);
			
		return provided.size();
	}

	@Override
	public int getItemsForExtraction(ItemStack toCount, EntityPlayer player)
	{
		IBagCap cap = CapHelper.getBagCap(bag);
		if(!cap.hasModuleAndEnabled("buildersbag:supplier"))
			return 0;
			
		if(player.world.isRemote)
		{
			BagCache.startSimulation(bag);
			int provided = BagHelper.getOrProvideStackWithCount(toCount, 500, cap, player, null).size();
			BagCache.stopSimulation(bag);
			return provided;
		}
		
		//We lie here to save resources, heh
		return 500;
	}
	
	
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return ItemStack.EMPTY;
	}


	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		return stack;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return false;
	}


	@Override
	public NBTTagCompound serializeNBT()
	{
		return new NBTTagCompound();
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
	}
	
	@Override
	public int getSlots()
	{
		return 0;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
	}

	@Override
	public void setSize(int size)
	{
	}

	@Override
	protected void validateSlotIndex(int slot)
	{
	}

	
}
