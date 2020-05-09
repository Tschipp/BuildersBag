package tschipp.buildersbag;

import java.lang.reflect.Field;

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
import tschipp.buildersbag.common.crafting.CraftingHandler;
import tschipp.buildersbag.common.inventory.BagGuiHandler;
import tschipp.buildersbag.compat.chisel.ChiselEvents;
import tschipp.buildersbag.compat.linear.LinearCompatManager;
import tschipp.buildersbag.network.SetHeldItemClient;
import tschipp.buildersbag.network.SyncBagCapClient;
import tschipp.buildersbag.network.SyncBagClient;
import tschipp.buildersbag.network.SyncItemStack;
import tschipp.buildersbag.network.SyncModuleState;

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
		BuildersBag.network.registerMessage(SyncItemStack.class, SyncItemStack.class, 0, Side.SERVER);
		BuildersBag.network.registerMessage(SyncModuleState.class, SyncModuleState.class, 1, Side.SERVER);
		BuildersBag.network.registerMessage(SyncBagClient.class, SyncBagClient.class, 2, Side.CLIENT);
		BuildersBag.network.registerMessage(SyncBagCapClient.class, SyncBagCapClient.class, 3, Side.CLIENT);
		BuildersBag.network.registerMessage(SetHeldItemClient.class, SetHeldItemClient.class, 4, Side.CLIENT);

		NetworkRegistry.INSTANCE.registerGuiHandler(BuildersBag.instance, new BagGuiHandler());
		BuildersBagRegistry.registerCapabilities();
		BuildersBagRegistry.registerItems();
		BuildersBagRegistry.registerModules();
	}

	public void init(FMLInitializationEvent event)
	{
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

}
