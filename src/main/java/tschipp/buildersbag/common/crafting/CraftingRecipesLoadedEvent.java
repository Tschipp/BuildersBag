package tschipp.buildersbag.common.crafting;

import net.minecraft.item.crafting.RecipeManager;
import net.minecraftforge.eventbus.api.Event;

public class CraftingRecipesLoadedEvent extends Event
{

	public final RecipeManager manager;
	
	public CraftingRecipesLoadedEvent(RecipeManager recipeManager)
	{
		this.manager = recipeManager;
	}

}
