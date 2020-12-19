package tschipp.buildersbag.common.inventory;

import baubles.api.BaublesApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.IGuiHandler;
import tschipp.buildersbag.client.gui.GuiBag;
import tschipp.buildersbag.common.helper.CapHelper;

public class BagGuiHandler implements IGuiHandler
{

	@Override
	public Object getServerGuiElement(int ID, PlayerEntity player, World world, int offhand, int y, int z)
	{
		if (ID == 0)
		{
			EnumHand hand = offhand == 1 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			ItemStack bag = player.getHeldItem(hand);

			return new ContainerBag(player, bag, hand);
		}
		else
		{
			if(Loader.isModLoaded("baubles"))
			{
				return new ContainerBag(player, BaublesApi.getBaubles(player).getStackInSlot(offhand), offhand);
			}
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, PlayerEntity player, World world, int offhand, int y, int z)
	{
		if (ID == 0)
		{
			EnumHand hand = offhand == 1 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			ItemStack bag = player.getHeldItem(hand);

			return new GuiBag(new ContainerBag(player, bag, hand), player);
		}
		else
		{
			if(Loader.isModLoaded("baubles"))
			{
				return new GuiBag(new ContainerBag(player, BaublesApi.getBaubles(player).getStackInSlot(offhand), offhand), player);
			}
		}
		
		return null;
	}

}
