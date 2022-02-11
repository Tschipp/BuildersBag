package tschipp.buildersbag.api;

import net.minecraft.nbt.CompoundNBT;

public abstract class AbstractBagModule implements IBagModule
{

	protected boolean enabled = false;
	protected boolean expanded = false;
	
	@Override
	public void toggle()
	{
		enabled = !enabled;
	}
	
	@Override
	public boolean isEnabled()
	{
		return enabled;
	}
	
	@Override
	public boolean isExpanded()
	{
		return expanded;
	}
	
	@Override
	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		enabled = nbt.getBoolean("enabled");
		expanded = nbt.getBoolean("expanded");
	}
	
	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT tag = new CompoundNBT();
		tag.putBoolean("enabled", enabled);
		tag.putBoolean("expanded", expanded);
		tag.putString("name", this.getType().getRegistryName().toString());
		return tag;
	}

}
