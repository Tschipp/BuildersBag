package tschipp.buildersbag.common.caps;

import net.minecraft.dispenser.IPosition;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import tschipp.buildersbag.api.IBagCap;

public class BagCapProvider implements ICapabilitySerializable {

	@CapabilityInject(IBagCap.class)
	public static final Capability<IBagCap> BAG_CAPABILITY = null;
	
	private IBagCap instance = BAG_CAPABILITY.getDefaultInstance();
	
	public BagCapProvider(int tier)
	{
		instance = new BagCap(tier);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == BAG_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == BAG_CAPABILITY ? BAG_CAPABILITY.cast(instance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		return BAG_CAPABILITY.getStorage().writeNBT(BAG_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		BAG_CAPABILITY.getStorage().readNBT(BAG_CAPABILITY, instance, null, nbt);
	}

}
