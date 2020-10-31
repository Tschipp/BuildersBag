package tschipp.buildersbag.client.selectionwheel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.vecmath.Vector2d;

import org.lwjgl.input.Mouse;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.client.rendering.BagRenderHelper;
import tschipp.buildersbag.common.config.BuildersBagConfig;
import tschipp.buildersbag.common.data.Tuple;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.helper.ItemHelper;
import tschipp.buildersbag.network.server.ModifyPaletteServer;
import tschipp.buildersbag.network.server.SetSelectedBlockServer;

public class SelectionWheel
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(BuildersBag.MODID, "textures/gui/blockselector.png");
	private static final ResourceLocation VANILLA = new ResourceLocation("textures/gui/icons.png");
	private static Minecraft mc = Minecraft.getMinecraft();
	private static RenderItem itemRender = mc.getRenderItem();

	private static final int WHEEL_WIDTH = 238, WHEEL_HEIGHT = 235;
	private static final int ARROW_WIDTH = 50, ARROW_HEIGHT = 54;
	private static final double SCALE = 0.6;

	private static ItemStack bag;
	private static IBagCap cap;
	public static boolean open = false;
	public static boolean shouldDraw = false;
	private static boolean isAnimating = false;
	private static double easeOpen = 0.0;
	private static double easeClose = 0.0;
	private static double ease = 0.0;
	private static final Stopwatch timer = Stopwatch.createUnstarted();
	private static ScaledResolution res;

	private static String activeFilter = "";
	private static int activePage = 0;
	private static List<SelectionWheelLogic.SelectionPage> activePageList = new ArrayList<>();
	private static List<SelectionWheelLogic.SelectionPage> unfilteredPageList = new ArrayList<>();
	private static Map<String, SelectionFilter> filters = new HashMap<>();

	private static Map<String, String> activeFilterCache = new HashMap<>();
	private static Map<String, Integer> activePageCache = new HashMap<>();

	private static List<Tuple<Integer, Integer>> rightPoints = getArrowPoints(true);
	private static List<Tuple<Integer, Integer>> leftPoints = getArrowPoints(false);
	private static List<Tuple<Integer, Integer>> wheelCorners = getCornerPoints();
	private static List<Tuple<Integer, Integer>> selectedCorners = getHexagonPoints(new Tuple(0, 178));
	private static List<List<Tuple<Integer, Integer>>> filterPoints = new ArrayList<>();
	private static List<String> filterNames = new ArrayList<>();

	public static void setBag(ItemStack stack)
	{
		rightPoints = getArrowPoints(true);
		leftPoints = getArrowPoints(false);

		int cachedPage = 0;
		String cachedFilter = "";

		if (cap != null && CapHelper.areCapsEqual(cap, CapHelper.getBagCap(stack)))
		{
			cachedFilter = activeFilterCache.getOrDefault(cap.getUUID(), "");
			cachedPage = activePageCache.getOrDefault(cap.getUUID(), 0);
		}

		bag = stack;
		cap = CapHelper.getBagCap(stack);
		activePageList.clear();
		unfilteredPageList.clear();
		filterPoints.clear();
		filterNames.clear();

		// SET UP FILTERS

		List<ItemStack> provideableStacks = new ArrayList<ItemStack>();

		filters.clear();
		for (IBagModule module : cap.getModules())
		{
			if (module.isEnabled())
			{
				List<ItemStack> provideable = module.getPossibleStacks(cap, mc.player).stream().filter(s -> s.getItem() instanceof ItemBlock).collect(Collectors.toList());
				if (!provideable.isEmpty())
				{
					List<SelectionWheelLogic.SelectionPage> filterpages = createPages(provideable);
					SelectionFilter filt = new SelectionFilter(filterpages, module.getName());
					filters.put(module.getName(), filt);
					provideableStacks.addAll(provideable);
				}
			}
		}
		List<ItemStack> palette = cap.getPalette();
		List<SelectionWheelLogic.SelectionPage> palettePages = createPages(palette);
		SelectionFilter filt = new SelectionFilter(palettePages, "palette");
		filters.put("palette", filt);

		int filtercount = filters.size();
		for (int i = 0; i < filters.size(); i++)
		{
			int tilePos = i <= filtercount / 2 ? (filtercount / 2 - i) * -66 : (i - filtercount / 2) * 66;
			int offX = filtercount % 2 == 0 ? 34 : 0;
			List<Tuple<Integer, Integer>> corners = getHexagonPoints(new Tuple(offX + tilePos, -140 - 40));
			filterPoints.add(corners);
		}

		filterNames.addAll(filters.keySet());
		filterNames.remove("palette");
		Collections.sort(filterNames);
		filterNames.add(0, "palette");

		////

		provideableStacks.addAll(InventoryHelper.getInventoryStacks(cap, mc.player).stream().filter(s -> s.getItem() instanceof ItemBlock).collect(Collectors.toList()));
		ItemHelper.removeDuplicates(provideableStacks);

		unfilteredPageList.addAll(createPages(provideableStacks));

		if (filters.containsKey(cachedFilter))
		{
			activeFilter = cachedFilter;
			activePageList.addAll(filters.get(activeFilter).pages);
		}
		else
		{
			activeFilter = "";
			activePageList.addAll(unfilteredPageList);
		}

		if (cachedPage < activePageList.size())
			activePage = cachedPage;
		else
			activePage = 0;
	}

	public static void startAnimation()
	{
		open = true;
		shouldDraw = true;
		if (timer.isRunning())
			timer.reset();
		timer.start();
		isAnimating = true;
	}

	public static void finishAnimation()
	{
		open = false;
		if (timer.isRunning())
			timer.reset();
		timer.start();
		isAnimating = true;
	}

	public static void draw(float partialTicks, ScaledResolution res)
	{
		SelectionWheel.res = res;

		double time = isAnimating ? timer.elapsed(TimeUnit.MILLISECONDS) / 1000.0 : 1.0;
		if (time > 1.0)
		{
			time = 1.0;
			timer.reset();
			isAnimating = false;

			if (!open)
			{
				shouldDraw = false;
				easeOpen = 0.0;
			}
			else
				easeClose = 0.0;
		}

		if (open)
			easeOpen = BagRenderHelper.easeOutElastic(0, 1, time);
		else
			easeClose = BagRenderHelper.easeOutCubic(1, 0, Math.min(time * 4, 1));

		ease = open ? easeOpen : easeClose;

		mc.getTextureManager().bindTexture(TEXTURE);
		GlStateManager.pushMatrix();
		transform(partialTicks, res);

		double renderScale = SCALE * ease;
		int mouseX = (int) ((Mouse.getX() / res.getScaleFactor() - res.getScaledWidth() / 2) / renderScale);
		int mouseY = (int) ((Mouse.getY() / res.getScaleFactor() - res.getScaledHeight() / 2) / renderScale);

		if (activePageList.size() > 1)
			drawPageButtons(partialTicks, mouseX, mouseY);

		drawCategoryButtons(partialTicks, mouseX, mouseY);
		drawSelectionWheel(partialTicks, mouseX, mouseY);

		GlStateManager.popMatrix();
		mc.getTextureManager().bindTexture(VANILLA);

	}

	public static void onClick(boolean isRight)
	{
		double renderScale = SCALE * ease;
		ScaledResolution res = new ScaledResolution(mc);
		int mouseX = (int) ((Mouse.getX() / res.getScaleFactor() - res.getScaledWidth() / 2) / renderScale);
		int mouseY = (int) ((Mouse.getY() / res.getScaleFactor() - res.getScaledHeight() / 2) / renderScale);

		boolean rightArrow = isInPolygon(mouseX, mouseY, rightPoints.subList(0, 4)) || isInTriangle(mouseX, mouseY, rightPoints.subList(4, 7));
		boolean leftArrow = isInPolygon(mouseX, mouseY, leftPoints.subList(0, 4)) || isInTriangle(mouseX, mouseY, leftPoints.subList(4, 7));

		if (rightArrow)
		{
			activePage++;
			activePage = activePage % activePageList.size();
			activePageCache.put(cap.getUUID(), activePage);
			mc.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2f, 1f);
			return;
		}

		if (leftArrow)
		{
			activePage--;
			if (activePage < 0)
				activePage = activePageList.size() - 1;
			activePageCache.put(cap.getUUID(), activePage);
			mc.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2f, 1f);
			return;
		}

		if(isInPolygon(mouseX, mouseY, getHexagonPoints(new Tuple(0, 178))))
		{
			if (!isRight)
			{
				cap.getSelectedInventory().setStackInSlot(0, ItemStack.EMPTY);
				BuildersBag.network.sendToServer(new SetSelectedBlockServer(cap.getUUID(), ItemStack.EMPTY));
				if (BuildersBagConfig.Settings.playPickBlockSounds)
					mc.player.playSound(SoundEvents.BLOCK_NOTE_HAT, 0.5f, 0.1f);
			}
			return;
		}
		
		SelectionWheelLogic.SelectionPage page = activePageList.size() > 0 ? activePageList.get(activePage) : null;

		for (int i = 0; i < wheelCorners.size(); i++)
		{
			if (isInTriangle(mouseX, mouseY, new Tuple(0, 0), wheelCorners.get(i), wheelCorners.get((i + 1) % wheelCorners.size())))
			{
				ItemStack selected = ItemStack.EMPTY;
				if (page != null && i < page.items.size())
				{
					selected = page.items.get(i);
				}
				if (!isRight)
				{
					cap.getSelectedInventory().setStackInSlot(0, selected.copy());
					BuildersBag.network.sendToServer(new SetSelectedBlockServer(cap.getUUID(), selected));
					if (BuildersBagConfig.Settings.playPickBlockSounds)
						mc.player.playSound(SoundEvents.BLOCK_NOTE_HAT, 0.5f, 0.1f);
				}
				else
				{
					if (selected.isEmpty())
						return;

					if (BuildersBagConfig.Settings.playPickBlockSounds)
						mc.player.playSound(SoundEvents.BLOCK_NOTE_HAT, 0.5f, 0.7f);

					List<ItemStack> palette = cap.getPalette();
					boolean add = ItemHelper.containsStack(selected, palette).isEmpty();

					BuildersBag.network.sendToServer(new ModifyPaletteServer(cap.getUUID(), selected, add));
					if (add)
						palette.add(selected.copy());
					else
					{
						for (int j = 0; j < palette.size(); j++)
						{
							if (ItemStack.areItemStacksEqual(selected, palette.get(j)))
							{
								palette.remove(j);
								break;
							}
						}
					}
					SelectionFilter filt = filters.get("palette");
					filt.pages.clear();
					filt.pages = createPages(palette);
					if (activeFilter.equals("palette"))
					{
						activePageList.clear();
						activePageList.addAll(filt.pages);
						activePage = activePageList.isEmpty() ? 0 : activePage % activePageList.size();
					}
				}
				return;
			}
		}

		for (int i = 0; i < filters.size(); i++)
		{
			if (isInPolygon(mouseX, mouseY, filterPoints.get(i)))
			{
				activePage = 0;
				String filter = filterNames.get(i);
				if (filter.equals(activeFilter))
				{
					activeFilter = "";
					activePageList.clear();
					activePageList.addAll(unfilteredPageList);
				}
				else
				{
					activeFilter = filter;
					activePageList.clear();
					activePageList.addAll(filters.get(filter).pages);
				}

				activeFilterCache.put(cap.getUUID(), activeFilter);
				activePageCache.put(cap.getUUID(), activePage);
				mc.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2f, 1f);
			}
		}

	}

	public static void onScroll(int scrollDelta)
	{
		if (scrollDelta < 0)
		{
			activePage--;
			if (activePage < 0)
				activePage = activePageList.size() - 1;
			activePageCache.put(cap.getUUID(), activePage);
		}
		else if (scrollDelta > 0)
		{
			activePage++;
			activePage = activePage % activePageList.size();
			activePageCache.put(cap.getUUID(), activePage);
		}
	}

	private static void drawPageButtons(float partialTicks, int mouseX, int mouseY)
	{
		if (!(!open && ease < 0.5))
		{
			boolean hoverRight = isInPolygon(mouseX, mouseY, rightPoints.subList(0, 4)) || isInTriangle(mouseX, mouseY, rightPoints.subList(4, 7));
			boolean hoverLeft = isInPolygon(mouseX, mouseY, leftPoints.subList(0, 4)) || isInTriangle(mouseX, mouseY, leftPoints.subList(4, 7));

			Gui.drawModalRectWithCustomSizedTexture((int) (258 * ease), 94, 0, 1179 + (hoverRight ? 55 : 0), ARROW_WIDTH, ARROW_HEIGHT, 500, 1500);
			Gui.drawModalRectWithCustomSizedTexture((int) (309 * (1.0 - ease)) - 71, 94, ARROW_WIDTH + 1, 1179 + (hoverLeft ? 55 : 0), ARROW_WIDTH, ARROW_HEIGHT, 500, 1500);
		}
	}

	private static void drawCategoryButtons(float partialTicks, int mouseX, int mouseY)
	{
		ItemStack selected = cap.getSelectedInventory().getStackInSlot(0);

		boolean selectedHover = isInPolygon(mouseX, mouseY, getHexagonPoints(new Tuple(0, 178)));
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(97, 19 - (ease * 97), 0);
		Gui.drawModalRectWithCustomSizedTexture(-14, -19, 119 + (selectedHover ? 69 : 0), 1192, 68, 78, 500, 1500);
		GlStateManager.scale(2.5, 2.5, 2.5);
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.renderItemAndEffectIntoGUI(mc.player, selected, 0, 0);
		RenderHelper.disableStandardItemLighting();
		mc.getTextureManager().bindTexture(TEXTURE);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(119, 121, 0);

		int filtercount = filters.size();
		int offX = filtercount % 2 == 0 ? 34 : 0;
		int width = 66;

		for (int i = 0; i < filtercount; i++)
		{
			GlStateManager.pushMatrix();
			int tilePos = i <= filtercount / 2 ? (filtercount / 2 - i) * -66 : (i - filtercount / 2) * 66;
			GlStateManager.translate((-34 + offX + tilePos) * ease, 140 * ease, 0);
			GlStateManager.enableAlpha();
			boolean isHovering = isInPolygon(mouseX, mouseY, filterPoints.get(i));
			Gui.drawModalRectWithCustomSizedTexture(0, 0, 119 + (isHovering ? 69 : 0), 1192, 68, 78, 500, 1500);
			if (activeFilter.equals(filterNames.get(i)))
			{
				GlStateManager.translate(14 * ease, 20 * ease, 0);
				GlStateManager.scale(2.1, 2.1, 2.1);
				Gui.drawModalRectWithCustomSizedTexture(0, 0, 313, 1213, 18, 18, 500, 1500);
			}
			else
			{
				if (cap.hasModuleAndEnabled(filterNames.get(i)))
				{
					GlStateManager.translate(14 * ease, 19 * ease, 0);
					GlStateManager.scale(2.5, 2.5, 2.5);
					RenderHelper.enableGUIStandardItemLighting();
					itemRender.renderItemAndEffectIntoGUI(mc.player, BagHelper.getModule(filterNames.get(i), cap).getDisplayItem(), 0, 0);
					RenderHelper.disableStandardItemLighting();
					mc.getTextureManager().bindTexture(TEXTURE);
				}
				else if (filterNames.get(i).equals("palette"))
				{
					GlStateManager.translate(16 * ease, 20 * ease, 0);
					GlStateManager.scale(2.3, 2.3, 2.3);
					Gui.drawModalRectWithCustomSizedTexture(0, 0, 314, 1234, 16, 16, 500, 1500);
				}
			}
			GlStateManager.popMatrix();
		}

		for (int i = 0; i < filtercount; i++)
		{
			if (isInPolygon(mouseX, mouseY, filterPoints.get(i)))
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(mouseX, -mouseY, 0);
				GlStateManager.scale(1.5, 1.5, 1.5);
				if (activeFilter.equals(filterNames.get(i)))
					GuiUtils.drawHoveringText(Lists.newArrayList(TextFormatting.DARK_PURPLE + "Click" + TextFormatting.RESET + " to view " + TextFormatting.GOLD + "all blocks" + TextFormatting.RESET), 0, 0, res.getScaledWidth() * res.getScaleFactor(), res.getScaledHeight() * res.getScaleFactor(), 1000, mc.fontRenderer);
				else
					GuiUtils.drawHoveringText(Lists.newArrayList(TextFormatting.DARK_PURPLE + "Click" + TextFormatting.RESET + " to set " + TextFormatting.GOLD + (filterNames.get(i).equals("palette") ? "Palette" : filterNames.get(i)) + TextFormatting.RESET + " filter"), 0, 0, res.getScaledWidth() * res.getScaleFactor(), res.getScaledHeight() * res.getScaleFactor(), 1000, mc.fontRenderer);
				mc.getTextureManager().bindTexture(TEXTURE);
				RenderHelper.disableStandardItemLighting();
				GlStateManager.enableBlend();
				GlStateManager.popMatrix();
			}
			if(selectedHover && !cap.getSelectedInventory().getStackInSlot(0).isEmpty())
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(mouseX, -mouseY, 0);
				GlStateManager.scale(1.5, 1.5, 1.5);
				GuiUtils.drawHoveringText(Lists.newArrayList(TextFormatting.DARK_PURPLE + "Left Click" + TextFormatting.RESET + " to deselect ", TextFormatting.DARK_PURPLE + "Right Click" + TextFormatting.RESET + " to " + TextFormatting.GREEN + "add " + TextFormatting.GOLD + cap.getSelectedInventory().getStackInSlot(0).getDisplayName() + TextFormatting.RESET + " to Palette"), 0, 0, res.getScaledWidth() * res.getScaleFactor(), res.getScaledHeight() * res.getScaleFactor(), 1000, mc.fontRenderer);
				mc.getTextureManager().bindTexture(TEXTURE);
				RenderHelper.disableStandardItemLighting();
				GlStateManager.enableBlend();
				GlStateManager.popMatrix();
			}
		}

		GlStateManager.popMatrix();
	}

	private static void drawSelectionWheel(float partialTicks, int mouseX, int mouseY)
	{
		double renderScale = SCALE * ease;

		int offsetX = 0;
		int offsetY = 0;

		for (int i = 0; i < wheelCorners.size(); i++)
		{
			if (isInTriangle(mouseX, mouseY, new Tuple(0, 0), wheelCorners.get(i), wheelCorners.get((i + 1) % wheelCorners.size())))
			{
				offsetX = ((i + 1) % 2) * 239;
				offsetY = (i % 2) * 235 + ((i / 2) * 235);
			}
		}

		Gui.drawModalRectWithCustomSizedTexture(0, 0, offsetX, offsetY, WHEEL_WIDTH, WHEEL_HEIGHT, 500, 1500);

		if (!activeFilter.isEmpty())
		{
			GlStateManager.pushMatrix();
			if (activeFilter.equals("palette"))
			{
				GlStateManager.translate(119 - 16, 121 - 20, 0);
				GlStateManager.scale(2.3, 2.3, 2.3);
				Gui.drawModalRectWithCustomSizedTexture(0, 0, 314, 1234, 16, 16, 500, 1500);
			}
			else
			{
				GlStateManager.translate(119 - 20, 121 - 20, 0);
				GlStateManager.scale(2.5, 2.5, 2.5);
				RenderHelper.enableGUIStandardItemLighting();
				itemRender.renderItemAndEffectIntoGUI(mc.player, BagHelper.getModule(activeFilter, cap).getDisplayItem(), 0, 0);
				RenderHelper.disableStandardItemLighting();
				mc.getTextureManager().bindTexture(TEXTURE);
			}
			GlStateManager.popMatrix();
		}

		if (activePageList.size() > 0)
		{
			List<ItemStack> paletteList = cap.getPalette();

			SelectionWheelLogic.SelectionPage currentPage = activePageList.get(activePage);
			for (int i = 0; i < currentPage.items.size(); i++)
			{
				Vector2d p1 = vec(wheelCorners.get(i));
				Vector2d diff = vec(wheelCorners.get((i + 1) % wheelCorners.size()));
				diff.sub(p1);
				diff.scale(0.5);
				p1.add(diff);
				Vector2d itemPoint = new Vector2d(p1);
				itemPoint.scale(0.75);
				itemPoint.x -= 16;
				itemPoint.y += 16;

				GlStateManager.pushMatrix();
				GlStateManager.translate(119, 121, 0);
				GlStateManager.translate(itemPoint.x, -itemPoint.y, 0);
				GlStateManager.scale(2.0, 2.0, 2.0);
				RenderHelper.enableGUIStandardItemLighting();
				itemRender.renderItemAndEffectIntoGUI(mc.player, currentPage.items.get(i), 0, 0);
				RenderHelper.disableStandardItemLighting();
				if (!activeFilter.equals("palette") && !ItemHelper.containsStack(currentPage.items.get(i), paletteList).isEmpty())
				{
					mc.getTextureManager().bindTexture(TEXTURE);
					GlStateManager.scale(0.5, 0.5, 0.5);
					GlStateManager.translate(20, -4, 1000);
					Gui.drawModalRectWithCustomSizedTexture(0, 0, 314, 1234, 16, 16, 500, 1500);
				}
				GlStateManager.popMatrix();
			}

			String page = (activePage + 1) + "/" + activePageList.size();
			GlStateManager.pushMatrix();
			GlStateManager.translate(119 - mc.fontRenderer.getStringWidth(page), 240, 0);
			GlStateManager.scale(2, 2, 2);
			mc.fontRenderer.drawStringWithShadow(page, 0, 0, 0xFFFFFF);
			GlStateManager.popMatrix();

			for (int i = 0; i < currentPage.items.size(); i++)
			{
				ItemStack currentStack = currentPage.items.get(i);
				if (isInTriangle(mouseX, mouseY, new Tuple(0, 0), wheelCorners.get(i), wheelCorners.get((i + 1) % wheelCorners.size())))
				{
					GlStateManager.pushMatrix();
					GlStateManager.translate(119 + mouseX, 121 - mouseY, 0);
					GlStateManager.scale(1.5, 1.5, 1.5);
					GuiUtils.drawHoveringText(currentStack, Lists.newArrayList(TextFormatting.DARK_PURPLE + "Left Click" + TextFormatting.RESET + " to select " + TextFormatting.GOLD + currentStack.getDisplayName(), TextFormatting.DARK_PURPLE + "Right Click" + TextFormatting.RESET + " to " + TextFormatting.GREEN + "add " + TextFormatting.GOLD + currentStack.getDisplayName() + TextFormatting.RESET + " to Palette"), 0, 0, res.getScaledWidth() * res.getScaleFactor(), res.getScaledHeight() * res.getScaleFactor(), 1000, mc.fontRenderer);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.enableBlend();
					GlStateManager.popMatrix();
				}
			}
		}

		mc.getTextureManager().bindTexture(TEXTURE);

	}

	private static void transform(float partialTicks, ScaledResolution res)
	{
		double renderScale = SCALE * ease;

		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();

		GlStateManager.translate((res.getScaledWidth() / 2) - WHEEL_WIDTH / 2 * renderScale, (res.getScaledHeight() / 2) - WHEEL_HEIGHT / 2 * renderScale, 0);
		GlStateManager.scale(renderScale, renderScale, renderScale);

	}

	private static List<Tuple<Integer, Integer>> getCornerPoints()
	{
		int radius = WHEEL_HEIGHT / 2;
		int origX = WHEEL_WIDTH / 2;
		int origY = WHEEL_HEIGHT / 2;

		List<Tuple<Integer, Integer>> points = new ArrayList<Tuple<Integer, Integer>>();

		for (int i = 0; i < 9; i++)
		{
			points.add(new Tuple<Integer, Integer>((int) (Math.cos(Math.toRadians((i * 40 - 90) % 360)) * radius), -(int) (Math.sin(Math.toRadians((i * 40 - 90) % 360)) * radius)));
		}

		return points;
	}

	private static List<Tuple<Integer, Integer>> getArrowPoints(boolean right)
	{
		List<Tuple<Integer, Integer>> points = new ArrayList<Tuple<Integer, Integer>>();

		int m = right ? 1 : -1;
		// Square
		points.add(new Tuple(m * 140, 14));
		points.add(new Tuple(m * 140, -14));
		points.add(new Tuple(m * 163, -14));
		points.add(new Tuple(m * 163, 14));

		if (!right)
			Collections.reverse(points);

		// triangle
		points.add(new Tuple(m * 163, -28));
		points.add(new Tuple(m * 189, 0));
		points.add(new Tuple(m * 163, 28));

		return points;
	}

	private static List<Tuple<Integer, Integer>> getHexagonPoints(Tuple<Integer, Integer> middlePoint)
	{
		List<Tuple<Integer, Integer>> points = new ArrayList<Tuple<Integer, Integer>>();

		int radius = 38;

		for (int i = 0; i < 6; i++)
		{
			points.add(new Tuple<Integer, Integer>((int) (Math.cos(Math.toRadians((i * 60 - 90) % 360)) * radius) + middlePoint.getFirst(), -(int) (Math.sin(Math.toRadians((i * 60 - 90) % 360)) * radius) + middlePoint.getSecond()));
		}

		Collections.reverse(points);
		return points;
	}

	private static boolean isInTriangle(int x, int y, Collection<Tuple<Integer, Integer>> points)
	{
		return isInTriangle(x, y, points.toArray(new Tuple[points.size()]));
	}

	private static boolean isInTriangle(int x, int y, Tuple<Integer, Integer>... points)
	{
		if (points.length != 3)
			return false;

		Vector2d a = new Vector2d(points[0].getFirst() - x, points[0].getSecond() - y);
		Vector2d b = new Vector2d(points[1].getFirst() - x, points[1].getSecond() - y);
		Vector2d c = new Vector2d(points[2].getFirst() - x, points[2].getSecond() - y);

		double totalAngles = a.angle(b) + b.angle(c) + c.angle(a);
		return Math.abs(totalAngles - Math.PI * 2) <= 0.000001;
	}

	private static boolean isInPolygon(int x, int y, Collection<Tuple<Integer, Integer>> points)
	{
		return isInPolygon(x, y, points.toArray(new Tuple[points.size()]));
	}

	// Points must be passed counter-clockwise
	private static boolean isInPolygon(int x, int y, Tuple<Integer, Integer>... points)
	{
		if (points.length <= 1)
			return false;

		for (int i = 0; i < points.length; i++)
		{
			Tuple<Integer, Integer> p1 = points[i];
			Tuple<Integer, Integer> p2 = points[(i + 1) % points.length];
			double d = (p2.getFirst() - p1.getFirst()) * (y - p1.getSecond()) - (x - p1.getFirst()) * (p2.getSecond() - p1.getSecond());
			if (d < 0)
				return false;
		}

		return true;
	}

	private static Vector2d vec(Tuple<Integer, Integer> tuple)
	{
		return new Vector2d(tuple.getFirst(), tuple.getSecond());
	}

	private static List<SelectionWheelLogic.SelectionPage> createPages(List<ItemStack> stacks)
	{
		List<SelectionWheelLogic.SelectionPage> pages = new ArrayList<SelectionWheelLogic.SelectionPage>();

		int counter = 0;
		int pageCount = 0;
		SelectionWheelLogic.SelectionPage currentPage = new SelectionWheelLogic.SelectionPage();
		for (int i = 0; i < stacks.size(); i++)
		{
			ItemStack provideable = stacks.get(i).copy();
			currentPage.items.add(provideable);
			counter++;

			if (counter == 9)
			{
				pageCount++;
				counter = 0;
				pages.add(currentPage);
				currentPage = new SelectionWheelLogic.SelectionPage();
				currentPage.index = pageCount;
			}
		}
		if (!currentPage.items.isEmpty())
			pages.add(currentPage);

		return pages;
	}

	private static class SelectionFilter
	{
		private List<SelectionWheelLogic.SelectionPage> pages = new ArrayList<SelectionWheelLogic.SelectionPage>();
		private String name = "";

		public SelectionFilter(List<SelectionWheelLogic.SelectionPage> pages, String name)
		{
			this.pages = pages;
			this.name = name;
		}

		@Override
		public String toString()
		{
			return pages.toString();
		}
	}
}
