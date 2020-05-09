package tschipp.buildersbag.client.rendering;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.item.BuildersBagItem;

@EventBusSubscriber(modid = BuildersBag.MODID, value = Side.CLIENT)
public class BagItemStackRenderer extends TileEntityItemStackRenderer
{
	public static TransformType transform;

	private NonNullList<ItemStack> possibleItems = NonNullList.create();
	private String renderStack = "";
	public static int listIndex;
	private static int renderTimer = 0;

	private static Method renderModelF;

	static
	{
		renderModelF = ReflectionHelper.findMethod(RenderItem.class, "renderModel", "func_191967_a", IBakedModel.class, int.class, ItemStack.class);
		renderModelF.setAccessible(true);
	}

	@Override
	public void renderByItem(ItemStack stack, float partialTicks)
	{

		EntityPlayer player = Minecraft.getMinecraft().player;
		Minecraft mc = Minecraft.getMinecraft();
		World world = player.world;
		RenderItem render = mc.getRenderItem();

		String serialized = stack.serializeNBT().toString();
		
		if(!serialized.equals(renderStack))
		{
			renderStack = serialized;
			regenerateAvailablityList(stack);
		}

		IBagCap bag = CapHelper.getBagCap(stack);

		boolean random = bag.hasModuleAndEnabled("buildersbag:random");

		ItemStack selected = random ? (possibleItems.size() > 0 ? possibleItems.get(listIndex % possibleItems.size()) : ItemStack.EMPTY) : bag.getSelectedInventory().getStackInSlot(0);

		IBakedModel selectedModel = render.getItemModelWithOverrides(selected, mc.world, mc.player);
		IBakedModel renderModel = render.getItemModelWithOverrides(stack, mc.world, mc.player);

		boolean renderAsBlock = transform == TransformType.FIRST_PERSON_LEFT_HAND || transform == TransformType.FIRST_PERSON_RIGHT_HAND || transform == TransformType.THIRD_PERSON_LEFT_HAND || transform == TransformType.THIRD_PERSON_RIGHT_HAND;

		GlStateManager.pushMatrix();

		GlStateManager.enableRescaleNormal();

		if (!selected.isEmpty())
		{
			if (transform == TransformType.THIRD_PERSON_LEFT_HAND || transform == TransformType.THIRD_PERSON_RIGHT_HAND)
			{
				GlStateManager.pushMatrix();

				GlStateManager.translate(0.5F, 0.17F, 0.5F);
				GlStateManager.scale(1.85, 1.85, 1.85);

				ForgeHooksClient.handleCameraTransforms(selectedModel, transform, false);

				GlStateManager.translate(-0.5F, -0.5F, -0.5F);

				renderModel(render, selectedModel, -1, selected);

				GlStateManager.popMatrix();
			} else if (transform == TransformType.FIRST_PERSON_LEFT_HAND || transform == TransformType.FIRST_PERSON_RIGHT_HAND)
			{
				GlStateManager.pushMatrix();

				if (transform == TransformType.FIRST_PERSON_RIGHT_HAND)
				{
					GlStateManager.translate(0, 0.22, 0.4);
					GlStateManager.scale(1.8, 1.8, 1.8);

					GlStateManager.rotate(90, 0, 1, 0);
					GlStateManager.rotate(25, 1, 0, 0);
				} else
				{
					GlStateManager.translate(1, 0.22, 0.4);
					GlStateManager.scale(1.8, 1.8, 1.8);

					GlStateManager.rotate(3, 0, 1, 0);
					GlStateManager.rotate(25, 0, 0, 1);

					if (!render.shouldRenderItemIn3D(selected))
					{
						GlStateManager.translate(-0.3, 0.15, 0);

						GlStateManager.rotate(10, 1, 0, 0);
						GlStateManager.rotate(-50, 0, 0, 1);
						GlStateManager.rotate(80, 0, 1, 0);
					}

				}

				ForgeHooksClient.handleCameraTransforms(selectedModel, transform, false);

				GlStateManager.translate(-0.5F, -0.5F, -0.5F);

				renderModel(render, selectedModel, -1, selected);

				GlStateManager.popMatrix();
			} else
			{
				GlStateManager.pushMatrix();
				renderModel(render, renderModel, -1, stack);
				GlStateManager.popMatrix();

				if (transform == TransformType.GUI)
				{
					GlStateManager.pushMatrix();

					GlStateManager.enableLighting();
					GlStateManager.translate(0, 0, 3);

					GlStateManager.translate(0.1, 0.1, 0);
					GlStateManager.scale(0.5, 0.5, 0.5);

					GlStateManager.translate(1.3, 1.3, 0);

					ForgeHooksClient.handleCameraTransforms(selectedModel, transform, false);

					GlStateManager.translate(-0.5F, -0.5F, -0.5F);

					renderModel(render, selectedModel, -1, selected);

					GlStateManager.popMatrix();
				}

			}
		} else
		{
			renderModel(render, renderModel, -1, stack);
		}

		GlStateManager.popMatrix();
	}

	private void renderModel(RenderItem render, IBakedModel model, int color, ItemStack stack)
	{
		try
		{
			if (model.isBuiltInRenderer() && !(stack.getItem() instanceof BuildersBagItem))
			{
				GlStateManager.pushMatrix();
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.enableRescaleNormal();
				TileEntityItemStackRenderer.instance.renderByItem(stack);
				GlStateManager.popMatrix();
			} else
			{
				renderModelF.invoke(render, model, color, stack);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
		}
	}

	private void regenerateAvailablityList(ItemStack bagStack)
	{
		if (!(bagStack.getItem() instanceof BuildersBagItem))
			return;

		IBagCap cap = CapHelper.getBagCap(bagStack);

		if (!cap.hasModuleAndEnabled("buildersbag:random"))
			return;

		possibleItems.clear();
		possibleItems.addAll(InventoryHelper.getAllAvailableStacks(cap));
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event)
	{
		renderTimer += 1;
		if (renderTimer >= 60)
		{
			listIndex++;

			renderTimer = 0;

			if (listIndex >= 50000)
				listIndex = 0;
		}
	}
}
