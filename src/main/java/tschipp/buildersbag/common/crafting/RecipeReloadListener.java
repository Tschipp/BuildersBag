package tschipp.buildersbag.common.crafting;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tschipp.buildersbag.BuildersBag;

@EventBusSubscriber(modid = BuildersBag.MODID, bus = Bus.FORGE)
public class RecipeReloadListener extends JsonReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	private RecipeManager recipeManager;
	
	public RecipeReloadListener(RecipeManager recipeManager)
	{
		super(GSON, "recipes");
		this.recipeManager = recipeManager;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objects, IResourceManager manager, IProfiler profiler)
	{
//		CraftingHandler.recipeTree = new RecipeTree();
//		
//		List<ICraftingRecipe> rec = recipeManager.getAllRecipesFor(IRecipeType.CRAFTING);
//		
//		for (ICraftingRecipe recipe : rec)
//		{ 
//			for(Ingredient ing : recipe.getIngredients())
//			{
//				CraftingHandler.addIngredientIfAlternative(ing);
//			}
//		}
//		
//		for (ICraftingRecipe recipe : rec)
//		{
//			ItemStack output = recipe.getResultItem();
//			if (!output.isEmpty())
//			{
//				NonNullList<Ingredient> ingredients = recipe.getIngredients();
//				String itemString = CraftingHandler.getItemString(output);
//				List<RecipeContainer> genRecipes = CraftingHandler.recipes.get(itemString);
//
//				if (genRecipes != null)
//				{
//					genRecipes.add(new RecipeContainer(ingredients, output, CraftingHandler.getTierIfStaged(recipe)));
//				}
//				else
//				{
//					genRecipes = new ArrayList<RecipeContainer>();
//					genRecipes.add(new RecipeContainer(ingredients, output, CraftingHandler.getTierIfStaged(recipe)));
//					CraftingHandler.recipes.put(itemString, genRecipes);
//				}
//
//			}
//
//			CraftingHandler.recipeTree.add(recipe);
//		}
//		
//		System.out.println("Crating tree loaded");
		CraftingRecipesLoadedEvent event = new CraftingRecipesLoadedEvent(recipeManager);
		MinecraftForge.EVENT_BUS.post(event);
	}
	
	@SubscribeEvent
	public static void onDatapackRegister(AddReloadListenerEvent event)
	{
		event.addListener(new RecipeReloadListener(event.getDataPackRegistries().getRecipeManager()));
	}
}
