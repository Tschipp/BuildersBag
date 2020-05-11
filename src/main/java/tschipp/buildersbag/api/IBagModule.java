package tschipp.buildersbag.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;

public interface IBagModule extends INBTSerializable<NBTTagCompound>
{

	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag, EntityPlayer player);
		
	
	/**
	 * Creates the requested stack using some method. If any remainder stacks are created during this process,
	 * they must be added with InventoryHelper.addStack.
	 * @param stack the stack that is requested
	 * @param bag
	 * @param player
	 * @return the newly created stack, with size 1
	 */
	public ItemStack createStack(ItemStack stack, IBagCap bag, EntityPlayer player);
	
	public void toggle();
	
	public boolean doesntUseOwnInventory();
	
	public boolean isEnabled();
	
	public boolean isExpanded();
	
	public void setExpanded(boolean bool);
		
	public ItemStackHandler getInventory();
	
	public ItemStack getDisplayItem();
	
	public String getName();
	
	/**
	 * Whether this module "dominates" other modules, meaning it has its own rule for supplying blocks.
	 * If multiple dominating modules are enabled, the first one will be used. A prominent example is the randomness module
	 */
	default boolean isDominating()
	{
		return false;
	}

	/**
	 * Selects a block based on some criteria. Only fired on dominating Modules
	 */
	default ItemStack getBlock(IBagCap bag, EntityPlayer player)
	{
		return ItemStack.EMPTY;
	}
	
	default boolean isSupplier()
	{
		return false;
	}
	
}
