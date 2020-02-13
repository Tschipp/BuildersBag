package tschipp.buildersbag.api;

import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractBagModule implements IBagModule
{

	protected boolean enabled = false;
	protected boolean expanded = false;
	protected String name;
	
	public AbstractBagModule(String name)
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
	public String getName()
	{
		return name;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		enabled = nbt.getBoolean("enabled");
		expanded = nbt.getBoolean("expanded");
		name = nbt.getString("name");
	}
	
	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("enabled", enabled);
		tag.setBoolean("expanded", expanded);
		tag.setString("name", name);
		return tag;
	}

}
