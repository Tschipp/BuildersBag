package tschipp.buildersbag.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;

public interface IBagModule extends INBTSerializable<NBTTagCompound>
{

	/**
	 * Gets a list of all stacks that this module can create using all other stacks,
	 * so most of the time {@link tschipp.buildersbag.common.helper.BagHelper#getAllAvailableStacksExcept} is used to find the stacks of all other modules first.
	 * @param bag
	 * @param player
	 * @return the possible stacks with size 1, or an empty list.
	 */
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag, EntityPlayer player);
		
	
	/**
	 * Creates the requested stack using some method. If any remainder stacks are created during this process,
	 * they must be added with InventoryHelper.addStack.
	 * @param stack the stack that is requested
	 * @param bag
	 * @param player
	 * @return the newly created stack, with size 1
	 */
	default ItemStack createStack(ItemStack stack, IBagCap bag, EntityPlayer player)
	{
		NonNullList<ItemStack> list = createStackWithCount(stack, 1, bag, player);
		if(list.isEmpty())
			return ItemStack.EMPTY;
		return list.get(0);
	}
	
	/**
	 * Creates the requested stack using some method. If any remainder stacks are created during this process,
	 * they must be added with InventoryHelper.addStack.
	 * @param stack the stack that is requested
	 * @param count
	 * @param bag
	 * @param player
	 * @return A list of the created stacks, all with size 1. If stacks cannot be created, return an empty list.
	 */
	public NonNullList<ItemStack> createStackWithCount(ItemStack stack, int count, IBagCap bag, EntityPlayer player);
	
	/**
	 * Toggles this module
	 */
	public void toggle();
	
	/**
	 * If this module *shouldn't* have its own inventory, this should be true
	 */
	public boolean doesntUseOwnInventory();
	
	/**
	 * If this module is enabled
	 */
	public boolean isEnabled();
	
	/**
	 * Check if the internal inventory is expanded
	 */
	public boolean isExpanded();
	
	/**
	 * Sets the module's expanded state
	 */
	public void setExpanded(boolean bool);
	
	/**
	 * @return This module's ItemStackHandler, or null if it doesn't have one
	 */
	public ItemStackHandler getInventory();
	
	/**
	 * Gets a display stack for the icon. Should be a static final stack, because this gets called every frame.
	 */
	public ItemStack getDisplayItem();
	
	/**
	 * The name of the module, mostly the same as the registry name
	 */
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
	
	/**
	 * If the module acts as a supplier
	 */
	default boolean isSupplier()
	{
		return false;
	}
	
	/**
	 * Compacts the stacks using some method. 
	 * @param toCompact
	 * @return a new, compacted list, or <code>toCompact</code> if it does nothing
	 */
	default NonNullList<ItemStack> getCompactedStacks(NonNullList<ItemStack> toCompact, EntityPlayer player)
	{
		return toCompact;
	}
	
	/**
	 * Returns the module Priority when providing stacks. Higher priority modules get checked first.
	 */
	default ModulePriority getPriority()
	{
		return ModulePriority.NORMAL;
	}
	
}
