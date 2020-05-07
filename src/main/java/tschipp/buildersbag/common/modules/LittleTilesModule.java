package tschipp.buildersbag.common.modules;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.item.ItemBlockIngredient;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredient;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredientEntry;
import com.creativemd.littletiles.common.util.ingredient.IngredientUtils;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.compat.littletiles.NonModifiableLittleIngredients;

public class LittleTilesModule extends AbstractBagModule
{
	private static final ItemStack DISPLAY = new ItemStack(Item.getByNameOrId("littletiles:chisel"));

	public LittleTilesModule()
	{
		super("buildersbag:littletiles");
	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag)
	{
		return NonNullList.create();
	}

	@Override
	public ItemStack createStack(ItemStack stack, IBagCap bag, EntityPlayer player)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean doesntUseOwnInventory()
	{
		return true;
	}

	@Override
	public ItemStackHandler getInventory()
	{
		return null;
	}

	@Override
	public ItemStack getDisplayItem()
	{
		return DISPLAY;
	}

	@Optional.Method(modid = "littletiles")
	public static strictfp void provideLittleIngredients(ItemStack stack, LittleIngredients ingredients, LittleIngredients overflow, EntityPlayer player, LittleInventory inventory)
	{
		BlockIngredient overflowBlk = new BlockIngredient();
		IBagCap bag = CapHelper.getBagCap(stack);

		List<LittleIngredient> toSub = new ArrayList<LittleIngredient>();

		for (LittleIngredient ing : ingredients)
		{
			if (ing instanceof BlockIngredient)
			{
				BlockIngredient blockIng = (BlockIngredient) ing;

				ing: for (BlockIngredientEntry entry : blockIng)
				{					
					ItemStack blockStack = entry.getItemStack();
					Block block = entry.block;
					int meta = entry.meta;
					int req = (int) Math.ceil(entry.value);

					NonNullList<ItemStack> inv = InventoryHelper.getStacks(bag.getBlockInventory());
					for (ItemStack invStack : inv)
					{
						if (invStack.getItem() instanceof ItemBlockIngredient)
						{
							BlockIngredientEntry invBlockIng = ItemBlockIngredient.loadIngredient(invStack);

							if (invBlockIng != null)
							{
								if (entry.block == invBlockIng.block && entry.meta == invBlockIng.meta)
								{
									if (entry.value <= invBlockIng.value)
									{
										BlockIngredient toSub1 = new BlockIngredient();
										toSub1.add(entry);
										toSub.add(toSub1);

										if (!player.world.isRemote && !inventory.isSimulation())
										{
											invBlockIng.value -= entry.value;
											ItemBlockIngredient.saveIngredient(invStack, invBlockIng);
										}
										if (invBlockIng.value <= 0)
											invStack.shrink(1);

										continue ing;

									} else
									{
										if (!player.world.isRemote && !inventory.isSimulation())
										{
											blockIng.sub(invBlockIng);
										}
										invStack.shrink(1);
									}
								}
							}
						}
					}

					List<ItemStack> providedBlocks = InventoryHelper.getOrProvideStackWithCount(blockStack, req, CapHelper.getBagCap(stack), player, null);

					double leftover = entry.value - providedBlocks.size();
					
					if (leftover > 0)
					{
						overflowBlk.add(IngredientUtils.getBlockIngredient(block, meta, entry.value - providedBlocks.size()));
						BlockIngredient subIng = new BlockIngredient();

						subIng.add(IngredientUtils.getBlockIngredient(block, meta, providedBlocks.size()));
						toSub.add(subIng);

					} else
					{
						if (leftover < 0)
						{

							BlockIngredientEntry toAdd = IngredientUtils.getBlockIngredient(block, meta, 1.0 - (entry.value % 1));
							insertIngredientsIntoBag(stack, player, toAdd);
						}
						BlockIngredient subIng = new BlockIngredient();

						subIng.add(entry);
						toSub.add(subIng);
					}
				}
			} else
				overflow.add(ing);
		}

		for (LittleIngredient sub : toSub)
			ingredients.sub(sub);

		if (!overflowBlk.isEmpty())
			overflow.add(overflowBlk);
	}

