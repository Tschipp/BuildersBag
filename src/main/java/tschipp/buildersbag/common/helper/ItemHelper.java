package tschipp.buildersbag.common.helper;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.NonNullList;

public class ItemHelper
{

	public static void addLore(ItemStack stack, String... lines)
	{
		CompoundNBT tag = stack.getTag();
		if (tag == null)
			tag = new CompoundNBT();
	
		CompoundNBT display = tag.getCompound("display");
		ListNBT lore = display.getList("Lore", 8);
		if (lore == null)
			lore = new ListNBT();
	
		for (String s : lines)
			lore.add(StringNBT.valueOf(s));
	
		display.put("Lore", lore);
		tag.put("display", display);
		stack.setTag(tag);
	}

	public static ItemStack containsStack(ItemStack stack, List<ItemStack> stacks)
	{
		for (ItemStack s : stacks)
		{
			if (ItemStack.isSame(stack, s))
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
	
	public static void removeDuplicates(List<ItemStack> stacks)
	{
		for (int i = 0; i < stacks.size(); i++)
		{
			ItemStack pr = stacks.get(i);
			for(int j = 0; j < stacks.size(); j++)
			{
				ItemStack dupe = stacks.get(j);
				if(dupe != pr && ItemStack.matches(pr.copy().split(1), dupe.copy().split(1)))
				{
					stacks.remove(j);
					j--;
				}
			}
		}
	}

}
