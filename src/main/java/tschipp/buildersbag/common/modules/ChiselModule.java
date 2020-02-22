package tschipp.buildersbag.common.modules;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import team.chisel.api.IChiselItem;
import team.chisel.api.carving.CarvingUtils;
import team.chisel.api.carving.ICarvingGroup;
import team.chisel.api.carving.ICarvingVariation;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.common.caps.IBagCap;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.inventory.ItemHandlerWithPredicate;

public class ChiselModule extends AbstractBagModule
{

	private ItemHandlerWithPredicate handler = new ItemHandlerWithPredicate(1, (stack, slot) -> stack.getItem() instanceof IChiselItem);
	private static final ItemStack DISPLAY = new ItemStack(Item.getByNameOrId("chisel:chisel_iron"));

	public ChiselModule()
	{
		super("buildersbag:chisel");
	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag)
	{
		NonNullList<ItemStack> providedSacks = InventoryHelper.getAllAvailableStacksExcept(bag, this);
		NonNullList<ItemStack> list = NonNullList.create();

		ItemStack chisel = handler.getStackInSlot(0);
		if (chisel.isEmpty())
			return list;

		Set<ICarvingGroup> groups = new HashSet<ICarvingGroup>();

		for (ItemStack stack : providedSacks)
		{
			if (!stack.isEmpty())
			{
				ICarvingGroup group = CarvingUtils.getChiselRegistry().getGroup(stack);
				if (group != null)
					groups.add(group);
			}
		}

		for (ICarvingGroup group : groups)
		{
			for (ICarvingVariation variation : group)
			{
				list.add(variation.getStack());
			}
		}

		return list;
	}

	@Override
	public String[] getModDependencies()
	{
		return new String[] { "chisel" };
	}

	@Override
	public ItemStackHandler getInventory()
	{
		return handler;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound tag = super.serializeNBT();
		tag.setTag("Inventory", handler.serializeNBT());
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		super.deserializeNBT(nbt);
		handler.deserializeNBT(nbt.getCompoundTag("Inventory"));
	}

	@Override
	public boolean doesntUseOwnInventory()
	{
		return false;
	}

	@Override
	public ItemStack getDisplayItem()
	{
		return DISPLAY;
	}

	@Override
	public ItemStack createStack(ItemStack stack, IBagCap bag, EntityPlayer player)
	{
		// if (InventoryHelper.containsStack(stack,
		// this.getPossibleStacks(bag)).isEmpty())
		// return ItemStack.EMPTY;

		ICarvingGroup group = CarvingUtils.getChiselRegistry().getGroup(stack);
		if (group == null)
			return ItemStack.EMPTY;

		ItemStack chisel = handler.getStackInSlot(0);
		if (chisel.isEmpty())
			return ItemStack.EMPTY;
		
		NonNullList<ItemStack> availableBlocks = InventoryHelper.getStacks(bag.getBlockInventory());

		for (ICarvingVariation variant : group)
		{
			ItemStack provided = InventoryHelper.containsStack(variant.getStack(), availableBlocks);

			if (!provided.isEmpty())
			{		
				if (!player.world.isRemote)
				{
					if (chisel.attemptDamageItem(1, new Random(), (EntityPlayerMP) player))
						chisel.shrink(1);
				}
				
				provided.shrink(1);
				return ItemHandlerHelper.copyStackWithSize(stack, 1);
			}
		}

		for (ICarvingVariation variant : group)
		{
			ItemStack provided = InventoryHelper.getOrProvideStack(variant.getStack(), bag, player, this);

			if (!provided.isEmpty())
			{
				if (!ItemStack.areItemsEqual(provided, stack) && !player.world.isRemote)
				{
					if (chisel.attemptDamageItem(1, new Random(), (EntityPlayerMP) player))
						chisel.shrink(1);
				}
				
				provided.shrink(1);
				return ItemHandlerHelper.copyStackWithSize(stack, 1);
			}
		}

		return ItemStack.EMPTY;
	}
}
