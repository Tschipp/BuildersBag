package tschipp.buildersbag.datagen;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;

public class BuildersBagRecipeProvider extends RecipeProvider
{

	public BuildersBagRecipeProvider(DataGenerator generatorIn)
	{
		super(generatorIn);
	}
	
	@Override
	protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer)
	{
//		ShapedRecipeBuilder.shapedRecipe(resultIn)
	}

}
