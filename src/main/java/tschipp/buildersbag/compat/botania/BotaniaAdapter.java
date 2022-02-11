//package tschipp.buildersbag.compat.botania;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import net.minecraft.block.Block;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.Hand;
//import tschipp.buildersbag.api.IBlockSourceAdapter;
//import tschipp.buildersbag.common.item.BuildersBagItem;
//import vazkii.botania.api.item.IBlockProvider;
//import vazkii.botania.common.item.ItemBlackHoleTalisman;
//import vazkii.botania.common.item.ItemEnderHand;
//import vazkii.botania.common.item.rod.ItemCobbleRod;
//import vazkii.botania.common.item.rod.ItemDirtRod;
//import vazkii.botania.common.item.rod.ItemTerraformRod;
//
//public class BotaniaAdapter implements IBlockSourceAdapter TODO
//{
//
//	@Override
//	public boolean isValid(ItemStack stack)
//	{
//		return stack.getItem() instanceof IBlockProvider && !(stack.getItem() instanceof BuildersBagItem);
//	}
//
//	@Override
//	public ItemStack createBlock(ItemStack fromStack, ItemStack toCreate, PlayerEntity player, boolean simulate)
//	{
//		IBlockProvider provider = (IBlockProvider) fromStack.getItem();
//		if (provider.provideBlock(player, player.getItemInHand(Hand.MAIN_HAND), fromStack, Block.byItem(toCreate.getItem()), toCreate.getMetadata(), !simulate || fromStack.getItem() instanceof ItemEnderHand))
//			return toCreate.copy();
//		return ItemStack.EMPTY;
//	}
//
//	@Override
//	public List<ItemStack> getCreateableBlocks(ItemStack fromStack, PlayerEntity player)
//	{
//		List<ItemStack> resultList = new ArrayList<ItemStack>();
//		Item item = fromStack.getItem();
//
//		if (item instanceof ItemBlackHoleTalisman)
//		{
//			ItemStack result = item.getContainerItem(fromStack);
//			if (!result.isEmpty())
//				resultList.add(result);
//		}
//		else if (item instanceof ItemCobbleRod)
//		{
//			resultList.add(new ItemStack(Blocks.COBBLESTONE));
//		}
//		else if (item instanceof ItemDirtRod || item instanceof ItemTerraformRod)
//		{
//			resultList.add(new ItemStack(Blocks.DIRT));
//		}
//		else if (item instanceof ItemEnderHand)
//		{
//			InventoryEnderChest echest = player.getEnderChestInventory();
//			for (int i = 0; i < echest.getInventoryStackLimit(); i++)
//			{
//				if (!echest.getStackInSlot(i).isEmpty())
//					resultList.add(echest.getStackInSlot(i).copy());
//			}
//		}
//
//		return resultList;
//	}
//
//}
