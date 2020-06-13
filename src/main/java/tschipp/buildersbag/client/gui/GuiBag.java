package tschipp.buildersbag.client.gui;

import static tschipp.buildersbag.common.helper.InventoryHelper.getBagRows;
import static tschipp.buildersbag.common.helper.InventoryHelper.getSlotWidth;
import static tschipp.buildersbag.common.helper.InventoryHelper.getTotalHeight;
import static tschipp.buildersbag.common.helper.InventoryHelper.getTotalWidth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.inventory.ContainerBag;
import tschipp.buildersbag.network.server.CompactBagServer;
import tschipp.buildersbag.network.server.RequestBagUpdateServer;
import tschipp.buildersbag.network.server.SyncModuleStateServer;

public class GuiBag extends GuiContainer
{

	private ContainerBag container;
	private EntityPlayer player;
	private ItemStack bag;
	private EnumHand hand;

	private boolean isBauble = false;
	private int slot = 0;
	
	private int mainWidth;
	private int mainHeight;
	private int leftOffset;
	
	private int compactButtonX;
	private int compactButtonY;

	private static int TEX_HEIGHT = 60;
	private static int TEX_WIDTH = 54;
	
	private boolean isNew = true;
	
	public GuiBag(ContainerBag container, EntityPlayer player)
	{
		super(container);
		this.container = container;
		this.player = player;
		this.bag = container.bag;
		this.hand = container.hand;

		this.isBauble = container.isBauble;
		this.slot = container.slot;
		
		this.mainWidth = getTotalWidth() ;
		this.mainHeight = getTotalHeight(container.invSize);

		this.leftOffset = Math.max(InventoryHelper.getBagExtraLeft(CapHelper.getBagCap(bag)),InventoryHelper.getBagExtraRight(CapHelper.getBagCap(bag)));
		
		this.xSize = mainWidth + leftOffset * 2;
		this.ySize = mainHeight;
		
		this.compactButtonX = this.leftOffset + 72;
		this.compactButtonY = mainHeight;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
		
		if(isNew)
		{
			isNew = false;
			BuildersBag.network.sendToServer(new RequestBagUpdateServer(slot, isBauble));
		}
		
		int mX = mouseX - guiLeft;
		int mY = mouseY - guiTop;

		if(mX >= this.compactButtonX && mX <= this.compactButtonX + 32 && mY >= this.compactButtonY && mY <= this.compactButtonY + 32)
		{
			List<String> tooltip = new ArrayList<String>();
			tooltip.add(TextFormatting.BOLD + I18n.translateToLocal("buildersbag.sort"));
			tooltip.add(TextFormatting.GOLD + I18n.translateToLocal("buildersbag.module.left") + TextFormatting.RESET +  I18n.translateToLocal("buildersbag.module.to") + I18n.translateToLocal("buildersbag.sort"));
		
			this.drawHoveringText(tooltip, mouseX, mouseY);
		}
		
		container.modules.forEach((module, triple) -> {

			int x = triple.getLeft();
			int y = triple.getMiddle();
			boolean right = triple.getRight();

			if (mX >= x && mX <= x + 32 && mY >= y && mY <= y + 32)
			{
				List<String> tooltip = new ArrayList<String>();
				tooltip.add(TextFormatting.BOLD + I18n.translateToLocal("buildersbag.module." + module.getName()));
				if (!module.doesntUseOwnInventory())
					tooltip.add(TextFormatting.GOLD + I18n.translateToLocal("buildersbag.module.shiftleft") + TextFormatting.RESET + I18n.translateToLocal("buildersbag.module.toggle"));
				tooltip.add(TextFormatting.GOLD + I18n.translateToLocal("buildersbag.module.left") + TextFormatting.RESET +  I18n.translateToLocal("buildersbag.module.to") + (module.isEnabled() ? TextFormatting.RED + I18n.translateToLocal("buildersbag.module.disable") : TextFormatting.GREEN + I18n.translateToLocal("buildersbag.module.enable")));
				
				this.drawHoveringText(tooltip, mouseX, mouseY);
			}

		});
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		drawDefaultBackground();

		GlStateManager.pushMatrix();
		mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));

		drawBackground(0 + leftOffset, 0, mainWidth, mainHeight); //Draw inventories
		drawBackground(72  + leftOffset, -32, 32, 32, 16); //Draw golden slot
		
		drawHoverableBackground(this.compactButtonX, this.compactButtonY, 32, 32, mouseX, mouseY, 28, 32); //Draw compact button
	
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.renderItemAndEffectIntoGUI(new ItemStack(Blocks.CHEST), this.compactButtonX + 8 + guiLeft, this.compactButtonY + 8 + guiTop);
		RenderHelper.disableStandardItemLighting();

		mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));
		
		container.modules.forEach((module, triple) -> {
			int x = triple.getLeft();
			int y = triple.getMiddle();
			boolean right = triple.getRight();

			drawHoverableBackgroundToggleable(x, y, 32, 32, mouseX, mouseY, module.isEnabled());

			RenderHelper.enableGUIStandardItemLighting();
			itemRender.renderItemAndEffectIntoGUI(module.getDisplayItem(), x + 8 + guiLeft, y + 8 + guiTop);
			RenderHelper.disableStandardItemLighting();

			mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));

			ItemStackHandler handler = module.getInventory();
			if (handler != null && module.isExpanded())
			{
				int slotWidth = getSlotWidth(handler.getSlots());
				drawBackground(x + (right ? 32 : -32), y, slotWidth, 32);
			}

		});

		drawSlotBackgrounds();

		this.fontRenderer.drawString(container.name, this.guiLeft + 7 + this.leftOffset, this.guiTop + 6, 4210752);
		this.fontRenderer.drawString(player.inventory.getDisplayName().getUnformattedText(), this.guiLeft + 7 + this.leftOffset, this.guiTop + 6 + getBagRows(container.invSize) * 18 + 14, 4210752);

		GlStateManager.popMatrix();

	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		int mousePosX = mouseX - guiLeft;
		int mousePosY = mouseY - guiTop;

		if(mousePosX >= this.compactButtonX && mousePosX <= this.compactButtonX + 32 && mousePosY >= this.compactButtonY && mousePosY <= this.compactButtonY + 32)
		{
			player.playSound(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0f);
			BagHelper.compactStacks(CapHelper.getBagCap(bag), player);
			BuildersBag.network.sendToServer(new CompactBagServer(container.slot, container.isBauble));
			container.update();
		}
		
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
	
	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
