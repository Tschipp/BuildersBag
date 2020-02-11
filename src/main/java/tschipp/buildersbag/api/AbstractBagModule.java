package tschipp.buildersbag.api;

import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractBagModule implements IBagModule
{

	protected boolean enabled = false;
	protected String name;
	
	protected AbstractBagModule(String name)
	{
		this.name = name;
	}
	
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
	public void deserializeNBT(NBTTagCompound nbt)
	{
		enabled = nbt.getBoolean("enabled");
		name = nbt.getString("name");
	}
	
	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("enabled", enabled);
		tag.setString("name", name);
		return tag;
	}

}
