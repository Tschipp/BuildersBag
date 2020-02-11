package tschipp.buildersbag;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@EventBusSubscriber
@Mod(modid = BuildersBag.MODID, name = BuildersBag.NAME, version = BuildersBag.VERSION, dependencies = BuildersBag.DEPENDENCIES, acceptedMinecraftVersions = BuildersBag.ACCEPTED_VERSIONS, guiFactory = "tschipp.buildersbag.client.gui.GuiFactoryBuildersBag")
public class BuildersBag
{

	@SidedProxy(clientSide = "tschipp.buildersbag.ClientProxy", serverSide = "tschipp.buildersbag.CommonProxy")
	public static CommonProxy proxy;

	// Instance
	@Instance(BuildersBag.MODID)
	public static BuildersBag instance;

	public static final String MODID = "buildersbag";
	public static final String VERSION = "1.0";
	public static final String NAME = "Builder's Bag";
	public static final String ACCEPTED_VERSIONS = "[1.12.2,1.13)";
	public static final String DEPENDENCIES = "required-after:forge@[13.20.1.2386,);";
	public static final Logger LOGGER = LogManager.getFormatterLogger(MODID.toUpperCase());

	public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		BuildersBag.proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		BuildersBag.proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		BuildersBag.proxy.postInit(event);
	}
}