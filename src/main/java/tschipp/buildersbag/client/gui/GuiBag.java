package tschipp.buildersbag.client.gui;

import static tschipp.buildersbag.common.helper.InventoryHelper.getBagRows;
import static tschipp.buildersbag.common.helper.InventoryHelper.getSlotWidth;
import static tschipp.buildersbag.common.helper.InventoryHelper.getTotalHeight;
import static tschipp.buildersbag.common.helper.InventoryHelper.getTotalWidth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Triple;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.inventory.ContainerBag;
import tschipp.buildersbag.network.server.CompactBagServer;
import tschipp.buildersbag.network.server.SyncModuleStateServer;

public class GuiBag extends ContainerScreen<ContainerBag>
{

	private ContainerBag container;
	private PlayerEntity player;
	private ItemStack bag;
//	private Hand hand;

//	private boolean isBauble = false;
//	private int slot = 0;

	private int mainWidth;
	private int mainHeight;
	private int leftOffset;

	private int compactButtonX;
	private int compactButtonY;

//	private static int TEX_HEIGHT = 60;
//	private static int TEX_WIDTH = 54;

	private Minecraft mc;

	public GuiBag(ContainerBag container, PlayerEntity player, ITextComponent name)
	{
		super(container, player.inventory, name);
		this.container = container;
		this.player = player;
		this.bag = container.bag;
//		this.hand = container.hand;
//
//		this.isBauble = container.isBauble;
//		this.slot = container.slot;

		this.mainWidth = getTotalWidth();
		this.mainHeight = getTotalHeight(container.invSize);

		this.leftOffset = Math.max(InventoryHelper.getBagExtraLeft(CapHelper.getBagCap(bag)), InventoryHelper.getBagExtraRight(CapHelper.getBagCap(bag)));

		this.xSize = mainWidth + leftOffset * 2;
		this.ySize = mainHeight;

		this.compactButtonX = this.leftOffset + 72;
		this.compactButtonY = mainHeight;

		this.mc = Minecraft.getInstance();
	}

	// @Override
	// public void drawScreen(int mouseX, int mouseY, float partialTicks)
	// {
	// super.drawScreen(mouseX, mouseY, partialTicks);
	// this.renderHoveredToolTip(mouseX, mouseY);
	//
	//// if(isNew)
	//// {
	//// isNew = false;
	//// BuildersBag.network.sendToServer(new RequestBagUpdateServer(slot,
	// isBauble));
	//// }
	//
	// int mX = mouseX - guiLeft;
	// int mY = mouseY - guiTop;
	//
	// if(mX >= this.compactButtonX && mX <= this.compactButtonX + 32 && mY >=
	// this.compactButtonY && mY <= this.compactButtonY + 32)
	// {
	// List<String> tooltip = new ArrayList<String>();
	// tooltip.add(TextFormatting.BOLD +
	// I18n.translateToLocal("buildersbag.sort"));
	// tooltip.add(TextFormatting.GOLD +
	// I18n.translateToLocal("buildersbag.module.left") + TextFormatting.RESET +
	// I18n.translateToLocal("buildersbag.module.to") +
	// I18n.translateToLocal("buildersbag.sort"));
	//
	// this.drawHoveringText(tooltip, mouseX, mouseY);
	// }
	//
	// container.modules.forEach((module, triple) -> {
	//
	// int x = triple.getLeft();
	// int y = triple.getMiddle();
	// boolean right = triple.getRight();
	//
	// if (mX >= x && mX <= x + 32 && mY >= y && mY <= y + 32)
	// {
	// List<String> tooltip = new ArrayList<String>();
	// tooltip.add(TextFormatting.BOLD +
	// I18n.translateToLocal("buildersbag.module." + module.getName()));
	// if (!module.doesntUseOwnInventory())
	// tooltip.add(TextFormatting.GOLD +
	// I18n.translateToLocal("buildersbag.module.shiftleft") +
	// TextFormatting.RESET +
	// I18n.translateToLocal("buildersbag.module.toggle"));
	// tooltip.add(TextFormatting.GOLD +
	// I18n.translateToLocal("buildersbag.module.left") + TextFormatting.RESET +
	// I18n.translateToLocal("buildersbag.module.to") + (module.isEnabled() ?
	// TextFormatting.RED + I18n.translateToLocal("buildersbag.module.disable")
	// : TextFormatting.GREEN +
	// I18n.translateToLocal("buildersbag.module.enable")));
	//
	// this.drawHoveringText(tooltip, mouseX, mouseY);
	// }
	//
	// });
	// }

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(matrix, mouseX, mouseY);

