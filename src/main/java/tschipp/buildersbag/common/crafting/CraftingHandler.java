package tschipp.buildersbag.common.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IngredientKey;
import tschipp.buildersbag.api.IngredientMapper;
import tschipp.buildersbag.api.RequirementListener;

@EventBusSubscriber(modid = BuildersBag.MODID, bus = Bus.FORGE)
public class CraftingHandler
{
	static final Map<String, List<RecipeContainer>> recipes = new HashMap<String, List<RecipeContainer>>();
	static RecipeTree recipeTree;

	private static final Map<String, Set<String>> alternativeIngredients = new HashMap<String, Set<String>>();
	private static final Map<Long, String> IDToNBTCache = new HashMap<Long, String>();
	private static final Map<String, Long> NBTToIDCache = new HashMap<String, Long>();

	private static long num = 1;

	static
	{
		IDToNBTCache.put(0L, "");
		NBTToIDCache.put("", 0L);
	}

	public static RequirementListener.Builder createRecipeListener(CraftingRecipesLoadedEvent event)
	{
		RecipeManager manager = event.manager;
		List<ICraftingRecipe> recipes = manager.getAllRecipesFor(IRecipeType.CRAFTING);

		RequirementListener.Builder builder = RequirementListener.builder();

//		top:
		for (ICraftingRecipe r : recipes)
		{
			if(r.getResultItem().getItem() == Items.PISTON)
				System.out.println("Foo");
			
			if (r.getIngredients().isEmpty() || r.getResultItem().isEmpty())
				continue;

			List<IngredientKey> keys = new ArrayList<>();

			for (Ingredient ing : r.getIngredients())
			{
				// create an ingredient key for each ingredient in the recipe
				Item[] items = Arrays.stream(ing.getItems()).map(stack -> stack.getItem()).toArray(Item[]::new);
				if (items.length > 0)
				{
					IngredientKey key = IngredientKey.of(items);

					// Add a mapping for item -> IngredientKey
					for (Item i : items)
						IngredientMapper.addMapping(i, key);

					keys.add(key);
				}
//				else
//					continue top;
			}

			Item result = r.getResultItem().getItem();
			builder.add(result, r, keys);
		}

		return builder;
	}
	
	public static Map<IngredientKey, Integer> getIngredientCounts(ICraftingRecipe recipe)
	{
		Map<IngredientKey, Integer> map = new HashMap<>();
		for (Ingredient ing : recipe.getIngredients())
		{
			if(!ing.isEmpty())
			{
				IngredientKey key = IngredientKey.of(ing);
				int val = map.getOrDefault(key, 0);
				map.put(key, val + 1);
			}
		}
		return map;
	}

	public static void generateRecipes()
	{
		// for (IRecipe recipe : ForgeRegistries.) TODO
		// {
		// for(Ingredient ing : recipe.getIngredients())
		// {
		// addIngredientIfAlternative(ing);
		// }
		// }
		//
		// for (IRecipe recipe : ForgeRegistries.RECIPES)
		// {
		// ItemStack output = recipe.getRecipeOutput();
		// if (!output.isEmpty())
		// {
		// NonNullList<Ingredient> ingredients = recipe.getIngredients();
		// String itemString = getItemString(output);
		// List<RecipeContainer> genRecipes = recipes.get(itemString);
		//
		// if (genRecipes != null)
		// {
		// genRecipes.add(new RecipeContainer(ingredients, output,
		// getTierIfStaged(recipe)));
		// }
		// else
		// {
		// genRecipes = new ArrayList<RecipeContainer>();
		// genRecipes.add(new RecipeContainer(ingredients, output,
		// getTierIfStaged(recipe)));
		// recipes.put(itemString, genRecipes);
		// }
		//
		// }
		//
		// recipeTree.add(recipe);
		// }
	}

	public static void addIngredientIfAlternative(Ingredient ing)
	{
		// if (ing.getItems().length > 1 && !(ing instanceof OreIngredient))
		// {
		// String ingString = getIngredientString(ing);
		// for (String s : getIngredientStrings(ing))
		// {
		// Set<String> set = alternativeIngredients.get(s);
		// if (set == null)
		// set = new HashSet<String>();
		//
		// set.add(ingString);
		// alternativeIngredients.put(s, set);
		// }
		// }
	}

	public static List<RecipeContainer> getRecipes(ItemStack stack)
	{
		String name = getItemString(stack);
		if (recipes.get(name) == null)
			return Collections.emptyList();
		else
			return recipes.get(name);
	}

	public static NonNullList<ItemStack> getPossibleItems(NonNullList<ItemStack> available, boolean removeAvailable)
	{
		RecipeTree subtree = recipeTree.getSubtree(available);

		return subtree.getPossibleStacks(removeAvailable);
	}

