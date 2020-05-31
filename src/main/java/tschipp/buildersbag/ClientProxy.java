package tschipp.buildersbag;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import tschipp.buildersbag.client.BuildersBagKeybinds;
import tschipp.buildersbag.client.rendering.BagItemStackRenderer;
import tschipp.buildersbag.client.rendering.ItemRendering;

public class ClientProxy extends CommonProxy
{

	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
		BuildersBagKeybinds.registerKeybinds();
		ItemRendering.regItemRenders();
	}
	
	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
	}
	
	@Override
	public EntityPlayer getPlayer()
	{
		return Minecraft.getMinecraft().player;
	}

	@Override
	public void setTEISR(Item item)
	{
		item.setTileEntityItemStackRenderer(new BagItemStackRenderer());
	}
	
	@Override
	public void startWorking(String uuid, EntityPlayer player)
	{
		BagItemStackRenderer.working.add(uuid);
	}
	
	@Override
	public void stopWorking(String uuid, EntityPlayer player)
	{
		BagItemStackRenderer.working.remove(uuid);
	}
}
