package tschipp.buildersbag.client.gui;

import static tschipp.buildersbag.common.helper.InventoryHelper.getBagRows;
import static tschipp.buildersbag.common.helper.InventoryHelper.getSlotWidth;
import static tschipp.buildersbag.common.helper.InventoryHelper.getTotalHeight;
import static tschipp.buildersbag.common.helper.InventoryHelper.getTotalWidth;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
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

		this.mainWidth = getTotalWidth();
		this.mainHeight = getTotalHeight(container.invSize);

		this.xSize = mainWidth;
		this.ySize = mainHeight;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		drawDefaultBackground();

		GlStateManager.pushMatrix();
		mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));

		drawBackground(0, 0, mainWidth, mainHeight);
		
		container.modules.forEach((module, triple) -> {
			int x = triple.getLeft();
			int y = triple.getMiddle();
			boolean right = triple.getRight();
			
			if(right)
			{
				drawHoverableBackground(x, y, 32, 32, mouseX, mouseY);
				
				ItemStackHandler handler = module.getInventory();
				if(handler != null && module.isExpanded())
				{
					int slotWidth = getSlotWidth(handler.getSlots());
					drawBackground(x + 32, y, slotWidth, 32);
				}
			}

		});
		
		drawSlotBackgrounds();

		this.fontRenderer.drawString(container.name, this.guiLeft + 7, this.guiTop + 6, 4210752);
		this.fontRenderer.drawString(player.inventory.getDisplayName().getUnformattedText(), this.guiLeft + 7, this.guiTop + 6 + getBagRows(container.invSize) * 18 + 14, 4210752);

		GlStateManager.popMatrix();

	}

	private void drawSlotBackgrounds()
	{
		for (Slot slot : container.inventorySlots)
		{
			if (slot.isEnabled())
				drawModalRectWithCustomSizedTexture(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 0, 4, 18, 18, 54, 33);
		}
	}
	
	private void drawHoverableBackground(int x, int y, int width, int height, int mouseX, int mouseY)
	{
		if(mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
			drawBackground(x, y, width, height, true);
		else
			drawBackground(x, y, width, height, false);
	}
	
	private void drawBackground(int x, int y, int width, int height)
	{
		drawBackground(x, y, width, height, false);
	}

	private void drawBackground(int x, int y, int width, int height, boolean hover)
	{
		int drawWidth = (width - 8) / 4;
		int drawHeight = (height - 8) / 4;

		x += this.getGuiLeft();
		y += this.getGuiTop();

		int oldX = x;
		int oldY = y;

		int hoverBonus = hover ? 4 : 0;
		
		// Draw First line
		drawModalRectWithCustomSizedTexture(x, y, 0, 0 + hoverBonus, 4, 4, 54, 33);
		x += 4;
		for (int i = 0; i < drawWidth; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 4, 0 + hoverBonus, 4, 4, 54, 33);
			x += 4;
		}
		drawModalRectWithCustomSizedTexture(x, y, 16, 0 + hoverBonus, 4, 4, 54, 33);

		// Draw main part
		x = oldX;
		y += 4;
		for (int i = 0; i < drawHeight; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 8, 0 + hoverBonus, 4, 4, 54, 33);
			x += 4;
			for (int j = 0; j < drawWidth; j++)
			{
				drawModalRectWithCustomSizedTexture(x, y, 12, 0 + hoverBonus, 4, 4, 54, 33);
				x += 4;
			}
			drawModalRectWithCustomSizedTexture(x, y, 20, 0 + hoverBonus, 4, 4, 54, 33);
			y += 4;
			x = oldX;
		}

		// Draw last line
		drawModalRectWithCustomSizedTexture(x, y, 32, 0 + hoverBonus, 4, 4, 54, 33);
		x += 4;
		for (int i = 0; i < drawWidth; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 28, 0 + hoverBonus, 4, 4, 54, 33);
			x += 4;
		}
		drawModalRectWithCustomSizedTexture(x, y, 24, 0 + hoverBonus, 4, 4, 54, 33);

	}

}
