package tschipp.buildersbag.common.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.IBlockSource;
import tschipp.buildersbag.common.cache.BagCache;
import tschipp.buildersbag.common.crafting.RecipeTree;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.compat.baubles.BaubleHelper;
import tschipp.buildersbag.compat.blocksourceadapter.BlockSourceAdapterHandler;

public class BagHelper
{
	private static final Map<String, Integer> recursion_depth = new HashMap<String, Integer>();

	private static RecipeTree cachedTree = null;
	private static long treeCacheTime = 0;
	private static ItemStack cachedRoot = ItemStack.EMPTY;

	public static NonNullList<ItemStack> getOrProvideStackWithTree(ItemStack stack, int count, IBagCap bag, PlayerEntity player, @Nullable IBagModule exclude, @Nullable RecipeTree tree, @Nonnull ItemStack root)
	{
//		ItemStack bagStack = BagHelper.getStackFromCap(player, bag);
//
//		NonNullList<ItemStack> providedList = NonNullList.create();
//
//		if (tree == null && cachedTree != null && ItemStack.isSame(cachedRoot, stack) && System.currentTimeMillis() - treeCacheTime <= 1000)
//		{
//			tree = cachedTree;
//			root = cachedRoot;
//		}
//
//		if (player.level.isClientSide)
//		{
//			int amount = BagCache.getCachedAmount(bagStack, player, stack, count);
//
//			BagCache.modifyCachedAmount(bagStack, stack, -Math.min(amount, count));
//			return ItemHelper.listOf(stack, Math.min(amount, count));
//		}
//
//		providedList.addAll(getStackDontProvide(stack, count, bag, player));
//
//		for (IBagModule module : BagHelper.getSortedModules(bag))
//		{
//			if (module.isEnabled() && !module.isSupplier() && (exclude == null ? true : exclude != module))
//			{
//				if (providedList.size() >= count)
//				{
//					BuildersBag.proxy.changeWorkState(bag.getUUID(), player, false);
//					return providedList;
//				}
//
//				if (BagHelper.incrementRecursionDepth(player))
//				{
//					NonNullList<ItemStack> provided = module instanceof CraftingModule ? ((CraftingModule) module).createStackWithRecipeTree(stack, count - providedList.size(), bag, player, tree, root) : module.createStackWithCount(stack, count, bag, player);
//
//					BagHelper.resetRecursionDepth(player);
//					BagCache.sendBagModificationToClient(bagStack, stack, -provided.size(), player);
//
//					providedList.addAll(provided);
//
//				}
//				else
//				{
//					BuildersBag.proxy.changeWorkState(bag.getUUID(), player, false);
//					return providedList;
//				}
//			}
//
//		}
//
//		BuildersBag.proxy.changeWorkState(bag.getUUID(), player, false);
//		return providedList;
		return null;
	}

	public static NonNullList<ItemStack> getStackDontProvide(ItemStack stack, int count, IBagCap bag, PlayerEntity player)
	{
		ItemStack bagStack = BagHelper.getStackFromCap(player, bag);

		NonNullList<ItemStack> providedList = NonNullList.create();
		ItemStack foundStack = ItemStack.EMPTY;
		NonNullList<ItemStack> availableBlocks = InventoryHelper.getStacks(bag.getBlockInventory());

		BuildersBag.proxy.changeWorkState(bag.getUUID(), player, true);

		providedList.addAll(InventoryHelper.removeMatchingStacksWithSizeOne(stack, count, availableBlocks));

		for (ItemStack available : availableBlocks)
		{
			if (available.getItem() instanceof IBlockSource)
			{
				if (providedList.size() >= count)
				{
					BuildersBag.proxy.changeWorkState(bag.getUUID(), player, false);
					return providedList;
				}

				int toProvide = count - providedList.size();
				for (int i = 0; i < toProvide; i++)
				{
					ItemStack provided = ((IBlockSource) available.getItem()).createBlock(available, stack, player, player.level.isClientSide);
					if (!provided.isEmpty())
					{
						BagCache.sendBagModificationToClient(bagStack, stack, -1, player);
						providedList.add(provided);
					}
				}
			}
			else if (BlockSourceAdapterHandler.hasAdapter(available))
			{
				if (providedList.size() >= count)
				{
					BuildersBag.proxy.changeWorkState(bag.getUUID(), player, false);
					return providedList;
				}

				int toProvide = count - providedList.size();
				for (int i = 0; i < toProvide; i++)
				{
					ItemStack provided = BlockSourceAdapterHandler.createBlock(available, stack, player, BagHelper.botaniaCheck(available) ? false : player.level.isClientSide);
					if (!provided.isEmpty())
					{
						BagCache.sendBagModificationToClient(bagStack, stack, -1, player);
						providedList.add(provided);
					}
				}
			}
		}

		return providedList;
	}

