package tschipp.buildersbag.common.inventory;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ToggleableSlot extends SlotItemHandler
{

	private boolean enabled = true;
	
	public ToggleableSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition)
	{
		super(itemHandler, index, xPosition, yPosition);
	}
	
	public ToggleableSlot setSlotEnabled(boolean bool)
	{
		this.enabled = bool;
		return this;
	}
	
	public ToggleableSlot toggle()
	{
		this.enabled = !this.enabled;
		return this;
	}
	
	public boolean isSlotEnabled()
	{
		return enabled;
	}

	@Override
	public boolean isEnabled()
	{
		return this.isSlotEnabled();
	}
	
}
