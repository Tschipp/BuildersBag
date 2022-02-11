package tschipp.buildersbag.common.config;

import java.util.Arrays;
import java.util.List;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.BuildersBagRegistry;

@Mod.EventBusSubscriber(modid = BuildersBag.MODID, bus = Bus.MOD)
public class Configs
{

	private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

	public static final ForgeConfigSpec SERVER_CONFIG;
	public static final ForgeConfigSpec CLIENT_CONFIG;

	static
	{
		Settings.init(SERVER_BUILDER, CLIENT_BUILDER);

		SERVER_CONFIG = SERVER_BUILDER.build();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}
	
	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading event)
	{
		if (event.getConfig().getModId().equals(BuildersBag.MODID))
		{
			CommentedConfig cfg = event.getConfig().getConfigData();
						
			if(cfg instanceof CommentedFileConfig)
				((CommentedFileConfig) cfg).load();
		}
	}
	
	@SubscribeEvent
	public static void onConfigChanged(ModConfig.Reloading event)
	{
		if (event.getConfig().getModId().equals(BuildersBag.MODID))
		{
			CommentedConfig cfg = event.getConfig().getConfigData();
						
			if(cfg instanceof CommentedFileConfig)
				((CommentedFileConfig) cfg).load();
		}
	}

	public static class Settings
	{

		public static IntValue tier1Slots;

		public static ConfigValue<List<? extends String>> tier1Modules;

		public static IntValue tier2Slots;

		public static ConfigValue<List<? extends String>> tier2Modules;

		public static IntValue tier3Slots;

		public static ConfigValue<List<? extends String>> tier3Modules;

		public static IntValue tier4Slots;

		public static ConfigValue<List<? extends String>> tier4Modules;

		public static IntValue tier5Slots;

		public static ConfigValue<List<? extends String>> tier5Modules;

		public static BooleanValue addUpdateRecipes;

		public static BooleanValue drawWorkingState;

		public static BooleanValue playFailSounds;

		public static BooleanValue playPickBlockSounds;
		
		public static void init(ForgeConfigSpec.Builder s, ForgeConfigSpec.Builder c)
		{
			s.comment("Settings");
			
			c.comment("Settings");
			
			tier1Slots = s.comment("Slots of the tier 1 bag")
					.worldRestart()
					.defineInRange("settings.tier1Slots", 9, 1, 54);
			
			tier2Slots = s.comment("Slots of the tier 2 bag")
					.worldRestart()
					.defineInRange("settings.tier2Slots", 18, 1, 54);
			
			tier3Slots = s.comment("Slots of the tier 3 bag")
					.worldRestart()
					.defineInRange("settings.tier3Slots", 18, 1, 54);
			
			tier4Slots = s.comment("Slots of the tier 4 bag")
					.worldRestart()
					.defineInRange("settings.tier4Slots", 27, 1, 54);
			
			tier5Slots = s.comment("Slots of the tier 5 bag")
					.worldRestart()
					.defineInRange("settings.tier5Slots", 36, 1, 54);
			
			addUpdateRecipes = s.comment("If recipes should be added that update a bag's modules (needed when new mods are added which add modules)")
					.worldRestart()
					.define("settings.addUpdateRecipes", false);
			
			drawWorkingState = c.comment("Whether the bag should render spinning gears while it is performing work")
					.define("settings.drawWorkingState", true);
				
			playFailSounds = c.comment("Whether a sound should be played if a block can't be placed")
					.define("settings.playFailSounds", true);
			
			playPickBlockSounds = c.comment("Whether a sound should be played if a pick-block action was successful")
					.define("settings.playPickBlockSounds", true);
			
			tier1Modules = s.comment("Modules of the tier 1 bag")
					.worldRestart()
					.defineList("settings.tier1Modules", Arrays.asList(BuildersBagRegistry.getDefaultModulesForTier(1)), (obj) -> obj instanceof String);
			
			tier2Modules = s.comment("Modules of the tier 2 bag")
					.worldRestart()
					.defineList("settings.tier2Modules", Arrays.asList(BuildersBagRegistry.getDefaultModulesForTier(2)), (obj) -> obj instanceof String);
			
			tier3Modules = s.comment("Modules of the tier 3 bag")
					.worldRestart()
					.defineList("settings.tier3Modules", Arrays.asList(BuildersBagRegistry.getDefaultModulesForTier(3)), (obj) -> obj instanceof String);
			
			tier4Modules = s.comment("Modules of the tier 4 bag")
					.worldRestart()
					.defineList("settings.tier4Modules", Arrays.asList(BuildersBagRegistry.getDefaultModulesForTier(4)), (obj) -> obj instanceof String);
			
			tier5Modules = s.comment("Modules of the tier 5 bag")
					.worldRestart()
					.defineList("settings.tier5Modules", Arrays.asList(BuildersBagRegistry.getDefaultModulesForTier(5)), (obj) -> obj instanceof String);
			
			
		}
		
	}

}
