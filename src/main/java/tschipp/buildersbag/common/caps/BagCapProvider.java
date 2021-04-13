package tschipp.buildersbag.common.caps;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import tschipp.buildersbag.api.IBagCap;

public class BagCapProvider implements ICapabilitySerializable<INBT> {

	@CapabilityInject(IBagCap.class)
	public static final Capability<IBagCap> BAG_CAPABILITY = null;
	
	private IBagCap instance = BAG_CAPABILITY.getDefaultInstance();
	
	public BagCapProvider(int tier)
	{
		this.instance.reInit(tier);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if(cap == BAG_CAPABILITY)
			return (LazyOptional<T>) LazyOptional.of(() -> instance);
		else
			return LazyOptional.empty();
	}

	@Override
	public INBT serializeNBT()
	{
		return BAG_CAPABILITY.getStorage().writeNBT(BAG_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(INBT nbt)
	{
		BAG_CAPABILITY.getStorage().readNBT(BAG_CAPABILITY, instance, null, nbt);		
	}

}