//		BuildersBag.network.sendToServer(new SyncItemStack(bag, hand));
	}

	private void drawSlotBackgrounds()
	{
		for (Slot slot : container.inventorySlots)
		{
			if (slot.isEnabled())
				drawModalRectWithCustomSizedTexture(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 36, 0, 18, 18, TEX_WIDTH, TEX_HEIGHT);
		}
	}

	private void drawHoverableBackground(int x, int y, int width, int height, int mouseX, int mouseY, int texVNormal, int texVHovered)
	{
		mouseX -= this.guiLeft;
		mouseY -= this.guiTop;

		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
			drawBackground(x, y, width, height, texVHovered);
		else
			drawBackground(x, y, width, height, texVNormal);
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
		drawModalRectWithCustomSizedTexture(x, y, 0, 0 + offset, 4, 4, TEX_WIDTH, TEX_HEIGHT);
		x += 4;
		for (int i = 0; i < drawWidth; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 4, 0 + offset, 4, 4, TEX_WIDTH, TEX_HEIGHT);
			x += 4;
		}
		drawModalRectWithCustomSizedTexture(x, y, 16, 0 + offset, 4, 4, TEX_WIDTH, TEX_HEIGHT);

		// Draw main part
		x = oldX;
		y += 4;
		for (int i = 0; i < drawHeight; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 8, 0 + offset, 4, 4, TEX_WIDTH, TEX_HEIGHT);
			x += 4;
			for (int j = 0; j < drawWidth; j++)
			{
				drawModalRectWithCustomSizedTexture(x, y, 12, 0 + offset, 4, 4, TEX_WIDTH, TEX_HEIGHT);
				x += 4;
			}
			drawModalRectWithCustomSizedTexture(x, y, 20, 0 + offset, 4, 4, TEX_WIDTH, TEX_HEIGHT);
			y += 4;
			x = oldX;
		}

		// Draw last line
		drawModalRectWithCustomSizedTexture(x, y, 32, 0 + offset, 4, 4, TEX_WIDTH, TEX_HEIGHT);
		x += 4;
		for (int i = 0; i < drawWidth; i++)
		{
			drawModalRectWithCustomSizedTexture(x, y, 28, 0 + offset, 4, 4, TEX_WIDTH, TEX_HEIGHT);
			x += 4;
		}
		drawModalRectWithCustomSizedTexture(x, y, 24, 0 + offset, 4, 4, TEX_WIDTH, TEX_HEIGHT);

	}

	private void sendUpdate(IBagModule module)
	{
		BuildersBag.network.sendToServer(new SyncModuleStateServer(module.getName(), module));
	}

}
