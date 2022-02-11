package tschipp.buildersbag.common.modules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.api.BagComplex;
import tschipp.buildersbag.api.BagInventory;
import tschipp.buildersbag.api.BagModuleType;
import tschipp.buildersbag.api.CreateableItemsManager;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.IngredientKey;
import tschipp.buildersbag.api.ModulePriority;
import tschipp.buildersbag.api.RequirementListener.ItemCreationRequirements;
import tschipp.buildersbag.common.BuildersBagRegistry;
import tschipp.buildersbag.common.crafting.CraftingHandler;
import tschipp.buildersbag.common.helper.BagHelper;

public class CraftingModule extends AbstractBagModule
{

	private CraftingStack crafter = new CraftingStack();

	private static final ItemStack DISPLAY = new ItemStack(Blocks.CRAFTING_TABLE);
	private final CreateableItemsManager manager;

	public CraftingModule()
	{
		manager = new CreateableItemsManager(this);
	}

	@Override
	public ItemStackHandler getInventory()
	{
		return null;
	}

	@Override
	public boolean doesntUseOwnInventory()
	{
		return true;
	}

	@Override
	public ItemStack getDisplayItem()
	{
		return DISPLAY;
	}

	// public NonNullList<ItemStack> createStackWithRecipeTree(ItemStack stack,
	// int count, IBagCap bag, PlayerEntity player, @Nullable RecipeTree
	// subTree, @Nonnull ItemStack root)
	// {
	// boolean newTree = subTree == null;
	//
	// if (subTree == null)
	// subTree =
	// CraftingHandler.getSubTree(InventoryHelper.getInventoryStacks(bag,
	// player));
	//
	// if (root.isEmpty())
	// root = stack;
	//
	// if (newTree)
	// BagHelper.updateTreeCache(subTree, root);
	//
	// NonNullList<ItemStack> possibleStacks = subTree.getPossibleStacks(false);
	//
	// boolean isPossible = false;
	// for (ItemStack possible : possibleStacks)
	// if (ItemStack.isSame(possible, stack))
	// isPossible = true;
	//
	// if (!isPossible)
	// {
	// return NonNullList.create();
	// }
	//
	// NonNullList<ItemStack> list = NonNullList.create();
	//
	// int attempt = 0;
	//
	// while (attempt < 10 && list.size() < count)
	// {
	// CraftingStepList stepList =
	// subTree.generateCraftingStepList(CraftingHandler.getItemString(stack),
	// count - list.size(), player, bag);
	//
	// attempt++;
	//
	// if(stepList == null)
	// {
	// continue;
	// }
	//
	// NonNullList<ItemStack> providedRecipeIngredients = NonNullList.create();
	//
	// steplist:
	// for (CraftingStep step : stepList)
	// {
	// RecipeContainer recipe = step.getRecipe();
	// int recipeAmount = step.getRecipeAmount();
	//
	// for (Entry<ItemContainer, Integer> ingEntry :
	// step.ingredientsToUse.entrySet())
	// {
	// int needed = ingEntry.getValue();
	// ItemStack ingStack = ingEntry.getKey().getItem();
	//
	// int removed = InventoryHelper.removeMatchingStacksWithSizeOne(ingStack,
	// needed, providedRecipeIngredients).size();
	// needed -= removed;
	//
	// if (needed <= 0)
	// continue;
	//
	// NonNullList<ItemStack> provided = BagHelper.getStackDontProvide(ingStack,
	// needed, bag, player);
	// needed -= provided.size();
	//
	// if(needed > 0)
	// {
	// stepList.blacklist();
	// break steplist;
	// }
	// }
	//
	// for(int i = 0; i < recipeAmount; i++)
	// providedRecipeIngredients.add(recipe.getOutput().copy());
	// }
	//
	// list.addAll(InventoryHelper.removeMatchingStacksWithSizeOne(stack, count
	// - list.size(), providedRecipeIngredients));
	//
	// for(ItemStack s : providedRecipeIngredients)
	// BagHelper.addStack(s, bag, player);
	//
	//
	// if(list.size() >= count)
	// return list;
	//
	// stepList.blacklist();
	// }

	// return list;
	// }

	@Override
	public ModulePriority getPriority()
	{
		return ModulePriority.LOW;
	}
	//
	// @Override
	// public NonNullList<ItemStack> createStackWithCount(ItemStack stack, int
	// count, IBagCap bag, PlayerEntity player)
	// {
	// return createStackWithRecipeTree(stack, count, bag, player, null,
	// ItemStack.EMPTY);
	// }

	@Override
	public CreateableItemsManager getCreateableItemsManager()
	{
		return manager;
	}

	@Override
	public BagModuleType<? extends IBagModule> getType()
	{
		return BuildersBagRegistry.MODULE_CRAFTING;
	}

