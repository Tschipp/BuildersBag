package tschipp.buildersbag.client.gui;

import static tschipp.buildersbag.common.helper.InventoryHelper.getBagRows;
import static tschipp.buildersbag.common.helper.InventoryHelper.getSlotWidth;
import static tschipp.buildersbag.common.helper.InventoryHelper.getTotalHeight;
import static tschipp.buildersbag.common.helper.InventoryHelper.getTotalWidth;

import java.io.IOException;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.inventory.ContainerBag;
import tschipp.buildersbag.network.SyncItemStack;
import tschipp.buildersbag.network.SyncModuleState;

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
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		drawDefaultBackground();

		GlStateManager.pushMatrix();
		mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));

		drawBackground(0, 0, mainWidth, mainHeight);
		drawBackground(72, -32, 32, 32, 16);

		container.modules.forEach((module, triple) -> {
			int x = triple.getLeft();
			int y = triple.getMiddle();
			boolean right = triple.getRight();

			if (right)
			{
				drawHoverableBackgroundToggleable(x, y, 32, 32, mouseX, mouseY, module.isEnabled());

				RenderHelper.enableGUIStandardItemLighting();
				itemRender.renderItemAndEffectIntoGUI(module.getDisplayItem(), x + 8 + guiLeft, y + 8 + guiTop);
				RenderHelper.disableStandardItemLighting();

				mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));

				ItemStackHandler handler = module.getInventory();
				if (handler != null && module.isExpanded())
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

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int mousePosX = mouseX - guiLeft;
		int mousePosY = mouseY - guiTop;

		container.modules.forEach((module, triple) -> {

			int x = triple.getLeft();
			int y = triple.getMiddle();
			boolean right = triple.getRight();

			if (mousePosX >= x && mousePosX <= x + 32 && mousePosY >= y && mousePosY <= y + 32)
			{
				if (mouseButton == 0)
				{
					if (module.doesntUseOwnInventory())
						module.toggle();
					else
					{
						if (this.isShiftKeyDown())
							module.setExpanded(!module.isExpanded());
						else
							module.toggle();
					}

					container.update();
					sendUpdate(module);
					player.playSound(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0f);
				}
			}

		});

	}

	private void drawSlotBackgrounds()
	{
		for (Slot slot : container.inventorySlots)
		{
			if (slot.isEnabled())
				drawModalRectWithCustomSizedTexture(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 36, 0, 18, 18, 54, 33);
		}
	}

	private void drawHoverableBackground(int x, int y, int width, int height, int mouseX, int mouseY)
	{
		mouseX -= this.guiLeft;
		mouseY -= this.guiTop;

		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
			drawBackground(x, y, width, height, 4);
		else
			drawBackground(x, y, width, height, 0);
	}

	private void drawHoverableBackgroundToggleable(int x, int y, int width, int height, int mouseX, int mouseY, boolean enabled)
	{
		mouseX -= this.guiLeft;
		mouseY -= this.guiTop;

		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
			if (enabled)
				drawBackground(x, y, width, height, 20);
			else
				drawBackground(x, y, width, height, 24);
		else if (enabled)
			drawBackground(x, y, width, height, 8);
		else
			drawBackground(x, y, width, height, 12);

	}

	private void drawBackground(int x, int y, int width, int height)
	{
		drawBackground(x, y, width, height, 0);
	}

	private void drawBackground(int x, int y, int width, int height, int offset)
	{
		int drawWidth = (width - 8) / 4;
		int drawHeight = (height - 8) / 4;

		x += this.getGuiLeft();
		y += this.getGuiTop();

		int oldX = x;
		int oldY = y;

		// Draw First line
		drawModalRectWithCustomSizedTexture(x, y, 0, 0 + offset, 4, 4, 54, 33);
		x += 4;
		for (int i = 0; i < drawWidth; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 4, 0 + offset, 4, 4, 54, 33);
			x += 4;
		}
		drawModalRectWithCustomSizedTexture(x, y, 16, 0 + offset, 4, 4, 54, 33);

		// Draw main part
		x = oldX;
		y += 4;
		for (int i = 0; i < drawHeight; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 8, 0 + offset, 4, 4, 54, 33);
			x += 4;
			for (int j = 0; j < drawWidth; j++)
			{
				drawModalRectWithCustomSizedTexture(x, y, 12, 0 + offset, 4, 4, 54, 33);
				x += 4;
			}
			drawModalRectWithCustomSizedTexture(x, y, 20, 0 + offset, 4, 4, 54, 33);
			y += 4;
			x = oldX;
		}

		// Draw last line
		drawModalRectWithCustomSizedTexture(x, y, 32, 0 + offset, 4, 4, 54, 33);
		x += 4;
		for (int i = 0; i < drawWidth; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 28, 0 + offset, 4, 4, 54, 33);
			x += 4;
		}
		drawModalRectWithCustomSizedTexture(x, y, 24, 0 + offset, 4, 4, 54, 33);

	}

	private void sendUpdate(IBagModule module)
	{
		BuildersBag.network.sendToServer(new SyncModuleState(module.getName(), module));
	}

}
