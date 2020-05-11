package tschipp.buildersbag.compat.botania;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import tschipp.buildersbag.api.IBlockSourceAdapter;
import tschipp.buildersbag.common.item.BuildersBagItem;
import vazkii.botania.api.item.IBlockProvider;
import vazkii.botania.common.item.ItemBlackHoleTalisman;
import vazkii.botania.common.item.ItemEnderHand;
import vazkii.botania.common.item.rod.ItemCobbleRod;
import vazkii.botania.common.item.rod.ItemDirtRod;
import vazkii.botania.common.item.rod.ItemTerraformRod;

public class BotaniaAdapter implements IBlockSourceAdapter
{

	@Override
	public boolean isValid(ItemStack stack)
	{
		return stack.getItem() instanceof IBlockProvider && !(stack.getItem() instanceof BuildersBagItem);
	}

	@Override
	public ItemStack createBlock(ItemStack fromStack, ItemStack toCreate, EntityPlayer player, boolean simulate)
	{
		IBlockProvider provider = (IBlockProvider) fromStack.getItem();
		if (provider.provideBlock(player, player.getHeldItem(EnumHand.MAIN_HAND), fromStack, Block.getBlockFromItem(toCreate.getItem()), toCreate.getMetadata(), !simulate || fromStack.getItem() instanceof ItemEnderHand))
			return toCreate.copy();
		return ItemStack.EMPTY;
	}

	@Override
	public List<ItemStack> getCreateableBlocks(ItemStack fromStack, EntityPlayer player)
	{
		List<ItemStack> resultList = new ArrayList<ItemStack>();
		Item item = fromStack.getItem();
		
		if(item instanceof ItemBlackHoleTalisman)
		{
			ItemStack result = item.getContainerItem(fromStack);
			if(!result.isEmpty())
				resultList.add(result);
		}
		else if (item instanceof ItemCobbleRod)
		{
			resultList.add(new ItemStack(Blocks.COBBLESTONE));
		}
		else if(item instanceof ItemDirtRod || item instanceof ItemTerraformRod)
		{
			resultList.add(new ItemStack(Blocks.DIRT));
		}
		else if(item instanceof ItemEnderHand)
		{
			InventoryEnderChest echest = player.getInventoryEnderChest();
			for(int i = 0; i < echest.getInventoryStackLimit(); i++)
			{
				resultList.add(echest.getStackInSlot(i).copy());
			}
		}
		
		return resultList;
	}

}
