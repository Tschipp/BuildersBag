package tschipp.buildersbag;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.forgespi.language.IModInfo;
import tschipp.buildersbag.common.config.Configs;

@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(BuildersBag.MODID)
public class BuildersBag
{

	public static final IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	// Instance
//	@Instance(BuildersBag.MODID)
//	public static BuildersBag instance;

	public static final String MODID = "buildersbag";
	public static final String VERSION = "GRADLE:VERSION";
	public static final String NAME = "Builder's Bag";
	public static final String ACCEPTED_VERSIONS = "[1.12.2,1.13)";
	public static final String DEPENDENCIES = "required-after:forge@[13.20.1.2386,);after:chiselsandbits;after:littletiles@[1.5.0,);after:creativecore@[1.10.0,);after:linear@[1.3,);after:craftteaker;after:buildinggadgets@[2.8.4,);";
	public static final Logger LOGGER = LogManager.getFormatterLogger(MODID.toUpperCase());
	public static final String CERTIFICATE = "fd21553434f4905f2f73ea7838147ac4ea07bd88";

	public static SimpleChannel network;
	public static IModInfo info;

	public static boolean FINGERPRINT_VIOLATED = false;

	private static final File configFile = new File("config/buildersbag.cfg");
	public static final File seenModsFile = new File("seen_buildersbag_addons.txt");
	private static final List<String> seenMods = new ArrayList<String>();
	public static boolean isNewlyGenerated = false;
	
	public static boolean TESTING = true;
	
	public BuildersBag()
	{
//		if(!configFile.exists())
//		{
//			isNewlyGenerated = true;
//		}
				
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_CONFIG);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Configs.SERVER_CONFIG);
		
		info = ModLoadingContext.get().getActiveContainer().getModInfo();
	}
	
//	@EventHandler
//	public void construction(FMLConstructionEvent event)
//	{
//		try
//		{	
//			if (seenModsFile.exists())
//			{
//				seenMods.addAll(Files.readAllLines(seenModsFile.toPath()));
//			} else
//				seenModsFile.createNewFile();
//		} catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//	}

	public static Set<String> getSeenMods()
	{
		return new HashSet<String>(seenMods);
	}
}