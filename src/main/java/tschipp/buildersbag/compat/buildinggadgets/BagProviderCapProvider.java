package tschipp.buildersbag.compat.buildinggadgets;

import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.IItemHandler;

public class BagProviderCapProvider implements ICapabilitySerializable
{

	private IItemHandler instance = ITEM_HANDLER_CAPABILITY.getDefaultInstance();
	
	public BagProviderCapProvider(ItemStack bag)
	{
		this.instance = new BagProviderItemHandler(bag);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == ITEM_HANDLER_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		return capability == ITEM_HANDLER_CAPABILITY ? ITEM_HANDLER_CAPABILITY.cast(instance) : null;
	}

	@Override
	public NBTBase serializeNBT()
	{
		return new NBTTagCompound();
	}

	@Override
	public void deserializeNBT(NBTBase nbt)
	{
	}
	
	public static void setPlayer(EntityPlayer player, ItemStack bag)
	{
		if(bag.hasCapability(ITEM_HANDLER_CAPABILITY, null))
		{
			BagProviderItemHandler provider = (BagProviderItemHandler) bag.getCapability(ITEM_HANDLER_CAPABILITY, null);
			provider.setPlayer(player);
		}
	}

}
