package tschipp.buildersbag.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import tschipp.buildersbag.client.gui.GuiBag;
import tschipp.buildersbag.common.helper.CapHelper;

public class BagGuiHandler implements IGuiHandler
{

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int offhand, int y, int z)
	{
		EnumHand hand = offhand == 1 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		ItemStack bag = player.getHeldItem(hand);
				
		return new ContainerBag(player, bag, hand);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int offhand, int y, int z)
	{
		EnumHand hand = offhand == 1 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		ItemStack bag = player.getHeldItem(hand);
				
		return new GuiBag(new ContainerBag(player, bag, hand), player, bag, hand);
	}

}