	public static IBagModule getModule(String name, IBagCap cap)
	{
		for (IBagModule mod : cap.getModules())
		{
			if (mod != null && mod.getName().equals(name))
				return mod;
		}

		return null;
	}

	public static int getAllAvailableStacksCount(IBagCap bag, PlayerEntity player)
	{
		NonNullList<ItemStack> list = getAllAvailableStacks(bag, player);

		int count = 0;

		for (ItemStack s : list)
			count += s.getCount();

		return count;
	}

	public static NonNullList<ItemStack> getAllAvailableStacks(IBagCap bag, PlayerEntity player)
	{
		return getAllAvailableStacksExcept(bag, player, null);
	}

	public static NonNullList<ItemStack> getAllProvideableStacksExcept(IBagCap bag, PlayerEntity player, @Nullable IBagModule exclude)
	{
		NonNullList<ItemStack> list = NonNullList.create();

//		for (IBagModule module : getSortedModules(bag))
//		{
//			if (module.isEnabled() && module != exclude)
//				list.addAll(module.getPossibleStacks(bag, player));
//		}
		return list;
	}

	public static NonNullList<ItemStack> getAllAvailableStacksExcept(IBagCap bag, PlayerEntity player, @Nullable IBagModule exclude)
	{
		NonNullList<ItemStack> list = NonNullList.create();

		list.addAll(InventoryHelper.getInventoryStacks(bag, player));
		list.addAll(BagHelper.getAllProvideableStacksExcept(bag, player, exclude));
		return list;
	}

	private static boolean incrementRecursionDepth(PlayerEntity player)
	{
//		Integer i = recursion_depth.get(player.getUUID().toString());
//		if (i == null)
//			i = new Integer(0);
//
//		i++;
//
//		recursion_depth.put(player.getUUID().toString(), i);

//		if (i > Configs.Settings.maximumRecursionDepth.get())
//		{
//			return false;
//		}

		return true;
	}

	public static void resetRecursionDepth(PlayerEntity player)
	{
		recursion_depth.put(player.getUUID().toString(), 0);
	}

	public static ItemStack getOrProvideStack(ItemStack stack, IBagCap bag, PlayerEntity player, @Nullable IBagModule exclude)
	{
		NonNullList<ItemStack> list = getOrProvideStackWithTree(stack, 1, bag, player, exclude, null, ItemStack.EMPTY);
		if (list.isEmpty())
			return ItemStack.EMPTY;

		return list.get(0);
	}

	/*
	 * Returns true if stack can be provided.
	 */
	public static boolean simulateProvideStack(ItemStack stack, ItemStack bag, PlayerEntity player, @Nullable IBagModule exclude)
	{
		return !getOrProvideStack(stack, CapHelper.getBagCap(bag.copy()), new FakePlayerCopy((ServerWorld) player.level, player.getGameProfile(), player), exclude).isEmpty();
	}

	/*
	 * Tries to provide the given amount of stacks. If it can't, it will give
	 * you those that it managed to make.
	 */
	public static NonNullList<ItemStack> getOrProvideStackWithCount(ItemStack stack, int count, IBagCap bag, PlayerEntity player, @Nullable IBagModule exclude)
	{
		NonNullList<ItemStack> list = getOrProvideStackWithTree(stack, count, bag, player, exclude, null, ItemStack.EMPTY);

		return list;
	}

	public static NonNullList<ItemStack> getOrProvideStackWithCountDominating(int count, IBagCap bag, PlayerEntity player)
	{
		NonNullList<ItemStack> provided = NonNullList.create();

		IBagModule dominatingModule = null;

		for (IBagModule module : getSortedModules(bag))
		{
			if (module.isEnabled() && module.isDominating())
			{
				dominatingModule = module;
				break;
			}
		}

		if (dominatingModule != null)
		{
			for (int i = 0; i < count; i++)
			{
				ItemStack s = BagHelper.getOrProvideStack(dominatingModule.getBlock(bag, player), bag, player, null);
				if (s.isEmpty())
					break;

				provided.add(s);
			}
		}

		return provided;
	}

	public static NonNullList<ItemStack> simulateProvideStackWithCount(ItemStack stack, int count, ItemStack bag, PlayerEntity player, @Nullable IBagModule exclude)
	{
		IBagCap bagCopy = CapHelper.getBagCap(bag.copy());
		// bagCopy.setUUID(UUID.randomUUID().toString());

		return getOrProvideStackWithCount(stack, count, bagCopy, new FakePlayerCopy((ServerWorld) player.level, player.getGameProfile(), player), exclude);
	}

