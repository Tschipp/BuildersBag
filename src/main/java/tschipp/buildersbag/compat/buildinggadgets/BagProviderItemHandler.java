package tschipp.buildersbag.compat.buildinggadgets;

import com.direwolf20.buildinggadgets.common.integration.BlockProviderItemHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.cache.BagCache;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;

public class BagProviderItemHandler extends ItemStackHandler implements BlockProviderItemHandler
{
	private ItemStack bag = ItemStack.EMPTY;
	private ItemStack lastExtractedItem = ItemStack.EMPTY;
	private EntityPlayer storedPlayer = null;

	private NonNullList<ItemStack> provideableStacks = NonNullList.create();
	private NonNullList<ItemStack> bagInventoryStacks = NonNullList.create();
	private NonNullList<ItemStack> providedStacks = NonNullList.create();

	private ItemStack markedExtractedItem = ItemStack.EMPTY;
	private long availabilityListGenerationTime = 0;

	public BagProviderItemHandler(ItemStack bag)
	{
		this.bag = bag;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
//		if (System.currentTimeMillis() - availabilityListGenerationTime >= 100)
//		{
//			updatePossibleItems(storedPlayer);
//		}
//
//		if (slot < bagInventoryStacks.size())
//		{
//			return bagInventoryStacks.get(slot);
//		}
//		else if (slot - bagInventoryStacks.size() < provideableStacks.size())
//		{
//			return provideableStacks.get(slot - bagInventoryStacks.size());
//		}
//		else
//		{
//			return providedStacks.get(slot - bagInventoryStacks.size() - provideableStacks.size());
//		}	
		
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
//		ItemStack toBeExtracted = getStackInSlot(slot);
//		boolean isAvailable = slot < bagInventoryStacks.size();
//		boolean isProvideable = slot < provideableStacks.size();
//
//		int toExtract = Math.min(amount, toBeExtracted.getMaxStackSize());
//
//		if (isAvailable)
//		{
//			ItemStack copystack = ItemHandlerHelper.copyStackWithSize(toBeExtracted, toExtract);
//			toBeExtracted.shrink(toExtract);
//			return copystack;
//		}
//
//		if (isProvideable)
//		{
//			if (!ItemStack.areItemsEqual(toBeExtracted, markedExtractedItem))
//			{
//				updatePossibleItems(storedPlayer);
//				markedExtractedItem = toBeExtracted.copy();
//			}
//			provide(markedExtractedItem);
//			return ItemStack.EMPTY;
//		}
//		
//		ItemStack copy = toBeExtracted.copy();
//		toBeExtracted.shrink(toExtract);
//		provide(markedExtractedItem);
//
//		return copy;
		
		return ItemStack.EMPTY;
	}

	private void provide(ItemStack stack)
	{
		if (storedPlayer == null)
			return;

		IBagCap cap = CapHelper.getBagCap(bag);
		ItemStack providedStack = BagHelper.getOrProvideStack(stack, cap, storedPlayer, null);
		if (!providedStack.isEmpty())
		{
			providedStacks.add(providedStack);
		}
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
//		IBagCap cap = CapHelper.getBagCap(bag);
//		ItemStackHandler handler = cap.getBlockInventory();
//		for (int i = 0; i < handler.getSlots(); i++)
//		{
//			if (handler.insertItem(i, stack, true) != stack)
//			{
//				ItemStack rest = handler.insertItem(i, stack, false);
//				if (!rest.isEmpty())
//					return this.insertItem(slot, stack, simulate);
//				return rest;
//			}
//		}
//
//		return stack;
		
//		IBagCap cap = CapHelper.getBagCap(bag);
//		BagHelper.addStack(stack, cap, storedPlayer);
//		return ItemStack.EMPTY;
		
		return stack;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
//		IBagCap cap = CapHelper.getBagCap(bag);
//		return cap.getBlockInventory().isItemValid(slot, stack);
		
		return false;
	}

	public void updatePossibleItems(EntityPlayer player)
	{
		if (player == null)
		{
			BuildersBag.LOGGER.error("SOMETHING HAS GONE WRONG, THIS STATE CANNOT EXIST.");
			BuildersBag.LOGGER.error("PLEASE REPORT THIS ERROR!");
			return;
		}

		IBagCap cap = CapHelper.getBagCap(bag);
		this.provideableStacks.clear();
		this.provideableStacks.addAll(BagHelper.getAllProvideableStacksExcept(cap, player, null));
		this.provideableStacks.add(ItemStack.EMPTY);
		this.bagInventoryStacks.clear();
		this.bagInventoryStacks.addAll(InventoryHelper.getInventoryStacks(cap, player));

		this.providedStacks.forEach(stack -> BagHelper.addStack(stack, cap, player));
		this.providedStacks.clear();
		
		availabilityListGenerationTime = System.currentTimeMillis();
	}

	public void setPlayer(EntityPlayer player)
	{
		if(player == null)
			System.out.println("PLAYER WAS NULL!!!!!!!!!!!!!!!");
		
//		System.out.println(this.hashCode() + BuildersBag.proxy.getSide().toString());
		
		this.storedPlayer = player;
//		
//		BagProviderItemHandler handler = (BagProviderItemHandler) bag.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//		
//		System.out.println(handler.hashCode());
//		
//		handler.storedPlayer = player;
//				
//		if (System.currentTimeMillis() - availabilityListGenerationTime >= 100)
//		{
//			updatePossibleItems(storedPlayer);
//		}
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
//		System.out.println(this.hashCode() + Arrays.toString(Thread.currentThread().getStackTrace()));
	
//		System.out.println(this.hashCode());
//		
//		if (System.currentTimeMillis() - availabilityListGenerationTime >= 100)
//		{
//			updatePossibleItems(storedPlayer);
//		}
		
//		int slots = bagInventoryStacks.size() + providedStacks.size() + provideableStacks.size();

//		System.out.println(this.hashCode() + BuildersBag.proxy.getSide().toString() + " " + slots);
		
//		return slots;
		
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

	@Override
	public int extractItems(ItemStack toExtract, int count, EntityPlayer player)
	{
		long time = System.currentTimeMillis();
				
		IBagCap cap = CapHelper.getBagCap(bag);
		
		if(!cap.hasModuleAndEnabled("buildersbag:supplier"))
			return 0;
		
		NonNullList<ItemStack> provided = BagHelper.getOrProvideStackWithCount(toExtract, count, cap, player, null);
	
		System.out.println(System.currentTimeMillis() - time + "ms needed to provide for count: " + count);
		
		return provided.size();
	}

	@Override
	public int getItemCount(ItemStack toCount, EntityPlayer player)
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
}
