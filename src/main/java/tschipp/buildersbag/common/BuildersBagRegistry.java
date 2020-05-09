package tschipp.buildersbag.common;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.ModuleRegistry;
import tschipp.buildersbag.common.caps.BagCap;
import tschipp.buildersbag.common.caps.BagCapStorage;
import tschipp.buildersbag.common.config.BuildersBagConfig;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.common.modules.ChiselModule;
import tschipp.buildersbag.common.modules.ChiselsBitsModule;
import tschipp.buildersbag.common.modules.CraftingModule;
import tschipp.buildersbag.common.modules.LittleTilesModule;
import tschipp.buildersbag.common.modules.RandomnessModule;
import tschipp.buildersbag.common.modules.SupplierModule;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class BuildersBagRegistry
{

	private static final Map<ResourceLocation, Supplier<IBagModule>> modules = new HashMap<ResourceLocation, Supplier<IBagModule>>();
	private static final Map<ResourceLocation, Set<Integer>> defaultModuleStages = new HashMap<ResourceLocation, Set<Integer>>();
	
	public static Item tier1;
	public static Item tier2;
	public static Item tier3;
	public static Item tier4;
	public static Item tier5;

	public static List<Item> items = new ArrayList<Item>();

	public static void registerItems()
	{
		tier1 = new BuildersBagItem(1, "one");
		tier2 = new BuildersBagItem(2, "two");
		tier3 = new BuildersBagItem(3, "three");
		tier4 = new BuildersBagItem(4, "four");
		tier5 = new BuildersBagItem(5, "five");

		items.add(tier1);
		items.add(tier2);
		items.add(tier3);
		items.add(tier4);
		items.add(tier5);

	}

	public static void registerModules()
	{
		addModule(new ResourceLocation(BuildersBag.MODID, "random"), RandomnessModule::new, 1, 2, 3, 4, 5);
		addModule(new ResourceLocation(BuildersBag.MODID, "crafting"), CraftingModule::new, 4, 5);
		addModule(new ResourceLocation(BuildersBag.MODID, "supplier"), SupplierModule::new, 5);
		
		if (Loader.isModLoaded("chisel"))
			addModule(new ResourceLocation(BuildersBag.MODID, "chisel"), ChiselModule::new, 2, 3, 4, 5);
		
		if (Loader.isModLoaded("littletiles"))
			addModule(new ResourceLocation(BuildersBag.MODID, "littletiles"), LittleTilesModule::new, 3, 4, 5);
		
		if (Loader.isModLoaded("chiselsandbits"))
			addModule(new ResourceLocation(BuildersBag.MODID, "chiselsandbits"), ChiselsBitsModule::new, 3, 4, 5);
	
		ModuleRegistry.registerModule("test", new ResourceLocation("test", "test"), RandomnessModule::new, 1, 2, 4, 5);

	}

	@Nullable
	public static IBagModule getModule(ResourceLocation loc)
	{
		Supplier<IBagModule> sup = modules.get(loc);
		if (sup != null)
			return sup.get();

		return null;
	}

	/**
	 * Modders: Don't use this method, use the one in {@link tschipp.buildersbag.api.ModuleRegistry#registerModule}
	 * Registers a module that gets instantiated with the given supplier
	 * The bag stages determine by what stage the module is unlocked by default.
	 * @param name the registry name of the module
	 * @param supplier the supplier
	 * @param bagStages the bag stages, ints ranging from 1 - 5 (inclusive)
	 */
	public static void addModule(ResourceLocation name, Supplier<IBagModule> supplier, int... bagStages)
	{
		modules.put(name, supplier);
		Set<Integer> list = new HashSet<Integer>();
		for(int i : bagStages)
			if(i >= 1 && i <= 5)
				list.add(i);
		defaultModuleStages.put(name, list);
	}

	public static void sayHi(String modid)
	{
		Set<String> seenMods = BuildersBag.getSeenMods();
		if(!seenMods.contains(modid))
		{
			BuildersBagConfig.addToCurrentConfig(modid);
			
			try
			{
				FileWriter writer = new FileWriter(BuildersBag.seenModsFile, true);
				writer.append(modid + "\n");
				writer.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static String[] getDefaultModulesForTier(int tier)
	{
		List<String> modules = new ArrayList<String>();
		for(Entry<ResourceLocation, Set<Integer>> entry : defaultModuleStages.entrySet())
		{
			for(int t : entry.getValue())
			{
				if(t == tier)
				{
					modules.add(entry.getKey().toString());
					break;
				}
			}
		}
		
		return modules.toArray(new String[modules.size()]);
	}
	
	public static List<Integer> getTiersForModule(ResourceLocation module)
	{		
		return new ArrayList<Integer>(defaultModuleStages.get(module));
	}
	
	public static List<ResourceLocation> getModulesFromMod(String modid)
	{
		List<ResourceLocation> moduleL = new ArrayList<ResourceLocation>();
		for(ResourceLocation module : modules.keySet())
		{
			if(module.getResourceDomain().equals(modid))
			{
				moduleL.add(module);
			}
		}
		return moduleL;
	}
	
	
	public static void registerCapabilities()
	{
		CapabilityManager.INSTANCE.register(IBagCap.class, new BagCapStorage(), BagCap::new);
	}
}
