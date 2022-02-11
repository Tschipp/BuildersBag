package tschipp.buildersbag.common.modules;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.api.BagComplex;
import tschipp.buildersbag.api.BagModuleType;
import tschipp.buildersbag.api.CreateableItemsManager;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.RequirementListener.ItemCreationRequirements;
import tschipp.buildersbag.common.BuildersBagRegistry;

public class SupplierModule extends AbstractBagModule
{

	private static final ItemStack DISPLAY = new ItemStack(Blocks.CHEST);
	
	@Override
	public boolean doesntUseOwnInventory()
	{
		return true;
	}

	@Override
	public ItemStackHandler getInventory()
	{
		return null;
	}

	@Override
	public ItemStack getDisplayItem()
	{
		return DISPLAY;
	}
	
	@Override
	public boolean isSupplier()
	{
		return true;
	}

	@Override
	public CreateableItemsManager getCreateableItemsManager()
	{
		return null;
	}

	@Override
	public BagModuleType<? extends IBagModule> getType()
	{
		return BuildersBagRegistry.MODULE_SUPPLIER;
	}

	@Override
	public int createItems(ItemCreationRequirements req, int count, BagComplex complex, PlayerEntity player)
	{
		return 0;
	}
}