	@Override
	public int createItems(ItemCreationRequirements req, int count, BagComplex complex, PlayerEntity player)
	{
		ICraftingRecipe recipe = req.getMeta();
		int recipeOutputCount = recipe.getResultItem().getCount();
		NonNullList<Ingredient> ings = recipe.getIngredients();
		BagInventory inv = complex.getInventory();

		int craftAmount = (int) Math.ceil(count / (double) recipeOutputCount);

		int created = 0;

		Map<IngredientKey, Integer> ingCounts = CraftingHandler.getIngredientCounts(recipe);
		Map<IngredientKey, LinkedList<Item>> gatheredIngs = new HashMap<>();
		Set<Item> uncraftable = new HashSet<>();
		
		crafter.push();

		crafter: for (int i = 0; i < craftAmount; i++)
		{
			gatheredIngs.clear();

			int width = 3;
			int height = 3;

			if (recipe instanceof IShapedRecipe)
			{
				width = ((IShapedRecipe<?>) recipe).getRecipeWidth();
				height = ((IShapedRecipe<?>) recipe).getRecipeHeight();
			}

			// Generates a map of ingredients for the crafting, so we gather all
			// of them at once
			for (Entry<IngredientKey, Integer> entry : ingCounts.entrySet())
			{
				int total = 0;
				Integer needed = entry.getValue();

				List<Item> sorted = entry.getKey().sorted(complex);

				for (Item it : sorted)
				{
					if(needed-total <= 0)
						break;
					
					if(uncraftable.contains(it))
						continue;
					
					int c = complex.take(it, needed - total, player);
					
					if(c == 0)
					{
						uncraftable.add(it);
						continue;
					}
					
					LinkedList<Item> items = gatheredIngs.getOrDefault(entry.getKey(), new LinkedList<>());
					for (int l = 0; l < Math.min(c, needed); l++)
						items.add(it);
					gatheredIngs.put(entry.getKey(), items);
					total += Math.min(needed, c);
				}

				if (total < needed)
				{
					reAddOverflow(player, gatheredIngs, inv);
					break crafter;
				}
			}

			itemplacer: for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					int slotIndex = x + (y * 3);
					int ingIndex = x + (y * width);
					if (ingIndex >= ings.size() || ings.get(ingIndex).isEmpty())
						continue;

					IngredientKey key = IngredientKey.of(ings.get(ingIndex));

					crafter.setItem(slotIndex, new ItemStack(gatheredIngs.get(key).removeFirst()));
					if (recipe.matches(crafter, player.level))
						break itemplacer;
				}
			}

			if (recipe.matches(crafter, player.level))
			{
				ItemStack output = recipe.assemble(crafter);
				crafter.clearContent();

				if (!output.isEmpty())
					created += output.getCount();
			}
			else
			{
				for (int y = 0; y < crafter.getHeight(); y++)
				{
					for (int x = 0; x < crafter.getWidth(); x++)
					{
						int slotIndex = x + (y * 3);

						inv.addPhysical(crafter.removeItem(slotIndex, 64), BagHelper.handleExcess(player));
					}
				}
			}

//			reAddOverflow(player, gatheredIngs, inv);
//			System.out.println("Should be empty: " + gatheredIngs);

		}

		crafter.pop();

		return created;
	}

	private void reAddOverflow(PlayerEntity player, Map<IngredientKey, LinkedList<Item>> gatheredIngs, BagInventory inv)
	{		
		for (Entry<IngredientKey, LinkedList<Item>> e : gatheredIngs.entrySet())
		{
			//TODO: This seems to be the issue witht he desyncs, look into this.
			e.getValue().forEach(it -> {
				inv.addPhysical(it, 1, BagHelper.handleExcess(player));
			});
		}
	}

	private static class CraftingStack extends CraftingInventory
	{
		private Stack<NonNullList<ItemStack>> craftStack = new Stack<>();

		public CraftingStack()
		{
			super(new Container((ContainerType<?>) null, -1) {
				public boolean stillValid(PlayerEntity player)
				{
					return false;
				}
			}, 3, 3);
		}

		public void push()
		{
			NonNullList<ItemStack> items = NonNullList.create();
			for (int y = 0; y < this.getHeight(); y++)
			{
				for (int x = 0; x < this.getWidth(); x++)
				{
					int slotIndex = x + (y * 3);
					items.add(this.removeItem(slotIndex, 64));
				}
			}
			craftStack.push(items);
			this.clearContent();
		}

		public void pop()
		{
			NonNullList<ItemStack> items = craftStack.pop();
			for (int y = 0; y < this.getHeight(); y++)
			{
				for (int x = 0; x < this.getWidth(); x++)
				{
					int slotIndex = x + (y * 3);
					this.setItem(slotIndex, items.get(slotIndex));
				}
			}
		}

	}

}
