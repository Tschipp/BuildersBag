//package tschipp.buildersbag.compat.crafttweaker;
//
//import net.minecraft.item.ItemStack;
//import tschipp.buildersbag.api.IBagCap;
//import tschipp.buildersbag.common.helper.CapHelper;
//import tschipp.buildersbag.common.item.BuildersBagItem;
//
//@ZenRegister
//@ZenClass("mods.buildersbag")
//public class BuildersBagCTIntegration
//{
//	@ZenProperty(value = "upgradeFunction")
//	public static IRecipeFunction bagUpgrader = (outputStack, inputs, info) -> TODO
//	{
//		
//		ICraftingInventory inv = info.getInventory();
//		
//		ItemStack bagToBeUpgraded = ItemStack.EMPTY;
//		
//		for (int i = 0; i < inv.getSize(); i++)
//		{
//			ItemStack inSlot = CraftTweakerMC.getItemStack(inv.getStack(i));
//			if (inSlot.getItem() instanceof BuildersBagItem)
//			{
//				bagToBeUpgraded = inSlot.copy();
//				break;
//			}
//		}
//
//		ItemStack result = CraftTweakerMC.getItemStack(outputStack);
//
//		if (bagToBeUpgraded.isEmpty())
//			return CraftTweakerMC.getIItemStack(result);
//
//		if (result.getItem() instanceof BuildersBagItem)
//		{
//			IBagCap old = CapHelper.getBagCap(bagToBeUpgraded);
//			IBagCap newCap = CapHelper.getBagCap(result);
//
//			newCap.transferDataFromCap(old);
//		}
//
//		return CraftTweakerMC.getIItemStack(result);
//	};
//
//	@ZenMethod
//	public static void addBagUpgradeRecipeShaped(String name, IItemStack output, IIngredient[][] ingredients, @Optional IRecipeAction action)
//	{
//		CraftTweakerAPI.recipes.addShapedMirrored(name, output, ingredients, bagUpgrader, action);
//	}
//
//	@ZenMethod
//	public static void addBagUpgradeRecipeShapeless(String name, IItemStack output, IIngredient[] ingredients, @Optional IRecipeAction action)
//	{
//		CraftTweakerAPI.recipes.addShapeless(name, output, ingredients, bagUpgrader, action);
//	}
//}