		int mX = mouseX - guiLeft;
		int mY = mouseY - guiTop;

		if (mX >= this.compactButtonX && mX <= this.compactButtonX + 32 && mY >= this.compactButtonY && mY <= this.compactButtonY + 32)
		{
			List<ITextComponent> tooltip = new ArrayList<ITextComponent>();
			tooltip.add(new TranslationTextComponent("buildersbag.sort").mergeStyle(TextFormatting.BOLD));
			tooltip.add(new TranslationTextComponent("buildersbag.module.left").mergeStyle(TextFormatting.GOLD).append(new TranslationTextComponent("buildersbag.module.to")).append(new TranslationTextComponent("buildersbag.sort")));

			this.func_243308_b(matrix, tooltip, mouseX, mouseY); // draw tooltip
		}

		container.modules.forEach((module, triple) -> {

			int x = triple.getLeft();
			int y = triple.getMiddle();
			if (mX >= x && mX <= x + 32 && mY >= y && mY <= y + 32)
			{
				List<ITextComponent> tooltip = new ArrayList<ITextComponent>();
				tooltip.add(new TranslationTextComponent("buildersbag.module." + module.getName()).mergeStyle(TextFormatting.BOLD));
				if (!module.doesntUseOwnInventory())
					tooltip.add(new TranslationTextComponent("buildersbag.module.shiftleft").mergeStyle(TextFormatting.GOLD).append(new TranslationTextComponent("buildersbag.module.toggle")));
				tooltip.add(new TranslationTextComponent("buildersbag.module.left").mergeStyle(TextFormatting.GOLD).append(new TranslationTextComponent("buildersbag.module.to")).append((module.isEnabled() ? new TranslationTextComponent("buildersbag.module.disable").mergeStyle(TextFormatting.RED) : new TranslationTextComponent("buildersbag.module.enable").mergeStyle(TextFormatting.GREEN))));

				this.func_243308_b(matrix, tooltip, mouseX, mouseY); // draw
																		// tooltip
			}

		});
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		renderBackground(matrix);

