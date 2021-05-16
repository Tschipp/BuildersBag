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

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderHelper;
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
	// private Hand hand;

	// private boolean isBauble = false;
	// private int slot = 0;

	private int mainWidth;
	private int mainHeight;
	private int leftOffset;

	private int compactButtonX;
	private int compactButtonY;

	// private static int TEX_HEIGHT = 60;
	// private static int TEX_WIDTH = 54;

	private Minecraft mc;

	public GuiBag(ContainerBag container, PlayerEntity player, ITextComponent name)
	{
		super(container, player.inventory, name);
		this.container = container;
		this.player = player;
		this.bag = container.bag;
		// this.hand = container.hand;
		//
		// this.isBauble = container.isBauble;
		// this.slot = container.slot;

		this.mainWidth = getTotalWidth();
		this.mainHeight = getTotalHeight(container.invSize);

		this.leftOffset = Math.max(InventoryHelper.getBagExtraLeft(CapHelper.getBagCap(bag)), InventoryHelper.getBagExtraRight(CapHelper.getBagCap(bag)));

		this.imageWidth = mainWidth + leftOffset * 2;
		this.imageHeight = mainHeight;

		this.compactButtonX = this.leftOffset + 72;
		this.compactButtonY = mainHeight;

		this.mc = Minecraft.getInstance();		
	}

	@Override
	protected void renderLabels(MatrixStack matrix, int mouseX, int mouseY)
	{
		
		int mX = mouseX - leftPos;
		int mY = mouseY - topPos;

		this.renderTooltip(matrix, mX, mY);
		
		if (mX >= this.compactButtonX && mX <= this.compactButtonX + 32 && mY >= this.compactButtonY && mY <= this.compactButtonY + 32)
		{
			List<ITextComponent> tooltip = new ArrayList<ITextComponent>();
			tooltip.add(new TranslationTextComponent("buildersbag.sort").withStyle(TextFormatting.BOLD));
			tooltip.add(new TranslationTextComponent("buildersbag.module.left").withStyle(TextFormatting.GOLD).append(new TranslationTextComponent("buildersbag.module.to")).append(new TranslationTextComponent("buildersbag.sort")));

			this.renderComponentTooltip(matrix, tooltip, mX, mY); // draw
																	// tooltip
		}

		container.modules.forEach((module, triple) -> {

			int x = triple.getLeft();
			int y = triple.getMiddle();
			if (mX >= x && mX <= x + 32 && mY >= y && mY <= y + 32)
			{
				List<ITextComponent> tooltip = new ArrayList<ITextComponent>();
				tooltip.add(new TranslationTextComponent("buildersbag.module." + module.getName()).withStyle(TextFormatting.BOLD));
				if (!module.doesntUseOwnInventory())
					tooltip.add(new TranslationTextComponent("buildersbag.module.shiftleft").withStyle(TextFormatting.GOLD).append(new TranslationTextComponent("buildersbag.module.toggle")));
				tooltip.add(new TranslationTextComponent("buildersbag.module.left").withStyle(TextFormatting.GOLD).append(new TranslationTextComponent("buildersbag.module.to")).append((module.isEnabled() ? new TranslationTextComponent("buildersbag.module.disable").withStyle(TextFormatting.RED) : new TranslationTextComponent("buildersbag.module.enable").withStyle(TextFormatting.GREEN))));

				this.renderComponentTooltip(matrix, tooltip, mX, mY); // draw
																		// tooltip
			}

		});
	}

	@Override
	protected void renderBg(MatrixStack matrix, float partialTicks, int mouseX, int mouseY)
	{
		renderBackground(matrix);

		matrix.pushPose();
		mc.getTextureManager().bind(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));

		drawBackground(matrix, 0 + leftOffset, 0, mainWidth, mainHeight); // Draw 
																// inventories
		drawBackground(matrix, 72 + leftOffset, -32, 32, 32, 16); // Draw golden slot

		drawHoverableBackground(matrix, this.compactButtonX, this.compactButtonY, 32, 32, mouseX, mouseY, 28, 32); // Draw
		// compact
		// button

		RenderHelper.turnBackOn();
		itemRenderer.renderAndDecorateItem(new ItemStack(Blocks.CHEST), this.compactButtonX + 8 + leftPos, this.compactButtonY + 8 + topPos);
		RenderHelper.turnOff();

		mc.getTextureManager().bind(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));

		container.modules.forEach((module, triple) -> {
			int x = triple.getLeft();
			int y = triple.getMiddle();
			boolean right = triple.getRight();

			drawHoverableBackgroundToggleable(matrix, x, y, 32, 32, mouseX, mouseY, module.isEnabled());

			RenderHelper.turnBackOn();
			itemRenderer.renderAndDecorateItem(module.getDisplayItem(), x + 8 + leftPos, y + 8 + topPos);
			RenderHelper.turnOff();

			mc.getTextureManager().bind(new ResourceLocation(BuildersBag.MODID + ":textures/gui/bag.png"));

			ItemStackHandler handler = module.getInventory();
			if (handler != null && module.isExpanded())
			{
				int slotWidth = getSlotWidth(handler.getSlots());
				drawBackground(matrix, x + (right ? 32 : -32), y, slotWidth, 32);
			}

		});

		drawSlotBackgrounds(matrix);

		this.font.draw(matrix, container.name.getString(), this.leftPos + 7 + this.leftOffset, this.topPos + 6, 4210752);
		this.font.draw(matrix, player.inventory.getDisplayName().getString(), this.leftPos + 7 + this.leftOffset, this.topPos + 6 + getBagRows(container.invSize) * 18 + 14, 4210752);

		matrix.popPose();		
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
		if (!super.mouseClicked(mouseX, mouseY, button))
			return true;

		double mousePosX = mouseX - leftPos;
		double mousePosY = mouseY - topPos;

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
		for (Slot slot : container.slots)
		{
			if (slot.isActive())
				blit(matrix, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, 18, 18, 36, 0, 18, 18, 54, 60); 
		}
	}

	private void drawHoverableBackground(MatrixStack matrix, int x, int y, int width, int height, int mouseX, int mouseY, int texVNormal, int texVHovered)
	{
		mouseX -= this.leftPos;
		mouseY -= this.topPos;

		if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
			drawBackground(matrix, x, y, width, height, texVHovered);
		else
			drawBackground(matrix, x, y, width, height, texVNormal);
	}

	private void drawHoverableBackgroundToggleable(MatrixStack matrix, int x, int y, int width, int height, int mouseX, int mouseY, boolean enabled)
	{
		mouseX -= this.leftPos;
		mouseY -= this.topPos;

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
		int drawWidth = (width - 8) / 4;
		int drawHeight = (height - 8) / 4;

		x += this.getGuiLeft();
		y += this.getGuiTop();

		int oldX = x;
		// Draw First line
		drawRect(matrix, x, y, 0, 0 + offset, 4, 4);
		x += 4;
		for (int i = 0; i < drawWidth; i++)
		{
			drawRect(matrix, x, y, 4, 0 + offset, 4, 4);
			x += 4;
		}
		drawRect(matrix, x, y, 16, 0 + offset, 4, 4);

		// Draw main part
		x = oldX;
		y += 4;
		for (int i = 0; i < drawHeight; i++)
		{
			drawRect(matrix, x, y, 8, 0 + offset, 4, 4);
			x += 4;
			for (int j = 0; j < drawWidth; j++)
			{
				drawRect(matrix, x, y, 12, 0 + offset, 4, 4);
				x += 4;
			}
			drawRect(matrix, x, y, 20, 0 + offset, 4, 4);
			y += 4;
			x = oldX;
		}

		// Draw last line
		drawRect(matrix, x, y, 32, 0 + offset, 4, 4);
		x += 4;
		for (int i = 0; i < drawWidth; i++)
		{
			drawRect(matrix, x, y, 28, 0 + offset, 4, 4);
			x += 4;
		}
		drawRect(matrix, x, y, 24, 0 + offset, 4, 4);
	}

	private void sendUpdate(IBagModule module)
	{
		BuildersBag.network.sendToServer(new SyncModuleStateServer(module.getName(), module));
	}

	private void drawRect(MatrixStack matrix, int x, int y, int u, int v, int width, int height)
	{
//		add(builder, matrix, x, y, u, v);
//		add(builder, matrix, x + width, y, u + width, v);
//		add(builder, matrix, x + width, y + height, u + width, v + height);
//		add(builder, matrix, x, y + height, u, v + height);
		blit(matrix, x, y, width, height, u, v, 4, 4, 54, 60);
	}

//	private static void add(IVertexBuilder builder, MatrixStack matrix, float x, float y, float u, float v)
//	{
//		builder
//		.vertex(matrix.last().pose(), x, y, 0) //pos
//		.color(1f, 1f, 1f, 1f) //color
//		.uv(u / 54.0f, v / 60.0f) //tex
//		.uv2(240, 240) //lightmap
//		.endVertex();
//	}

}
