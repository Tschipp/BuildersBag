package tschipp.buildersbag;

import java.lang.reflect.Field;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkEventFiringHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import tschipp.buildersbag.common.BuildersBagRegistry;
import tschipp.buildersbag.common.config.BuildersBagConfig;
import tschipp.buildersbag.common.crafting.CraftingHandler;
import tschipp.buildersbag.common.inventory.BagGuiHandler;
import tschipp.buildersbag.compat.bbw.BBWCompat;
import tschipp.buildersbag.compat.botania.BotaniaCompat;
import tschipp.buildersbag.compat.chisel.ChiselEvents;
import tschipp.buildersbag.compat.linear.LinearCompatManager;
import tschipp.buildersbag.network.GrowItemClient;
import tschipp.buildersbag.network.OpenBaubleBagServer;
import tschipp.buildersbag.network.SetHeldItemClient;
import tschipp.buildersbag.network.SyncBagCapClient;
import tschipp.buildersbag.network.SyncBagCapInventoryClient;
import tschipp.buildersbag.network.CompactBagServer;
import tschipp.buildersbag.network.SyncBagCapServer;
import tschipp.buildersbag.network.SyncEnderchestToClient;
import tschipp.buildersbag.network.SyncItemStackServer;
import tschipp.buildersbag.network.SyncModuleStateServer;

public class CommonProxy
{

	private static Field eventChannel;

	static
	{
		eventChannel = ReflectionHelper.findField(NetworkEventFiringHandler.class, "eventChannel");
		eventChannel.setAccessible(true);
	}

	public void preInit(FMLPreInitializationEvent event)
	{
		BuildersBag.network = NetworkRegistry.INSTANCE.newSimpleChannel(BuildersBag.MODID);
		BuildersBag.network.registerMessage(SyncItemStackServer.class, SyncItemStackServer.class, 0, Side.SERVER);
		BuildersBag.network.registerMessage(SyncModuleStateServer.class, SyncModuleStateServer.class, 1, Side.SERVER);
		BuildersBag.network.registerMessage(SyncBagCapClient.class, SyncBagCapClient.class, 2, Side.CLIENT);
		BuildersBag.network.registerMessage(SetHeldItemClient.class, SetHeldItemClient.class, 3, Side.CLIENT);
		BuildersBag.network.registerMessage(SyncBagCapInventoryClient.class, SyncBagCapInventoryClient.class, 4, Side.CLIENT);
		BuildersBag.network.registerMessage(OpenBaubleBagServer.class, OpenBaubleBagServer.class, 5, Side.SERVER);
		BuildersBag.network.registerMessage(GrowItemClient.class, GrowItemClient.class, 6, Side.CLIENT);
		BuildersBag.network.registerMessage(SyncEnderchestToClient.class, SyncEnderchestToClient.class, 7, Side.CLIENT);
		BuildersBag.network.registerMessage(SyncBagCapServer.class, SyncBagCapServer.class, 8, Side.SERVER);
		BuildersBag.network.registerMessage(CompactBagServer.class, CompactBagServer.class, 9, Side.SERVER);

		NetworkRegistry.INSTANCE.registerGuiHandler(BuildersBag.instance, new BagGuiHandler());
		BuildersBagRegistry.registerModules();

		BuildersBagConfig.setDefaultsOnFirstLoad();
		
		BuildersBagRegistry.registerCapabilities();
		BuildersBagRegistry.registerItems();
	}

	public void init(FMLInitializationEvent event)
	{
		if(Loader.isModLoaded("betterbuilderswands"))
			BBWCompat.register();
		
		if(Loader.isModLoaded("botania"))
			BotaniaCompat.register();

	}

	public void postInit(FMLPostInitializationEvent event)
	{
		CraftingHandler.generateRecipes();

		if (Loader.isModLoaded("chiselsandbits"))
		{
			FMLEmbeddedChannel server = NetworkRegistry.INSTANCE.getChannel("ChiselsAndBits", Side.SERVER);
			NetworkEventFiringHandler handler = server.pipeline().get(NetworkEventFiringHandler.class);
			try
			{
				FMLEventChannel fmleventChannel = (FMLEventChannel) eventChannel.get(handler);
				fmleventChannel.register(new ChiselEvents());
			} catch (IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		
		if(Loader.isModLoaded("linear"))
			LinearCompatManager.register();

	}
	
	public EntityPlayer getPlayer()
	{
		return null;
	}
	
	public void setTEISR(Item item)
	{
		
	}

}
