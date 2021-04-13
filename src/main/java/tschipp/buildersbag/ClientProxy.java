package tschipp.buildersbag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tschipp.buildersbag.client.BuildersBagKeybinds;
import tschipp.buildersbag.client.gui.GuiBag;
import tschipp.buildersbag.client.rendering.BagItemStackRenderer;
import tschipp.buildersbag.client.rendering.ItemRendering;
import tschipp.buildersbag.common.BuildersBagRegistry;


@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class ClientProxy implements IProxy
{
	
	@SubscribeEvent
	public static void onClientStartup(FMLClientSetupEvent event)
	{
		BuildersBagKeybinds.registerKeybinds();
		ItemRendering.regItemRenders();
		
		ScreenManager.registerFactory(BuildersBagRegistry.BAG_CONTAINER_TYPE, (container, inv, name) -> {
			return new GuiBag(container, inv.player, name);
		});
	}
	
	@Override
	public PlayerEntity getPlayer()
	{
		return Minecraft.getInstance().player;
	}

//	@Override
//	public void setTEISR(Item item)
//	{
//		item.setTileItemEntityStackRenderer(new BagItemStackRenderer());
//	}
	
	@Override
	public World getWorld()
	{
		return Minecraft.getInstance().world;
	}

	@Override
	public void changeWorkState(String uuid, PlayerEntity player, boolean start)
	{
		if(start)
			BagItemStackRenderer.working.add(uuid);
		else
			BagItemStackRenderer.working.remove(uuid);
	}
}