	public static void compactStacks(IBagCap cap, PlayerEntity player)
	{
		NonNullList<ItemStack> stacks = InventoryHelper.getStacks(cap.getBlockInventory());

		for (IBagModule module : getSortedModules(cap))
		{
			stacks = module.getCompactedStacks(stacks, player);
		}

		for (int i = 0; i < cap.getBlockInventory().getSlots(); i++)
		{
			cap.getBlockInventory().setStackInSlot(i, ItemStack.EMPTY);
		}

		for (ItemStack s : stacks)
		{
			addStack(s, cap, player);
		}
	}

	public static void addStack(ItemStack stack, int count, IBagCap cap, PlayerEntity player)
	{
		int stacks = count / 64;
		int rest = count % 64;

		for (int i = 0; i < stacks; i++)
		{
			ItemStack s = stack.copy();
			s.setCount(64);
			addStack(s, cap, player);
		}

		ItemStack s = stack.copy();
		s.setCount(rest);
		addStack(s, cap, player);
	}

	public static void addStack(ItemStack stack, IBagCap cap, PlayerEntity player)
	{
		if (player == null || player.isCreative())
			return;

		ItemStackHandler handler = cap.getBlockInventory();
		for (int i = 0; i < handler.getSlots(); i++)
		{
			if (handler.insertItem(i, stack, true) != stack)
			{
				ItemStack rest = handler.insertItem(i, stack, false);
				if (!rest.isEmpty())
					addStack(rest, cap, player);
				return;
			}
		}

		if (!player.addItem(stack))
		{
			player.drop(stack, false);
		}
	}

	public static void addOrDropStack(ItemStack stack, IBagCap cap, PlayerEntity player)
	{
		if (player.isCreative())
			return;

		ItemStackHandler handler = cap.getBlockInventory();
		for (int i = 0; i < handler.getSlots(); i++)
		{
			if (handler.insertItem(i, stack, true) != stack)
			{
				ItemStack rest = handler.insertItem(i, stack, false);
				if (!rest.isEmpty())
					addStack(rest, cap, player);
				return;
			}
		}

		if (!player.addItem(stack))
		{
			player.drop(stack, false);
		}
	}

	public static void addStackToPlayerInvOrDrop(ItemStack stack, PlayerEntity player)
	{
		if (player.isCreative())
			return;

		if (!player.level.isClientSide)
		{
			ItemEntity eItem = new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), stack);
			player.level.addFreshEntity(eItem);
		}
	}

	public static ItemStack getStackFromCap(PlayerEntity player, IBagCap cap)
	{
		for (int i = 0; i < player.inventory.getContainerSize(); i++)
		{
			ItemStack s = player.inventory.getItem(i);
			if (s.getItem() instanceof BuildersBagItem)
			{
				if (CapHelper.areCapsEqual(cap, CapHelper.getBagCap(s)))
					return s;
			}
		}

		if (ModList.get().isLoaded("baubles"))
		{
			ItemStack bauble = BaubleHelper.getBauble(player,3);
			if (bauble.getItem() instanceof BuildersBagItem)
			{
				if (CapHelper.areCapsEqual(cap, CapHelper.getBagCap(bauble)))
					return bauble;
			}
		}

		return ItemStack.EMPTY;

	}

	public static List<IBagModule> getSortedModules(IBagCap cap)
	{
		List<IBagModule> modules = Lists.newArrayList(cap.getModules());
		modules = modules.stream().sorted((a, b) -> Integer.compare(b.getPriority().getVal(), a.getPriority().getVal())).collect(Collectors.toList());
		return modules;
	}

	public static void updateTreeCache(RecipeTree tree, ItemStack root)
	{
		cachedTree = tree;
		cachedRoot = root;
		treeCacheTime = System.currentTimeMillis();
	}

	public static void clearTreeBlacklist()
	{
		cachedTree.blacklistedRecipes.clear();
	}

	private static boolean botaniaCheck(ItemStack stack)
	{
//		if (ModList.get().isLoaded("botania")) TODO
//			return BotaniaCompat.isEnderHand(stack);
		return false;
	}

	@SuppressWarnings("deprecation")
	public static BiConsumer<Item, Integer> handleExcess(PlayerEntity player)
	{
		return (item, amount) -> {
			if (player == null || player.isCreative())
				return;

			while (amount > 0)
			{
				ItemStack stack = new ItemStack(item, Math.min(amount, item.getMaxStackSize()));
				if (!player.addItem(stack))
				{
					player.drop(stack, false);
				}
				amount -= Math.min(amount, item.getMaxStackSize());
			}
		};
	}

}
