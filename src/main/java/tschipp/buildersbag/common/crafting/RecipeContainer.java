package tschipp.buildersbag.common.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

public class RecipeContainer
{
	private NonNullList<Ingredient> ingredients;
	private ItemStack output;
	private String stage;

	public RecipeContainer(NonNullList<Ingredient> ingredients, ItemStack output, String stage)
	{
		this.ingredients = NonNullList.create();
		this.ingredients.addAll(ingredients);
		this.output = output.copy();
		this.stage = stage;
	}

	public NonNullList<Ingredient> getIngredients()
	{
		return ingredients;
	}

	public ItemStack getOutput()
	{
		return output.copy();
	}

	public String getStage()
	{
		return stage;
	}
}
