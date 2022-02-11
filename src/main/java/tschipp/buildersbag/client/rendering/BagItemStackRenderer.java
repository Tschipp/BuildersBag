package tschipp.buildersbag.client.rendering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.item.BuildersBagItem;

@EventBusSubscriber(modid = BuildersBag.MODID, value = Dist.CLIENT)
public class BagItemStackRenderer extends ItemStackTileEntityRenderer
{
	private Map<Integer, List<Item>> possibleItems = new HashMap<Integer, List<Item>>();
	private Map<Integer, String> renderStack = new HashMap<Integer, String>();
	public static Set<String> working = new HashSet<String>();
	public static int listIndex;
	private static int renderTimer = 0;
	private static int renderTotal = 0;

	// private static Method renderModelF;

	// static
	// {
	// renderModelF = ReflectionHelper.findMethod(RenderItem.class,
	// "renderModel", "func_191967_a", IBakedModel.class, int.class,
	// ItemStack.class);
	// renderModelF.setAccessible(true);
	// }

	@Override
	public void renderByItem(ItemStack stack, TransformType transform, MatrixStack matrix, IRenderTypeBuffer buf, int x, int y)
	{
		PlayerEntity player = Minecraft.getInstance().player;
		Minecraft mc = Minecraft.getInstance();
		World world = player.level;
		ItemRenderer render = mc.getItemRenderer();
		IBakedModel gears = render.getItemModelShaper().getModelManager().getModel(new ModelResourceLocation("buildersbag:gears", "inventory"));

		String serialized = stack.serializeNBT().toString();
		int hash = stack.hashCode();

		if (renderStack.get(hash) == null)
		{
			renderStack.put(hash, serialized);
			regenerateAvailablityList(stack);
		}

		if (renderStack.get(hash) != null && !renderStack.get(hash).equals(serialized))
		{
			renderStack.put(hash, serialized);
			regenerateAvailablityList(stack);
		}

		IBagCap bag = CapHelper.getBagCap(stack);

		boolean random = bag.hasModuleAndEnabled("buildersbag:random");

		List<Item> possibleItems = this.possibleItems.get(hash);

		ItemStack selected = random ? (possibleItems.size() > 0 ? new ItemStack(possibleItems.get(listIndex % possibleItems.size())) : ItemStack.EMPTY) : bag.getSelectedInventory().getStackInSlot(0);

		IBakedModel selectedModel = render.getModel(selected, mc.level, mc.player);
		IBakedModel renderModel = render.getModel(stack, mc.level, mc.player);

		boolean renderAsBlock = transform == TransformType.FIRST_PERSON_LEFT_HAND || transform == TransformType.FIRST_PERSON_RIGHT_HAND || transform == TransformType.THIRD_PERSON_LEFT_HAND || transform == TransformType.THIRD_PERSON_RIGHT_HAND;

		matrix.pushPose();

		// GlStateManager._enableRescaleNormal();

		if (!selected.isEmpty() && renderAsBlock)
		{
			matrix.pushPose();
			render.render(stack, transform, true, matrix, buf, x, y, selectedModel);
			matrix.popPose();
			// if (transform == TransformType.THIRD_PERSON_LEFT_HAND ||
			// transform == TransformType.THIRD_PERSON_RIGHT_HAND)
			// {
			// matrix.pushPose();
			//
			// matrix.translate(0.5F, 0.17F, 0.5F);
			// matrix.scale(1.85f, 1.85f, 1.85f);
			//
			// ForgeHooksClient.handleCameraTransforms(matrix, selectedModel,
			// transform, false);
			//
			// matrix.translate(-0.5F, -0.5F, -0.5F);
			//
			// renderModel(render, selectedModel, -1, selected);
			//
			// matrix.popPose();
			// }
			// else if (transform == TransformType.FIRST_PERSON_LEFT_HAND ||
			// transform == TransformType.FIRST_PERSON_RIGHT_HAND)
			// {
			// matrix.pushPose();
			//
			// if (transform == TransformType.FIRST_PERSON_RIGHT_HAND)
			// {
			// matrix.translate(0, 0.22, 0.4);
			// matrix.scale(1.8f, 1.8f, 1.8f);
			//
			// matrix.mulPose(Vector3f.YP.rotation(90));
			// matrix.mulPose(Vector3f.XP.rotation(25));
			// }
			// else
			// {
			// matrix.translate(1, 0.22, 0.4);
			// matrix.scale(1.8f, 1.8f, 1.8f);
			//
			// matrix.mulPose(Vector3f.YP.rotation(3));
			// matrix.mulPose(Vector3f.ZP.rotation(25));
			//
			// if (!render..shouldRenderItemIn3D(selected))
			// {
			// matrix.translate(-0.3, 0.15, 0);
			//
			// matrix.mulPose(Vector3f.YP.rotation(3));
			// matrix.mulPose(Vector3f.YP.rotation(3));
			// matrix.mulPose(Vector3f.YP.rotation(3));
			//
			// GlStateManager.rotate(10, 1, 0, 0);
			// GlStateManager.rotate(-50, 0, 0, 1);
			// GlStateManager.rotate(80, 0, 1, 0);
			// }
			//
			// }
			//
			// ForgeHooksClient.handleCameraTransforms(selectedModel, transform,
			// false);
			//
			// matrix.translate(-0.5F, -0.5F, -0.5F);
			//
			// renderModel(render, selectedModel, -1, selected);
			//
			// matrix.popPose();
			// }
			// else
			// {
			// matrix.pushPose();
			// renderModel(render, renderModel, -1, stack);
			// matrix.popPose();
			//
			// if (transform == TransformType.GUI)
			// {
			//
			//
			// matrix.pushPose();
			//
			// GlStateManager._enableLighting();
			// matrix.translate(0, 0, 3);
			//
			// matrix.translate(0.1, 0.1, 0);
			// matrix.scale(0.5f, 0.5f, 0.5f);
			//
			// matrix.translate(1.3, 1.3, 0);
			//
			// ForgeHooksClient.handleCameraTransforms(matrix, selectedModel,
			// transform, false);
			//
			// matrix.translate(-0.5F, -0.5F, -0.5F);
			//
			// renderModel(render, selectedModel, -1, selected);
			// matrix.popPose();
			//
			// if (Configs.Settings.drawWorkingState.get() &&
			// BagItemStackRenderer.working.contains(bag.getUUID()))
			// {
			// GlStateManager._disableLighting();
			// mc.getTextureManager().bind(mc.getTextureMapBlocks().LOCATION_BLOCKS_TEXTURE);
			// matrix.pushPose();
			// matrix.scale(0.7f, 0.7f, 0.7f);
			// matrix.translate(0, 0.0, 2);
			// renderModel(render, gears, -1, stack);
			// matrix.popPose();
			// GlStateManager._enableLighting();
			// }
			//
			// }
			//
			// }
		}
		else
		{
			matrix.pushPose();
			render.render(stack, transform, true, matrix, buf, x, y, renderModel);
			matrix.popPose();
		}

		if (transform == TransformType.GUI)
		{
			matrix.pushPose();

			matrix.scale(0.7f, 0.7f, 0.7f);
			matrix.translate(0, 0.0, 2);

			render.render(stack, transform, true, matrix, buf, x, y, gears);

			matrix.popPose();
		}

		matrix.popPose();
	}

