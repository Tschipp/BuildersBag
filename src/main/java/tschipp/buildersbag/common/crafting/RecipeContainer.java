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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ingredients == null) ? 0 : ingredients.hashCode());
		result = prime * result + ((output == null) ? 0 : output.hashCode());
		result = prime * result + ((stage == null) ? 0 : stage.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecipeContainer other = (RecipeContainer) obj;
		if (ingredients == null)
		{
			if (other.ingredients != null)
				return false;
		} else if (!ingredients.equals(other.ingredients))
			return false;
		if (output == null)
		{
			if (other.output != null)
				return false;
		} else if (!output.equals(other.output))
			return false;
		if (stage == null)
		{
			if (other.stage != null)
				return false;
		} else if (!stage.equals(other.stage))
			return false;
		return true;
	}
	
	
}
