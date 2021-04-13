package tschipp.buildersbag.common.config;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.BuildersBagRegistry;

@Config(modid = BuildersBag.MODID)
public class BuildersBagConfig
{	

	@Config.LangKey(BuildersBag.MODID)
	@Config.Comment("General Mod Settings")
	public static Configs.Settings Settings = new Configs.Settings();

	@Mod.EventBusSubscriber
	public static class EventHandler
	{
		/**
		 * The {@link ConfigManager#CONFIGS} getter.
		 */
		private static final MethodHandle CONFIGS_GETTER = ReflectionUtil.findFieldGetter(ConfigManager.class, "CONFIGS");

		/**
		 * The {@link Configuration} instance.
		 */
		private static Configuration configuration;

		/**
		 * Get the {@link Configuration} instance from {@link ConfigManager}.
		 * <p>
		 * TODO: Use a less hackish method of getting the
		 * {@link Configuration}/{@link IConfigElement}s when possible.
		 *
		 * @return The Configuration instance
		 */
		public static Configuration getConfiguration()
		{
			if (EventHandler.configuration == null)
				try
				{
					final String fileName = BuildersBag.MODID + ".cfg";

					@SuppressWarnings("unchecked")
					final Map<String, Configuration> configsMap = (Map<String, Configuration>) EventHandler.CONFIGS_GETTER.invokeExact();

					final Optional<Map.Entry<String, Configuration>> entryOptional = configsMap.entrySet().stream().filter(entry -> fileName.equals(new File(entry.getKey()).getName())).findFirst();

					entryOptional.ifPresent(stringConfigurationEntry -> EventHandler.configuration = stringConfigurationEntry.getValue());
				} catch (Throwable throwable)
				{
					throwable.printStackTrace();
				}

			return EventHandler.configuration;
		}

		/**
		 * Inject the new values and save to the config file when the config has
		 * been changed from the GUI.
		 *
		 * @param event
		 *            The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
		{
			if (event.getModID().equals(BuildersBag.MODID))
				ConfigManager.load(BuildersBag.MODID, Config.Type.INSTANCE);
		}
		
		

	}

	public static void addToCurrentConfig(String modid)
	{
		if(BuildersBag.isNewlyGenerated)
			return;
		
		Configuration config = EventHandler.getConfiguration();
		List<ResourceLocation> modules = BuildersBagRegistry.getModulesFromMod(modid);
		
		for(ResourceLocation module : modules)
		{
			List<Integer> tiers = BuildersBagRegistry.getTiersForModule(module);
			for(int tier : tiers)
			{
				Property prop = config.get("general.settings", "tier" + tier + "Modules", BuildersBagRegistry.getDefaultModulesForTier(tier));
				
				if(prop != null)
				{
					String[] current = prop.getStringList();
					List<String> currentList = Lists.newArrayList(current);
					currentList.add(module.toString());
					prop.set(currentList.toArray(new String[currentList.size()]));
				}
			}
		}
		
		config.save();
		ConfigManager.load(BuildersBag.MODID, Config.Type.INSTANCE);
	}

	public static void setDefaultsOnFirstLoad()
	{
		if(BuildersBag.isNewlyGenerated)
		{
			Configuration config = EventHandler.getConfiguration();
			for(int i = 1; i <= 5; i++)
			{
				Property prop = config.get("general.settings", "tier" + i + "Modules", BuildersBagRegistry.getDefaultModulesForTier(i));
				prop.setDefaultValues(BuildersBagRegistry.getDefaultModulesForTier(i));
				prop.set(BuildersBagRegistry.getDefaultModulesForTier(i));
			}
			config.save();
			ConfigManager.load(BuildersBag.MODID, Config.Type.INSTANCE);
		}
	}
	
}
