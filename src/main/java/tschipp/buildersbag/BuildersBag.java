package tschipp.buildersbag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import tschipp.buildersbag.common.config.BuildersBagConfig;

@EventBusSubscriber
@Mod(modid = BuildersBag.MODID, name = BuildersBag.NAME, version = BuildersBag.VERSION, dependencies = BuildersBag.DEPENDENCIES, acceptedMinecraftVersions = BuildersBag.ACCEPTED_VERSIONS, guiFactory = "tschipp.buildersbag.client.gui.GuiFactoryBuildersBag", certificateFingerprint = BuildersBag.CERTIFICATE)
public class BuildersBag
{

	@SidedProxy(clientSide = "tschipp.buildersbag.ClientProxy", serverSide = "tschipp.buildersbag.CommonProxy")
	public static CommonProxy proxy;

	// Instance
	@Instance(BuildersBag.MODID)
	public static BuildersBag instance;

	public static final String MODID = "buildersbag";
	public static final String VERSION = "GRADLE:VERSION";
	public static final String NAME = "Builder's Bag";
	public static final String ACCEPTED_VERSIONS = "[1.12.2,1.13)";
	public static final String DEPENDENCIES = "required-after:forge@[13.20.1.2386,);after:chiselsandbits;after:littletiles@[1.5.0,);after:creativecore@[1.10.0,);after:linear@[1.3,);after:craftteaker;";
	public static final Logger LOGGER = LogManager.getFormatterLogger(MODID.toUpperCase());
	public static final String CERTIFICATE = "fd21553434f4905f2f73ea7838147ac4ea07bd88";

	public static SimpleNetworkWrapper network;

	public static boolean FINGERPRINT_VIOLATED = false;

	private static final File configFile = new File("config/buildersbag.cfg");
	public static final File seenModsFile = new File("seen_buildersbag_addons.txt");
	private static final List<String> seenMods = new ArrayList<String>();
	public static boolean isNewlyGenerated = false;

	public BuildersBag()
	{
		if(!configFile.exists())
		{
			isNewlyGenerated = true;
		}
	}
	
	@EventHandler
	public void construction(FMLConstructionEvent event)
	{
		try
		{	
			if (seenModsFile.exists())
			{
				seenMods.addAll(Files.readAllLines(seenModsFile.toPath()));
			} else
				seenModsFile.createNewFile();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

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

	@EventHandler
	public void onFingerprintViolation(FMLFingerprintViolationEvent event)
	{

		LOGGER.error("WARNING! Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with! If you didn't download the file from https://minecraft.curseforge.com/projects/buildersbag or through any kind of mod launcher, immediately delete the file and re-download it from https://minecraft.curseforge.com/projects/buildersbag");
		FINGERPRINT_VIOLATED = true;
	}

	public static Set<String> getSeenMods()
	{
		return new HashSet<String>(seenMods);
	}
}