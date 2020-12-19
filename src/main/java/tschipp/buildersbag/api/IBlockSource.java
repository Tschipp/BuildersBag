package tschipp.buildersbag.api;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Implement this interface on items that should be able to be inserted into the bag and used as a source for blocks.
 * @author Tschipp
 *
 */
public interface IBlockSource
{
	/**
	 * Creates a block from the given ItemStack. If simulate is true, then the state of the stack shouldn't be changed.
	 * @param fromStack
	 * @param toCreate The stack that should be created. NOTE: This gets called regardless if the block is in the getCreateableBlocks list.
	 * @param player
	 * @param simulate 
	 * @return The block as an ItemStack, or ItemStack.EMPTY if it can't be created.
	 */
	public ItemStack createBlock(ItemStack fromStack, ItemStack toCreate, PlayerEntity player, boolean simulate);

	/**
	 * Gets a list of all createable ItemStacks. This should be simulated, so the state of <code>fromStack</code> shouldn't be changed.
	 * @param fromStack
	 * @param player
	 * @return A list of stacks, or an emtpy list if none can be created.
	 */
	public List<ItemStack> getCreateableBlocks(ItemStack fromStack, PlayerEntity player);
}
