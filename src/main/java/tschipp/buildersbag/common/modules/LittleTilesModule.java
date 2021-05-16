//package tschipp.buildersbag.common.modules;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import net.minecraft.block.Block;
//import net.minecraft.client.resources.I18n;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.BlockItem;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraft.util.NonNullList;
//import net.minecraftforge.items.ItemStackHandler;
//import tschipp.buildersbag.api.AbstractBagModule;
//import tschipp.buildersbag.api.IBagCap;
//import tschipp.buildersbag.api.datastructures.ItemContainer;
//import tschipp.buildersbag.common.helper.BagHelper;
//import tschipp.buildersbag.common.helper.CapHelper;
//import tschipp.buildersbag.common.helper.InventoryHelper;
//import tschipp.buildersbag.common.helper.ItemHelper;
//import tschipp.buildersbag.compat.littletiles.NonModifiableLittleIngredients;
//
//public class LittleTilesModule extends AbstractBagModule
//{
//	private static final ItemStack DISPLAY = new ItemStack(Item.getByNameOrId("littletiles:chisel"));
//
//	public LittleTilesModule()
//	{
//		super("buildersbag:littletiles");
//	}
//
//	@Override
//	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag, PlayerEntity player)
//	{
//		return NonNullList.create();
//	}
//
//	@Override
//	public boolean doesntUseOwnInventory()
//	{
//		return true;
//	}
//
//	@Override
//	public ItemStackHandler getInventory()
//	{
//		return null;
//	}
//
//	@Override
//	public ItemStack getDisplayItem()
//	{
//		return DISPLAY;
//	}
//
//	@Optional.Method(modid = "littletiles")
//	public static strictfp void provideLittleIngredients(ItemStack stack, LittleIngredients ingredients, LittleIngredients overflow, PlayerEntity player, LittleInventory inventory)
//	{
//		BlockIngredient overflowBlk = new BlockIngredient();
//		IBagCap bag = CapHelper.getBagCap(stack);
//
//		List<LittleIngredient> toSub = new ArrayList<LittleIngredient>();
//
//		for (LittleIngredient ing : ingredients)
//		{
//			if (ing instanceof BlockIngredient)
//			{
//				BlockIngredient blockIng = (BlockIngredient) ing;
//
//				ing: for (BlockIngredientEntry entry : blockIng)
//				{
//					ItemStack blockStack = entry.getItemStack();
//					Block block = entry.block;
//					int meta = entry.meta;
//					int req = (int) Math.ceil(entry.value);
//
//					NonNullList<ItemStack> inv = InventoryHelper.getStacks(bag.getBlockInventory());
//					for (ItemStack invStack : inv)
//					{
//						if (invStack.getItem() instanceof BlockItemIngredient)
//						{
//							BlockIngredientEntry invBlockIng = BlockItemIngredient.loadIngredient(invStack);
//
//							if (invBlockIng != null)
//							{
//								if (entry.block == invBlockIng.block && entry.meta == invBlockIng.meta)
//								{
//									if (entry.value <= invBlockIng.value)
//									{
//										BlockIngredient toSub1 = new BlockIngredient();
//										toSub1.add(entry);
//										toSub.add(toSub1);
//
//										if (!player.level.isClientSide && !inventory.isSimulation())
//										{
//											invBlockIng.value -= entry.value;
//											BlockItemIngredient.saveIngredient(invStack, invBlockIng);
//										}
//										if (invBlockIng.value <= 0)
//											invStack.shrink(1);
//
//										continue ing;
//
//									}
//									else
//									{
//										if (!player.level.isClientSide && !inventory.isSimulation())
//										{
//											blockIng.sub(invBlockIng);
//										}
//										invStack.shrink(1);
//									}
//								}
//							}
//						}
//					}
//
//					List<ItemStack> providedBlocks = BagHelper.getOrProvideStackWithCount(blockStack, req, CapHelper.getBagCap(stack), player, null);
//
//					double leftover = entry.value - providedBlocks.size();
//
//					if (leftover > 0)
//					{
//						overflowBlk.add(IngredientUtils.getBlockIngredient(block, meta, entry.value - providedBlocks.size()));
//						BlockIngredient subIng = new BlockIngredient();
//
//						subIng.add(IngredientUtils.getBlockIngredient(block, meta, providedBlocks.size()));
//						toSub.add(subIng);
//
//					}
//					else
//					{
//						if (leftover < 0)
//						{
//
//							BlockIngredientEntry toAdd = IngredientUtils.getBlockIngredient(block, meta, 1.0 - (entry.value % 1));
//							insertIngredientsIntoBag(stack, player, toAdd);
//						}
//						BlockIngredient subIng = new BlockIngredient();
//
//						subIng.add(entry);
//						toSub.add(subIng);
//					}
//				}
//			}
//			else
//				overflow.add(ing);
//		}
//
//		for (LittleIngredient sub : toSub)
//			ingredients.sub(sub);
//
//		if (!overflowBlk.isEmpty())
//			overflow.add(overflowBlk);
//	}
//
//	public static LittleIngredients getAvailableIngredients(ItemStack bag)
//	{
//		NonModifiableLittleIngredients existingIng = new NonModifiableLittleIngredients();
//
//		existingIng.setModifiable(true);
//
//		NonNullList<ItemStack> allAvailable = BagHelper.getAllAvailableStacks(CapHelper.getBagCap(bag), null); // TODO: Get the player
//		BlockIngredient blIng = new BlockIngredient();
//
//		for (ItemStack available : allAvailable)
//		{
//			if (available.getItem() instanceof BlockItem)
//			{
//				if (LittleAction.isBlockValid(BlockUtils.getState(available)))
//					blIng.add(IngredientUtils.getBlockIngredient(Block.byItem(available.getItem()), available.getMetadata(), 1));
//			}
//			else if (available.getItem() instanceof BlockItemIngredient)
//			{
//				LittleIngredients av = ((BlockItemIngredient) available.getItem()).getInventory(available);
//				if (av != null)
//				{
//					av = av.copy();
//
//					existingIng.add(av);
//				}
//			}
//		}
//
//		NonModifiableLittleIngredients ing = new NonModifiableLittleIngredients();
//
//		ing.setModifiable(true);
//
//		ing.add(blIng);
//		ing.add(existingIng);
//
//		for (LittleIngredient aving : ing)
//		{
//			if (aving instanceof BlockIngredient)
//			{
//				for (BlockIngredientEntry aventr : (BlockIngredient) aving)
//				{
//					aventr.value = LittleGridContext.get().pixelSize / 512;
//				}
//			}
//		}
//
//		ing.setModifiable(false);
//		return ing;
//	}
//
//	public static void setAvailableIngredients(HashMapList<String, ItemStack> list, ItemStack bag, IBagCap bagCap, PlayerEntity player)
//	{
//		NonNullList<ItemStack> allAvailable = BagHelper.getAllAvailableStacks(CapHelper.getBagCap(bag), player); // TODO: Get the player
//
//		Map<ItemContainer, Double> amounts = new HashMap<ItemContainer, Double>();
//
//		for (ItemStack stack : allAvailable)
//		{
//			if (stack.getItem() instanceof BlockItemIngredient)
//			{
//				BlockIngredientEntry entry = BlockItemIngredient.loadIngredient(stack);
//				ItemContainer cont = ItemContainer.forStack(new ItemStack(entry.block, 1, entry.meta));
//
//				Double amount = amounts.get(cont);
//				if (amount == null)
//					amount = 0.0;
//
//				amount += entry.value;
//
//				amounts.put(cont, amount);
//			}
//			else if (LittleAction.isBlockValid(BlockUtils.getState(stack)))
//			{
//				ItemContainer cont = ItemContainer.forStack(stack);
//
//				Double amount = amounts.get(cont);
//				if (amount == null)
//					amount = 0.0;
//
//				amount += stack.getCount();
//
//				amounts.put(cont, amount);
//			}
//		}
//
//		for (Entry<ItemContainer, Double> entry : amounts.entrySet())
//		{
//			ItemStack stack = entry.getKey().getItem();
//
//			stack.setCount(1);
//			
//			double amount = entry.getValue();
//			if (amount == 1)
//				ItemHelper.addLore(stack, I18n.translateToLocal("buildersbag.bagprovides"));
//			else
//				ItemHelper.addLore(stack, BlockIngredient.printVolume(amount, false));
//
//			list.add(I18n.translateToLocal("buildersbag.name"), stack);
//		}
//
//	}
//
//	public static strictfp void addIngredients(ItemStack stack, LittleIngredients ing, PlayerEntity player)
//	{
//		for (LittleIngredient i : ing)
//		{
//			if (i instanceof BlockIngredient)
//			{
//				for (BlockIngredientEntry entry : (BlockIngredient) i)
//				{
//					if (entry.value >= LittleGridContext.get().pixelSize)
//					{
//						entry.value = -(LittleGridContext.get().pixelSize / 512) + entry.value;
//						insertIngredientsIntoBag(stack, player, entry);
//					}
//				}
//			}
//		}
//		
////		if(!player.world.isRemote)
////		{
////			BuildersBag.network.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayerEntity) player), new SyncBagCapInventoryClient(stack, player));
////		}
//	}
//
//	private static void insertIngredientsIntoBag(ItemStack stack, PlayerEntity player, BlockIngredientEntry ing)
//	{
//		IBagCap bag = CapHelper.getBagCap(stack);
//		NonNullList<ItemStack> inventory = InventoryHelper.getStacks(bag.getBlockInventory());
//
//		ItemStack tileItemStack = ItemStack.EMPTY;
//
//		boolean newStack = true;
//
//		for (ItemStack invStack : inventory)
//		{
//			if (!invStack.isEmpty() && invStack.getItem() instanceof BlockItemIngredient)
//			{
//				BlockIngredientEntry loadedIngredient = BlockItemIngredient.loadIngredient(invStack);
//				if (loadedIngredient.block == ing.block && loadedIngredient.meta == ing.meta)
//				{
//					tileItemStack = invStack;
//					loadedIngredient.value += ing.value;
//					ing = loadedIngredient;
//					newStack = false;
//					break;
//				}
//			}
//		}
//
//		if (tileItemStack.isEmpty())
//			tileItemStack = new ItemStack(LittleTiles.blockIngredient);
//
//		tileItemStack.setTagCompound(new CompoundNBT());
//		BlockItemIngredient.saveIngredient(tileItemStack, ing);
//
//		BlockIngredientEntry entry = BlockItemIngredient.loadIngredient(tileItemStack);
//		if (entry.value >= 1.0)
//		{
//			int fullblocks = (int) entry.value;
//			BagHelper.addStack(new ItemStack(entry.block, fullblocks, entry.meta), bag, player);
//			entry.value -= fullblocks;
//
//			if (entry.value <= 0)
//			{
//				tileItemStack.shrink(1);
//				newStack = false;
//			}
//			else
//			{
//				BlockItemIngredient.saveIngredient(tileItemStack, entry);
//			}
//		}
//
//		if (newStack)
//			BagHelper.addStack(tileItemStack, bag, player);
//	}
//
//	@Override
//	public NonNullList<ItemStack> getCompactedStacks(NonNullList<ItemStack> toCompact, PlayerEntity player)
//	{
//		if (!this.isEnabled())
//			return toCompact;
//
//		NonNullList<ItemStack> stacks = NonNullList.create();
//
//		BlockIngredient ing = new BlockIngredient();
//
//		for (ItemStack s : toCompact)
//		{
//			if (s.getItem() instanceof BlockItemIngredient)
//			{
//				ing.add(BlockItemIngredient.loadIngredient(s));
//			}
//			else
//				stacks.add(s);
//		}
//
//		for (BlockIngredientEntry entry : ing)
//		{
//			ItemStack ingStack = new ItemStack(LittleTiles.blockIngredient);
//			ingStack.setTagCompound(new CompoundNBT());
//			BlockItemIngredient.saveIngredient(ingStack, entry);
//			stacks.add(ingStack);
//		}
//
//		return stacks;
//
//	}
//
//	@Override
//	public NonNullList<ItemStack> createStackWithCount(ItemStack stack, int count, IBagCap bag, PlayerEntity player)
//	{
//		return NonNullList.create();
//	}
//}
