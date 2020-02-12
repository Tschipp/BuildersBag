package tschipp.buildersbag;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import tschipp.buildersbag.common.RegistryHandler;
import tschipp.buildersbag.common.inventory.BagGuiHandler;

public class CommonProxy
{

	public void preInit(FMLPreInitializationEvent event)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(BuildersBag.instance, new BagGuiHandler());
		RegistryHandler.registerCapabilities();
		RegistryHandler.registerItems();
		RegistryHandler.registerModules();
	}

	public void init(FMLInitializationEvent event)
	{
	}

	public void postInit(FMLPostInitializationEvent event)
	{
		
	}

}
