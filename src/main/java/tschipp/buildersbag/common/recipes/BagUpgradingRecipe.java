//package tschipp.buildersbag.common.recipes;
//
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//
//import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonSyntaxException;
//
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.crafting.IRecipe;
//import net.minecraft.item.crafting.Ingredient;
//import net.minecraft.util.NonNullList;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.common.crafting.CraftingHelper;
//import tschipp.buildersbag.api.IBagCap;
//import tschipp.buildersbag.common.helper.CapHelper;
//import tschipp.buildersbag.common.item.BuildersBagItem;
//
//public class BagUpgradingRecipe extends ShapedOreRecipe TODO
//{
//
//	public BagUpgradingRecipe(ResourceLocation group, ItemStack result, ShapedPrimer recipe)
//	{
//		super(group, result, recipe);
//	}
//
//	@Override
//	public ItemStack getCraftingResult(InventoryCrafting inv)
//	{
//		ItemStack bagToBeUpgraded = ItemStack.EMPTY;
//
//		for (int i = 0; i < inv.getSizeInventory(); i++)
//		{
//			ItemStack inSlot = inv.getStackInSlot(i);
//			if (inSlot.getItem() instanceof BuildersBagItem)
//			{
//				bagToBeUpgraded = inSlot.copy();
//				break;
//			}
//		}
//
//		ItemStack result = super.getCraftingResult(inv);
//
//		if (bagToBeUpgraded.isEmpty())
//			return result;
//
//		if (result.getItem() instanceof BuildersBagItem)
//		{
//			IBagCap old = CapHelper.getBagCap(bagToBeUpgraded);
//			IBagCap newCap = CapHelper.getBagCap(result);
//
//			newCap.transferDataFromCap(old);
//		}
//
//		return result;
//	}
//
//	public static class Factory implements IRecipeFactory
//	{
//
//		@Override
//		public IRecipe parse(JsonContext context, JsonObject json)
//		{
//			final String group = JsonUtils.getString(json, "group", "");
//
//			Map<Character, Ingredient> ingMap = Maps.newHashMap();
//			for (Entry<String, JsonElement> entry : JsonUtils.getJsonObject(json, "key").entrySet())
//			{
//				if (entry.getKey().length() != 1)
//					throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
//				if (" ".equals(entry.getKey()))
//					throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
//
//				ingMap.put(entry.getKey().toCharArray()[0], CraftingHelper.getIngredient(entry.getValue(), context));
//			}
//
//			ingMap.put(' ', Ingredient.EMPTY);
//
//			JsonArray patternJ = JsonUtils.getJsonArray(json, "pattern");
//
//			if (patternJ.size() == 0)
//				throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
//
//			String[] pattern = new String[patternJ.size()];
//			for (int x = 0; x < pattern.length; ++x)
//			{
//				String line = JsonUtils.getString(patternJ.get(x), "pattern[" + x + "]");
//				if (x > 0 && pattern[0].length() != line.length())
//					throw new JsonSyntaxException("Invalid pattern: each row must  be the same width");
//				pattern[x] = line;
//			}
//
//			ShapedPrimer primer = new ShapedPrimer();
//			primer.width = pattern[0].length();
//			primer.height = pattern.length;
//			primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
//			primer.input = NonNullList.withSize(primer.width * primer.height, Ingredient.EMPTY);
//
//			Set<Character> keys = Sets.newHashSet(ingMap.keySet());
//			keys.remove(' ');
//
//			int x = 0;
//			for (String line : pattern)
//			{
//				for (char chr : line.toCharArray())
//				{
//					Ingredient ing = ingMap.get(chr);
//					if (ing == null)
//						throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
//					primer.input.set(x++, ing);
//					keys.remove(chr);
//				}
//			}
//
//			if (!keys.isEmpty())
//				throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);
//
//			final ItemStack result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
//
//			return new BagUpgradingRecipe(new ResourceLocation(group), result, primer);
//		}
//
//	}
//
//}
