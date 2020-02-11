package tschipp.buildersbag.client.gui;

import static tschipp.buildersbag.common.helper.InventoryHelper.BOTTOM_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.HOTBAR_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.INV_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.TOP_OFFSET;
import static tschipp.buildersbag.common.helper.InventoryHelper.getBagRows;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.inventory.ContainerBag;

public class GuiBag extends GuiContainer
{

	private ContainerBag container;
	private EntityPlayer player;
	private ItemStack bag;
	private EnumHand hand;
	
	private int mainWidth;
	private int mainHeight;
	
	public GuiBag(ContainerBag container, EntityPlayer player, ItemStack bag, EnumHand hand)
	{
		super(container);
		this.container = container;
		this.player = player;
		this.bag = bag;
		this.hand = hand;
		
		this.xSize = 176;
		this.ySize = TOP_OFFSET + (getBagRows(container.invSize) + 4) * 18 + HOTBAR_OFFSET + INV_OFFSET + BOTTOM_OFFSET; 
	
		this.mainWidth = 176;
		this.mainHeight = TOP_OFFSET + (getBagRows(container.invSize) + 4) * 18 + HOTBAR_OFFSET + INV_OFFSET + BOTTOM_OFFSET; 
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		drawDefaultBackground();
		
		GlStateManager.pushMatrix();
		mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));
		
		drawBackground(0, 0, mainWidth, mainHeight);
		
		drawSlotBackgrounds();
		
        this.fontRenderer.drawString("Builder's Bag", this.guiLeft + 7, this.guiTop + 6, 4210752);
        this.fontRenderer.drawString("Inventory", this.guiLeft + 7, this.guiTop + 6 + getBagRows(container.invSize) * 18 + 14, 4210752);

		GlStateManager.popMatrix();
		
	}
	
	private void drawSlotBackgrounds()
	{
		for(Slot slot : container.inventorySlots)
		{
			drawModalRectWithCustomSizedTexture(this.guiLeft + slot.xPos-1, this.guiTop + slot.yPos-1, 0, 4, 18, 18, 36, 22);

		}
	}
	
	private void drawBackground(int x, int y, int width, int height)
	{
		int drawWidth = (width - 8)/4;
		int drawHeight = (height - 8)/4;

		x += this.getGuiLeft();
		y += this.getGuiTop();
		
		int oldX = x;
		int oldY = y;
		
		//Draw First line
		drawModalRectWithCustomSizedTexture(x, y, 0, 0, 4, 4, 36, 22);
		x += 4;
		for(int i = 0; i < drawWidth; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 4, 0, 4, 4, 36, 22);
			x += 4;
		}
		drawModalRectWithCustomSizedTexture(x, y, 16, 0, 4, 4, 36, 22);

		//Draw main part
		x = oldX;
		y += 4;
		for(int i = 0; i < drawHeight; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 8, 0, 4, 4, 36, 22);
			x += 4;
			for(int j = 0; j < drawWidth; j++)
			{
				drawModalRectWithCustomSizedTexture(x, y, 12, 0, 4, 4, 36, 22);
				x += 4;
			}
			drawModalRectWithCustomSizedTexture(x, y, 20, 0, 4, 4, 36, 22);
			y += 4;
			x = oldX;
		}
		
		//Draw last line
		drawModalRectWithCustomSizedTexture(x, y, 32, 0, 4, 4, 36, 22);
		x += 4;
		for(int i = 0; i < drawWidth; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 28, 0, 4, 4, 36, 22);
			x += 4;
		}
		drawModalRectWithCustomSizedTexture(x, y, 24, 0, 4, 4, 36, 22);
		
	}

}
