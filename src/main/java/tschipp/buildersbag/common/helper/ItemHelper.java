package tschipp.buildersbag.common.helper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;

public class ItemHelper
{

	public static void addLore(ItemStack stack, String... lines)
	{
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null)
			tag = new NBTTagCompound();
	
		NBTTagCompound display = tag.getCompoundTag("display");
		NBTTagList lore = display.getTagList("Lore", 8);
		if (lore == null)
			lore = new NBTTagList();
	
		for (String s : lines)
			lore.appendTag(new NBTTagString(s));
	
		display.setTag("Lore", lore);
		tag.setTag("display", display);
		stack.setTagCompound(tag);
	}

	public static ItemStack containsStack(ItemStack stack, NonNullList<ItemStack> stacks)
	{
		for (ItemStack s : stacks)
		{
			if (ItemStack.areItemsEqual(stack, s))
				return s;
		}
		return ItemStack.EMPTY;
	}

	public static NonNullList<ItemStack> listOf(ItemStack stack, int count)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		for (int i = 0; i < count; i++)
			list.add(stack.copy());
		return list;
	}
	
	public static NonNullList<ItemStack> copy(NonNullList<ItemStack> list)
	{
		NonNullList<ItemStack> l = NonNullList.create();
		for(ItemStack s : list)
		{
			l.add(s.copy());
		}
		
		return l;
	}

}