	public static NonNullList<ItemStack> getPossibleBlocks(NonNullList<ItemStack> available, boolean removeAvailable)
	{
		NonNullList<ItemStack> items = getPossibleItems(available, removeAvailable);

		NonNullList<ItemStack> blocks = NonNullList.create();

		blocks.addAll(items.stream().filter(stack -> stack.getItem() instanceof BlockItem).collect(Collectors.toList()));

		return blocks;
	}

	public static RecipeTree getRecipeTree(ItemStack requested, NonNullList<ItemStack> available)
	{
		RecipeTree subtree = recipeTree.getSubtree(available);
		return subtree.getRecipeTree(requested);
	}

	public static RecipeTree getSubTree(NonNullList<ItemStack> available)
	{
		RecipeTree subtree = recipeTree.getSubtree(available);
		return subtree;
	}

	public static String getTierIfStaged(IRecipe recipe)
	{
		//
		// if (ModList.get().isLoaded("recipestages"))
		// {
		// try
		// {
		// Class clazz =
		// Class.forName("com.blamejared.recipestages.recipes.RecipeStage");
		//
		// if (clazz.isInstance(recipe))
		// {
		// Method getTier = ReflectionHelper.findMethod(clazz, "getTier", null);
		// String tier = (String) getTier.invoke(recipe);
		//
		// return tier;
		// }
		//
		// }
		// catch (Exception e)
		// {
		// return "";
		// }
		// }

		return "";
	}

	public static String getItemString(ItemStack output)
	{
		// String nbt = output.hasTagCompound() ?
		// output.getTagCompound().toString() : "";
		// long nbtID = 0;
		//
		// if (NBTToIDCache.containsKey(nbt))
		// {
		// nbtID = NBTToIDCache.get(nbt);
		// }
		// else
		// {
		// NBTToIDCache.put(nbt, num);
		// IDToNBTCache.put(num, nbt);
		// num++;
		// }
		//
		// String outputString = output.getItem().getRegistryName().toString() +
		// "@" + output.getMetadata() + "$" + nbtID + ";";
		// return outputString;
		return "";
	}

	public static String[] getStackIngredientStrings(ItemStack output, boolean self)
	{
		// if (output.isEmpty())
		// return new String[0];
		//
		// int[] ores = OreDictionary.getOreIDs(output);
		//
		// List<String> oredict = new ArrayList<String>();
		// for (int i = 0; i < ores.length; i++)
		// {
		// OreIngredient ore = new
		// OreIngredient(OreDictionary.getOreName(ores[i]));
		// oredict.add(getIngredientString(ore));
		// }
		//
		// String itemString = getItemString(output);
		// if (self)
		// oredict.add(itemString);
		//
		// if (alternativeIngredients.containsKey(itemString))
		// {
		// oredict.addAll(alternativeIngredients.get(itemString));
		// }
		//
		// return oredict.toArray(new String[oredict.size()]);
		return new String[0];
	}

	public static String getStackIngredientString(ItemStack output, boolean self)
	{
		String[] str = getStackIngredientStrings(output, self);
		StringBuilder sb = new StringBuilder();
		sb.ensureCapacity(10000);
		for (String s : str)
		{
			sb.append(s);
		}

		return sb.toString();
	}

	public static String[] getIngredientStrings(Ingredient ing)
	{
		String[] strings = new String[ing.getItems().length];

		for (int i = 0; i < ing.getItems().length; i++)
		{
			strings[i] = getItemString(ing.getItems()[i]);
		}

		return strings;
	}

	public static String getIngredientString(Ingredient ing)
	{
		StringBuilder sb = new StringBuilder();
		for (ItemStack stack : ing.getItems())
			sb.append(getItemString(stack));

		return sb.toString();
	}

	public static ItemStack getItemFromString(String str)
	{
		// str = str.substring(0, str.length() - 1);
		//
		// int at = str.indexOf('@');
		// int hash = str.indexOf('#');
		// int dollar = str.indexOf('$');
		//
		// String name = str.substring(0, at);
		// int meta = Integer.parseInt(str.substring(at + 1, dollar));
		// String nbt = dollar == str.length() - 1 ? "" : str.substring(dollar +
		// 1, str.length());
		//
		// ItemStack stack = new ItemStack(Item.getByNameOrId(name), 1, meta);
		//
		// if (!nbt.isEmpty())
		// {
		// try
		// {
		// String nbtString = IDToNBTCache.get(Long.parseLong(nbt));
		//
		// CompoundNBT tag;
		//
		// tag = JsonToNBT.parseTag(nbtString);
		// stack.setTagCompound(tag);
		// }
		// catch (Exception e)
		// {
		// return stack;
		// }
		//
		// }
		//
		// return stack;
		return null;
	}
}