		matrix.push();
		mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));

		drawBackground(matrix, 0 + leftOffset, 0, mainWidth, mainHeight); // Draw
																	// inventories
		drawBackground(matrix, 72 + leftOffset, -32, 32, 32, 16); // Draw golden slot

		drawHoverableBackground(matrix, this.compactButtonX, this.compactButtonY, 32, 32, mouseX, mouseY, 28, 32); // Draw
																											// compact
																											// button

		RenderHelper.enableStandardItemLighting();
		itemRenderer.renderItemAndEffectIntoGUI(new ItemStack(Blocks.CHEST), this.compactButtonX + 8 + guiLeft, this.compactButtonY + 8 + guiTop);
		RenderHelper.disableStandardItemLighting();

		mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));

		container.modules.forEach((module, triple) -> {
			int x = triple.getLeft();
			int y = triple.getMiddle();
			boolean right = triple.getRight();

			drawHoverableBackgroundToggleable(matrix, x, y, 32, 32, mouseX, mouseY, module.isEnabled());

			RenderHelper.enableStandardItemLighting();
			itemRenderer.renderItemAndEffectIntoGUI(module.getDisplayItem(), x + 8 + guiLeft, y + 8 + guiTop);
			RenderHelper.disableStandardItemLighting();

			mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));

			ItemStackHandler handler = module.getInventory();
			if (handler != null && module.isExpanded())
			{
				int slotWidth = getSlotWidth(handler.getSlots());
				drawBackground(matrix, x + (right ? 32 : -32), y, slotWidth, 32);
			}

		});

		drawSlotBackgrounds(matrix);
		
		this.font.drawString(matrix, container.name.getString(), this.guiLeft + 7 + this.leftOffset, this.guiTop + 6, 4210752);
		this.font.drawString(matrix, player.inventory.getDisplayName().getString(), this.guiLeft + 7 + this.leftOffset, this.guiTop + 6 + getBagRows(container.invSize) * 18 + 14, 4210752);

		matrix.pop();
	}

	// @Override
	// protected void drawGuiContainerBackgroundLayer(float partialTicks, int
	// mouseX, int mouseY)
	// {
	// drawDefaultBackground();
	//
	// GlStateManager.pushMatrix();
	// mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID
	// + ":textures/gui/bag.png"));
	//
	// drawBackground(0 + leftOffset, 0, mainWidth, mainHeight); //Draw
	// inventories
	// drawBackground(72 + leftOffset, -32, 32, 32, 16); //Draw golden slot
	//
	// drawHoverableBackground(this.compactButtonX, this.compactButtonY, 32, 32,
	// mouseX, mouseY, 28, 32); //Draw compact button
	//
	// RenderHelper.enableGUIStandardItemLighting();
	// itemRender.renderItemAndEffectIntoGUI(new ItemStack(Blocks.CHEST),
	// this.compactButtonX + 8 + guiLeft, this.compactButtonY + 8 + guiTop);
	// RenderHelper.disableStandardItemLighting();
	//
	// mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID
	// + ":textures/gui/bag.png"));
	//
	// container.modules.forEach((module, triple) -> {
	// int x = triple.getLeft();
	// int y = triple.getMiddle();
	// boolean right = triple.getRight();
	//
	// drawHoverableBackgroundToggleable(x, y, 32, 32, mouseX, mouseY,
	// module.isEnabled());
	//
	// RenderHelper.enableGUIStandardItemLighting();
	// itemRender.renderItemAndEffectIntoGUI(module.getDisplayItem(), x + 8 +
	// guiLeft, y + 8 + guiTop);
	// RenderHelper.disableStandardItemLighting();
	//
	// mc.getTextureManager().bindTexture(new ResourceLocation(BuildersBag.MODID
	// + ":textures/gui/bag.png"));
	//
	// ItemStackHandler handler = module.getInventory();
	// if (handler != null && module.isExpanded())
	// {
	// int slotWidth = getSlotWidth(handler.getSlots());
	// drawBackground(x + (right ? 32 : -32), y, slotWidth, 32);
	// }
	//
	// });
	//
	// drawSlotBackgrounds();
	//
	// this.fontRenderer.drawString(container.name, this.guiLeft + 7 +
	// this.leftOffset, this.guiTop + 6, 4210752);
	// this.fontRenderer.drawString(player.inventory.getDisplayName().getUnformattedText(),
	// this.guiLeft + 7 + this.leftOffset, this.guiTop + 6 +
	// getBagRows(container.invSize) * 18 + 14, 4210752);
	//
	// GlStateManager.popMatrix();
	//
	// }

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if (super.mouseClicked(mouseX, mouseY, button))
			return true;

		double mousePosX = mouseX - guiLeft;
		double mousePosY = mouseY - guiTop;

		if (mousePosX >= this.compactButtonX && mousePosX <= this.compactButtonX + 32 && mousePosY >= this.compactButtonY && mousePosY <= this.compactButtonY + 32)
		{
			player.playSound(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0f);
			BagHelper.compactStacks(CapHelper.getBagCap(bag), player);
			BuildersBag.network.sendToServer(new CompactBagServer(container.slot, container.isBauble));
			container.update();
			return true;
		}

		for (Entry<IBagModule, Triple<Integer, Integer, Boolean>> entry : container.modules.entrySet())
		{
			Triple<Integer, Integer, Boolean> triple = entry.getValue();
			IBagModule module = entry.getKey();

			int x = triple.getLeft();
			int y = triple.getMiddle();
			if (mousePosX >= x && mousePosX <= x + 32 && mousePosY >= y && mousePosY <= y + 32)
			{
				if (button == 0)
				{
					if (module.doesntUseOwnInventory())
						module.toggle();
					else
					{
						if (Screen.hasShiftDown())
							module.setExpanded(!module.isExpanded());
						else
							module.toggle();
					}

					container.update();
					sendUpdate(module);
					player.playSound(SoundEvents.UI_BUTTON_CLICK, 1.0F, 1.0f);
					return true;
				}
			}

		}

		return false;
	}

	// @Override
	// public void onGuiClosed()
	// {
	// super.onGuiClosed();
	// BuildersBag.network.sendToServer(new SyncItemStack(bag, hand));
	// }

	private void drawSlotBackgrounds(MatrixStack matrix)
	{
		Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		IVertexBuilder builder = buffer.getBuffer(RenderType.getCutout());
		
		for (Slot slot : container.inventorySlots)
		{
			if (slot.isEnabled())
				drawRect(builder, matrix, this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 36, 0, 18, 18);
		}
		
		buffer.finish();
	}

	private void drawHoverableBackground(MatrixStack matrix, int x, int y, int width, int height, int mouseX, int mouseY, int texVNormal, int texVHovered)
	{
		mouseX -= this.guiLeft;
		mouseY -= this.guiTop;

		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
			drawBackground(matrix, x, y, width, height, texVHovered);
		else
			drawBackground(matrix, x, y, width, height, texVNormal);
	}

	private void drawHoverableBackgroundToggleable(MatrixStack matrix, int x, int y, int width, int height, int mouseX, int mouseY, boolean enabled)
	{
		mouseX -= this.guiLeft;
		mouseY -= this.guiTop;

		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
			if (enabled)
				drawBackground(matrix, x, y, width, height, 20);
			else
				drawBackground(matrix, x, y, width, height, 24);
		else if (enabled)
			drawBackground(matrix, x, y, width, height, 8);
		else
			drawBackground(matrix, x, y, width, height, 12);

	}

	private void drawBackground(MatrixStack matrix, int x, int y, int width, int height)
	{
		drawBackground(matrix, x, y, width, height, 0);
	}

	private void drawBackground(MatrixStack matrix, int x, int y, int width, int height, int offset)
	{
		Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
		IVertexBuilder builder = buffer.getBuffer(RenderType.getCutout());
		
		int drawWidth = (width - 8) / 4;
		int drawHeight = (height - 8) / 4;

		x += this.getGuiLeft();
		y += this.getGuiTop();

		int oldX = x;
		// Draw First line
		drawRect(builder, matrix, x, y, 0, 0 + offset, 4, 4);
		x += 4;
		for (int i = 0; i < drawWidth; i++)
		{
			drawRect(builder, matrix,x, y, 4, 0 + offset, 4, 4);
			x += 4;
		}
		drawRect(builder, matrix,x, y, 16, 0 + offset, 4, 4);

		// Draw main part
		x = oldX;
		y += 4;
		for (int i = 0; i < drawHeight; i++)
		{
			drawRect(builder, matrix,x, y, 8, 0 + offset, 4, 4);
			x += 4;
			for (int j = 0; j < drawWidth; j++)
			{
				drawRect(builder, matrix,x, y, 12, 0 + offset, 4, 4);
				x += 4;
			}
			drawRect(builder, matrix,x, y, 20, 0 + offset, 4, 4);
			y += 4;
			x = oldX;
		}

		// Draw last line
		drawRect(builder, matrix,x, y, 32, 0 + offset, 4, 4);
		x += 4;
		for (int i = 0; i < drawWidth; i++)
		{
			drawRect(builder, matrix,x, y, 28, 0 + offset, 4, 4);
			x += 4;
		}
		drawRect(builder, matrix,x, y, 24, 0 + offset, 4, 4);

		buffer.finish();
	}

	private void sendUpdate(IBagModule module)
	{
		BuildersBag.network.sendToServer(new SyncModuleStateServer(module.getName(), module));
	}
	
	private void drawRect(IVertexBuilder builder, MatrixStack matrix, int x, int y, int u, int v, int width, int height)
	{
		add(builder, matrix, x, y, u, v);
		add(builder, matrix, x + width, y, u + width, v);
		add(builder, matrix, x + width, y + height, u + width, v + height);
		add(builder, matrix, x, y + height, u, v + height);
	}
	
	private static void add(IVertexBuilder builder, MatrixStack matrix, float x, float y, float u, float v)
	{
		builder.pos(matrix.getLast().getMatrix(), x, y, 0)
			   .tex(u, v)
			   .lightmap(240, 240)
			   .normal(1,0,0)
			   .endVertex();
	}

}