	public static LittleIngredients getAvailableIngredients(ItemStack bag)
	{
		NonModifiableLittleIngredients existingIng = new NonModifiableLittleIngredients();

		existingIng.setModifiable(true);
		
		NonNullList<ItemStack> allAvailable = InventoryHelper.getAllAvailableStacks(CapHelper.getBagCap(bag));
		BlockIngredient blIng = new BlockIngredient();

		for (ItemStack available : allAvailable)
		{
			if (available.getItem() instanceof ItemBlock)
			{
				if (LittleAction.isBlockValid(BlockUtils.getState(available)))
					blIng.add(IngredientUtils.getBlockIngredient(Block.getBlockFromItem(available.getItem()), available.getMetadata(), 1));
			} else if (available.getItem() instanceof ItemBlockIngredient)
			{
				LittleIngredients av = ((ItemBlockIngredient) available.getItem()).getInventory(available);
				if (av != null)
				{
					av = av.copy();

					existingIng.add(av);
				}
			}
		}

		NonModifiableLittleIngredients ing = new NonModifiableLittleIngredients();
		
		ing.setModifiable(true);
		
		ing.add(blIng);
		ing.add(existingIng);

		for (LittleIngredient aving : ing)
		{
			if (aving instanceof BlockIngredient)
			{
				for (BlockIngredientEntry aventr : (BlockIngredient) aving)
				{
					aventr.value = LittleGridContext.get().pixelSize / 512;
				}
			}
		}
		
		ing.setModifiable(false);
		return ing;
	}

	public static strictfp void addIngredients(ItemStack stack, LittleIngredients ing, EntityPlayer player)
	{
		for (LittleIngredient i : ing)
		{
			if (i instanceof BlockIngredient)
			{
				for (BlockIngredientEntry entry : (BlockIngredient) i)
				{
					if (entry.value >= LittleGridContext.get().pixelSize)
					{
						entry.value = -(LittleGridContext.get().pixelSize / 512) + entry.value;
						insertIngredientsIntoBag(stack, player, entry);
					}
				}
			}
		}
	}

	private static void insertIngredientsIntoBag(ItemStack stack, EntityPlayer player, BlockIngredientEntry ing)
	{
		IBagCap bag = CapHelper.getBagCap(stack);
		NonNullList<ItemStack> inventory = InventoryHelper.getStacks(bag.getBlockInventory());

		ItemStack tileItemStack = ItemStack.EMPTY;

		boolean newStack = true;

		for (ItemStack invStack : inventory)
		{
			if(!invStack.isEmpty() && invStack.getItem() instanceof ItemBlockIngredient)
			{
				BlockIngredientEntry loadedIngredient = ItemBlockIngredient.loadIngredient(invStack);
				if(loadedIngredient.block == ing.block && loadedIngredient.meta == ing.meta)
				{
					tileItemStack = invStack;
					loadedIngredient.value += ing.value;
					ing = loadedIngredient;
					newStack = false;
					break;
				}
			}
		}

		if (tileItemStack.isEmpty())
			tileItemStack = new ItemStack(LittleTiles.blockIngredient);

		tileItemStack.setTagCompound(new NBTTagCompound());
		ItemBlockIngredient.saveIngredient(tileItemStack, ing);

		BlockIngredientEntry entry = ItemBlockIngredient.loadIngredient(tileItemStack);
		if(entry.value >= 1.0)
		{
			int fullblocks = (int) entry.value;
			InventoryHelper.addStack(new ItemStack(entry.block, fullblocks, entry.meta), bag, player);
			entry.value -= fullblocks;
			
			if(entry.value <= 0)
			{
				tileItemStack.shrink(1);
				newStack = false;
			}
			else
			{
				ItemBlockIngredient.saveIngredient(tileItemStack, entry);
			}
		}
		
		if (newStack)
			InventoryHelper.addStack(tileItemStack, bag, player);
	}
}
