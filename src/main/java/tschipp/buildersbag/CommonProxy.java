package tschipp.buildersbag;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import tschipp.buildersbag.common.RegistryHandler;
import tschipp.buildersbag.common.inventory.BagGuiHandler;
import tschipp.buildersbag.network.SyncItemStack;
import tschipp.buildersbag.network.SyncModuleState;

public class CommonProxy
{

	public void preInit(FMLPreInitializationEvent event)
	{
		BuildersBag.network = NetworkRegistry.INSTANCE.newSimpleChannel(BuildersBag.MODID);
		BuildersBag.network.registerMessage(SyncItemStack.class, SyncItemStack.class, 0, Side.SERVER);
		BuildersBag.network.registerMessage(SyncModuleState.class, SyncModuleState.class, 1, Side.SERVER);

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
