package tschipp.buildersbag.common.recipes;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;

import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import tschipp.buildersbag.common.config.BuildersBagConfig;

public class EnableUpdateRecipes implements IConditionFactory
{

	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json)
	{
		return () -> BuildersBagConfig.Settings.addUpdateRecipes;
	}

}
