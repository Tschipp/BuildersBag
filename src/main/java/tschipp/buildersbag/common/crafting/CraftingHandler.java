package tschipp.buildersbag.common.crafting;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CraftingHandler
{
	private static Map<String, List<RecipeContainer>> recipes = new HashMap<String, List<RecipeContainer>>();


	public static void generateRecipes()
	{
		for (IRecipe recipe : ForgeRegistries.RECIPES)
		{
			ItemStack output = recipe.getRecipeOutput();
			if(!output.isEmpty())
			{
				NonNullList<Ingredient> ingredients = recipe.getIngredients();
				String itemString = getItemString(output);
				List<RecipeContainer> genRecipes = recipes.get(itemString);
				
				if(genRecipes != null)
				{
					genRecipes.add(new RecipeContainer(ingredients, output, getTierIfStaged(recipe)));
				}
				else
				{
					genRecipes = new ArrayList<RecipeContainer>();
					genRecipes.add(new RecipeContainer(ingredients, output, getTierIfStaged(recipe)));
					recipes.put(itemString, genRecipes);
				}
				
			}
		}
	}
	
	public static List<RecipeContainer> getRecipes(ItemStack stack)
	{
		String name = getItemString(stack);
		if(recipes.get(name) == null)
			return Collections.EMPTY_LIST;
		else return recipes.get(name);
	}
	
	public static String getTierIfStaged(IRecipe recipe)
	{

		if (Loader.isModLoaded("recipestages"))
		{
			try
			{
				Class clazz = Class.forName("com.blamejared.recipestages.recipes.RecipeStage");

				if (clazz.isInstance(recipe))
				{
					Method getTier = ReflectionHelper.findMethod(clazz, "getTier", null);
					String tier = (String) getTier.invoke(recipe);

					return tier;
				}

			} catch (Exception e)
			{
				return "";
			}
		}

		return "";
	}
	
	public static String getItemString(ItemStack output)
	{
		String outputString = output.getItem().getRegistryName().toString() + "@" + output.getMetadata() + "$" + (output.hasTagCompound() ? output.getTagCompound().toString() : "");
		return outputString;
	}

}
