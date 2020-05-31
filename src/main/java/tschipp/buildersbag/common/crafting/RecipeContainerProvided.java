package tschipp.buildersbag.common.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

public class RecipeContainerProvided extends RecipeContainer
{

	public RecipeContainerProvided(ItemStack output)
	{
		super(NonNullList.from(Ingredient.fromStacks(output)), output, "");
	}

}
