package tschipp.buildersbag.compat.littletiles;

import com.creativemd.littletiles.common.util.ingredient.LittleIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;

public class NonModifiableLittleIngredients extends LittleIngredients
{
	private boolean modifiable = false;

	public void setModifiable(boolean bool)
	{
		this.modifiable = bool;
	}

	@Override
	public LittleIngredient sub(LittleIngredient ingredient)
	{
		if (!modifiable)
			return ingredient;
		else
			return super.sub(ingredient);
	}

	@Override
	public LittleIngredients sub(LittleIngredients ing)
	{
		if (!modifiable)
			return ing;
		else
			return super.sub(ing);
	}

	
	@Override
	protected boolean canAddNewIngredients()
	{
		return modifiable;
	}
}