	// private void renderModel(RenderItem render, IBakedModel model, int color,
	// ItemStack stack)
	// {
	// try
	// {
	// if (model.isCustomRenderer() && !(stack.getItem() instanceof
	// BuildersBagItem))
	// {
	// matrix.pushPose();
	// GlStateManager.color3arg(1.0F, 1.0F, 1.0F, 1.0F);
	// GlStateManager._enableRescaleNormal();
	// TileItemEntityStackRenderer.instance.renderByItem(stack);
	// matrix.popPose();
	// }
	// else
	// {
	// renderModelF.invoke(render, model, color, stack);
	// }
	// }
	// catch (IllegalAccessException | IllegalArgumentException |
	// InvocationTargetException e)
	// {
	// }
	// }+

	private void regenerateAvailablityList(ItemStack bagStack)
	{
		if (!(bagStack.getItem() instanceof BuildersBagItem))
			return;

		if (renderTotal >= 10000)
		{
			this.possibleItems.clear();
			this.renderStack.clear();
			BagItemStackRenderer.renderTotal = 0;
		}

		List<Item> list = possibleItems.get(bagStack.hashCode());
		if (list == null)
			list = Lists.newArrayList();

		IBagCap cap = CapHelper.getBagCap(bagStack);

		if (!cap.hasModuleAndEnabled("buildersbag:random"))
			return;

		list.clear();
		for (Item s : cap.getComplex().getAllAvailableItems())
		{
			if (s instanceof BlockItem)
				list.add(s);
		}

		possibleItems.put(bagStack.hashCode(), list);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event)
	{
		renderTimer += 1;
		renderTotal++;
		if (renderTimer >= 60)
		{
			listIndex++;

			renderTimer = 0;

			if (listIndex >= 50000)
				listIndex = 0;
		}
	}
}
