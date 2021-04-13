package tschipp.buildersbag.common.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import tschipp.buildersbag.client.gui.GuiBag;
import tschipp.buildersbag.compat.baubles.BaubleHelper;

public class BagGuiHandler implements IGuiHandler
{

	@Override
	public Object getServerGuiElement(int ID, PlayerEntity player, World world, int offhand, int y, int z)
	{
		if (ID == 0)
		{
			Hand hand = offhand == 1 ? Hand.MAIN_HAND : Hand.OFF_HAND;
			ItemStack bag = player.getHeldItem(hand);

			return new ContainerBag(player, bag, hand);
		}
		else
		{
			if(ModList.get().isLoaded("baubles"))
			{
				return new ContainerBag(player, BaubleHelper.getBauble(player, offhand), offhand);
			}
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, PlayerEntity player, World world, int offhand, int y, int z)
	{
		if (ID == 0)
		{
			Hand hand = offhand == 1 ? Hand.MAIN_HAND : Hand.OFF_HAND;
			ItemStack bag = player.getHeldItem(hand);

			return new GuiBag(new ContainerBag(player, bag, hand), player);
		}
		else
		{
			if(ModList.get().isLoaded("baubles"))
			{
				return new GuiBag(new ContainerBag(player, BaubleHelper.getBauble(player, offhand), offhand), player);
			}
		}
		
		return null;
	}

}
