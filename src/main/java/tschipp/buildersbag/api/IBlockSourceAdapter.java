package tschipp.buildersbag.api;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * This interface should be implemented if you want to register an item as a block source, but can't actually implement IBlockSource on that item
 * @author Tschipp
 *
 */
public interface IBlockSourceAdapter
{
	/**
	 * Check if the given ItemStack is valid for this Adapter.
	 * @param stack
	 * @return whether the stack is valid
	 */
	public boolean isValid(ItemStack stack);
	
	/**
	 * Creates a block from the given ItemStack. If simulate is true, then the state of the stack shouldn't be changed.
	 * @param fromStack
	 * @param toCreate The stack that should be created. NOTE: This gets called regardless if the block is in the getCreateableBlocks list.
	 * @param player
	 * @param simulate 
	 * @return The block as an ItemStack, or ItemStack.EMPTY if it can't be created.
	 */
	public ItemStack createBlock(ItemStack fromStack, ItemStack toCreate, EntityPlayer player, boolean simulate);

	/**
	 * Gets a list of all createable ItemStacks. This should be simulated, so the state of <code>fromStack</code> shouldn't be changed.
	 * @param fromStack
	 * @param player
	 * @return A list of stacks, or an emtpy list if none can be created.
	 */
	public List<ItemStack> getCreateableBlocks(ItemStack fromStack, EntityPlayer player);
}
