package tschipp.buildersbag.compat.baubles;

import com.lazy.baubles.api.BaublesAPI;
import com.lazy.baubles.api.cap.IBaublesItemHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class BaubleHelper
{
	public static ItemStack getBauble(PlayerEntity player, int slot)
	{
		IBaublesItemHandler handler =  BaublesAPI.getBaublesHandler(player).orElse(null);
		if(handler == null)
			return ItemStack.EMPTY;
		
		return handler.getStackInSlot(slot);
	}
	
	public static IBaublesItemHandler getBaubles(PlayerEntity player)
	{
		return  BaublesAPI.getBaublesHandler(player).orElse(null);
	}
}
