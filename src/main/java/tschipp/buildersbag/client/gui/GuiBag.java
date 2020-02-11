package tschipp.buildersbag.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import tschipp.buildersbag.common.inventory.ContainerBag;

public class GuiBag extends GuiContainer
{

	private ContainerBag container;
	private EntityPlayer player;
	private ItemStack bag;
	private EnumHand hand;
	
	public GuiBag(ContainerBag container, EntityPlayer player, ItemStack bag, EnumHand hand)
	{
		super(container);
		this.container = container;
		this.player = player;
		this.bag = bag;
		this.hand = hand;
		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		
	}

}
