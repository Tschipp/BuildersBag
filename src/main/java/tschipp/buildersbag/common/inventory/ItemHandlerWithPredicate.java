package tschipp.buildersbag.common.inventory;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class ItemHandlerWithPredicate extends ItemStackHandler
{
	private BiPredicate<ItemStack, Integer> predicate;
	
	public ItemHandlerWithPredicate(int slots, BiPredicate<ItemStack, Integer> predicate)
	{
		super(slots);
		this.predicate = predicate;
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return predicate.test(stack, slot);
	}
}
