package tschipp.buildersbag.compat.blocksourceadapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import tschipp.buildersbag.api.IBlockSourceAdapter;

public class BlockSourceAdapterHandler //TODO: Rewrite
{
	private static final Set<IBlockSourceAdapter> adapters = new HashSet<IBlockSourceAdapter>();
	
	public static void registerAdapter(IBlockSourceAdapter adapter)
	{
		adapters.add(adapter);
	}
	
	public static boolean hasAdapter(ItemStack stack)
	{
		for(IBlockSourceAdapter adapter : adapters)
		{
			if(adapter.isValid(stack))
				return true;
		}
		return false;
	}
	
	public static ItemStack createBlock(ItemStack fromStack, ItemStack toCreate, PlayerEntity player, boolean simulate)
	{
		for(IBlockSourceAdapter adapter : adapters)
		{
			if(adapter.isValid(fromStack))
				return adapter.createBlock(fromStack, toCreate, player, simulate);
		}
		return ItemStack.EMPTY;
	}
	
	public static List<ItemStack> getCreateableBlocks(ItemStack fromStack, PlayerEntity player)
	{
		for(IBlockSourceAdapter adapter : adapters)
		{
			if(adapter.isValid(fromStack))
				return adapter.getCreateableBlocks(fromStack, player);
		}
		return Collections.EMPTY_LIST;
	}
	

}
