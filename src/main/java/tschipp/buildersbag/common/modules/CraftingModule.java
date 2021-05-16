//package tschipp.buildersbag.common.modules;
//
//import java.util.Map.Entry;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.minecraft.block.Blocks;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.NonNullList;
//import net.minecraftforge.items.ItemStackHandler;
//import tschipp.buildersbag.api.AbstractBagModule;
//import tschipp.buildersbag.api.IBagCap;
//import tschipp.buildersbag.api.ModulePriority;
//import tschipp.buildersbag.api.datastructures.ItemContainer;
//import tschipp.buildersbag.common.crafting.CraftingHandler;
//import tschipp.buildersbag.common.crafting.CraftingStepList;
//import tschipp.buildersbag.common.crafting.CraftingStepList.CraftingStep;
//import tschipp.buildersbag.common.crafting.RecipeContainer;
//import tschipp.buildersbag.common.crafting.RecipeTree;
//import tschipp.buildersbag.common.helper.BagHelper;
//import tschipp.buildersbag.common.helper.InventoryHelper;
//
//public class CraftingModule extends AbstractBagModule
//{
//
//	private static final ItemStack DISPLAY = new ItemStack(Blocks.CRAFTING_TABLE);
//
//	public CraftingModule()
//	{
//		super("buildersbag:crafting");
//	}
//
//	@Override
//	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag, PlayerEntity player)
//	{
//		return CraftingHandler.getPossibleBlocks(InventoryHelper.getInventoryStacks(bag, player), true);
//	}
//
//	@Override
//	public ItemStackHandler getInventory()
//	{
//		return null;
//	}
//
//	@Override
//	public boolean doesntUseOwnInventory()
//	{
//		return true;
//	}
//
//	@Override
//	public ItemStack getDisplayItem()
//	{
//		return DISPLAY;
//	}
//
//	public NonNullList<ItemStack> createStackWithRecipeTree(ItemStack stack, int count, IBagCap bag, PlayerEntity player, @Nullable RecipeTree subTree, @Nonnull ItemStack root)
//	{
//		boolean newTree = subTree == null;
//
//		if (subTree == null)
//			subTree = CraftingHandler.getSubTree(InventoryHelper.getInventoryStacks(bag, player));
//
//		if (root.isEmpty())
//			root = stack;
//
//		if (newTree)
//			BagHelper.updateTreeCache(subTree, root);
//
//		NonNullList<ItemStack> possibleStacks = subTree.getPossibleStacks(false);
//
//		boolean isPossible = false;
//		for (ItemStack possible : possibleStacks)
//			if (ItemStack.isSame(possible, stack))
//				isPossible = true;
//
//		if (!isPossible)
//		{
//			return NonNullList.create();
//		}
//
//		NonNullList<ItemStack> list = NonNullList.create();
//		
//		int attempt = 0;
//
//		while (attempt < 10 && list.size() < count)
//		{
//			CraftingStepList stepList = subTree.generateCraftingStepList(CraftingHandler.getItemString(stack), count - list.size(), player, bag);
//			
//			attempt++;
//
//			if(stepList == null)
//			{
//				continue;
//			}
//			
//			NonNullList<ItemStack> providedRecipeIngredients = NonNullList.create();
//
//			steplist:
//			for (CraftingStep step : stepList)
//			{
//				RecipeContainer recipe = step.getRecipe();
//				int recipeAmount = step.getRecipeAmount();
//
//				for (Entry<ItemContainer, Integer> ingEntry : step.ingredientsToUse.entrySet())
//				{
//					int needed = ingEntry.getValue();
//					ItemStack ingStack = ingEntry.getKey().getItem();
//
//					int removed = InventoryHelper.removeMatchingStacksWithSizeOne(ingStack, needed, providedRecipeIngredients).size();
//					needed -= removed;
//
//					if (needed <= 0)
//						continue;
//
//					NonNullList<ItemStack> provided = BagHelper.getStackDontProvide(ingStack, needed, bag, player);
//					needed -= provided.size();
//					
//					if(needed > 0)
//					{
//						stepList.blacklist();
//						break steplist;
//					}
//				}
//				
//				for(int i = 0; i < recipeAmount; i++)
//					providedRecipeIngredients.add(recipe.getOutput().copy());
//			}
//
//			list.addAll(InventoryHelper.removeMatchingStacksWithSizeOne(stack, count - list.size(), providedRecipeIngredients));
//			
//			for(ItemStack s : providedRecipeIngredients)
//				BagHelper.addStack(s, bag, player);
//			
//			
//			if(list.size() >= count)
//				return list;
//			
//			stepList.blacklist();
//		}
//
//		return list;
//	}
//
//	@Override
//	public ModulePriority getPriority()
//	{
//		return ModulePriority.LOW;
//	}
//
//	@Override
//	public NonNullList<ItemStack> createStackWithCount(ItemStack stack, int count, IBagCap bag, PlayerEntity player)
//	{
//		return createStackWithRecipeTree(stack, count, bag, player, null, ItemStack.EMPTY);
//	}
//
//}
