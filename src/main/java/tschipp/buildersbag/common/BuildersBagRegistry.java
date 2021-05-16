package tschipp.buildersbag.common;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryBuilder;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.BagModuleType;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.datastructures.RequirementListener;
import tschipp.buildersbag.common.caps.BagCap;
import tschipp.buildersbag.common.caps.BagCapStorage;
import tschipp.buildersbag.common.inventory.ContainerBag;
import tschipp.buildersbag.common.inventory.ContainerBag.BagContainerFactory;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.common.modules.RandomnessModule;
import tschipp.buildersbag.common.modules.SupplierModule;

@ObjectHolder(BuildersBag.MODID)
@EventBusSubscriber(modid = BuildersBag.MODID, bus = Bus.MOD)
public class BuildersBagRegistry
{
	public static IForgeRegistry<BagModuleType<?>> REGISTRY_MODULES;

	@ObjectHolder("builders_bag_tier_one")
	public static final BuildersBagItem TIER_1 = null;
	
	@ObjectHolder("builders_bag_tier_two")
	public static final BuildersBagItem TIER_2 = null;
	
	@ObjectHolder("builders_bag_tier_three")
	public static final BuildersBagItem TIER_3 = null;
	
	@ObjectHolder("builders_bag_tier_four")
	public static final BuildersBagItem TIER_4 = null;
	
	@ObjectHolder("builders_bag_tier_five")
	public static final BuildersBagItem TIER_5 = null;
	
	@ObjectHolder("buildersbag")
	public static final ContainerType<ContainerBag> BAG_CONTAINER_TYPE = null;
	
	
	
	@ObjectHolder("random")
	public static final BagModuleType<RandomnessModule> MODULE_RANDOM = null;

	@ObjectHolder("supplier")
	public static final BagModuleType<SupplierModule> MODULE_SUPPLIER = null;
	
	@SubscribeEvent
	public static void onRegistriesRegister(RegistryEvent.NewRegistry event)
	{
		RegistryBuilder<BagModuleType<?>> builder = new RegistryBuilder<>();
		builder.setType(c(BagModuleType.class));
		builder.setName(rs("modules"));
		REGISTRY_MODULES = builder.create();
	}
	
	
	@SubscribeEvent
	public static void onItemsRegister(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> reg = event.getRegistry();
		
		reg.registerAll(new BuildersBagItem(1, "one"),
					    new BuildersBagItem(2, "two"),
		                new BuildersBagItem(3, "three"),
		                new BuildersBagItem(4, "four"),
		                new BuildersBagItem(5, "five"));		
	}

	@SubscribeEvent
	public static void onContainerRegister(RegistryEvent.Register<ContainerType<?>> event)
	{
		event.getRegistry().register(IForgeContainerType.create(new BagContainerFactory()).setRegistryName(BuildersBag.MODID, "buildersbag"));
	}

	@SubscribeEvent
	public static void onBagModuleRegister(RegistryEvent.Register<BagModuleType<?>> event)
	{
		event.getRegistry().registerAll(
				BagModuleType.create(rs("random"), RandomnessModule::new,  RequirementListener::builder, 1, 2, 3, 4, 5),
				BagModuleType.create(rs("supplier"), SupplierModule::new,  RequirementListener::builder, 5)
				);
	}
	
	private static ResourceLocation rs(String name)
	{
		return new ResourceLocation(BuildersBag.MODID, name);
	}

	@SuppressWarnings("unchecked") //Ugly hack to let us pass in a typed Class object. Remove when we remove type specific references.
    private static <T> Class<T> c(Class<?> cls) { return (Class<T>)cls; }

	@Nullable
	public static IBagModule createModule(ResourceLocation loc)
	{
		BagModuleType<?> type = REGISTRY_MODULES.getValue(loc);

		if(type == null)
			return null;
		
		return type.create();
	}

//	/** REMOVE, use registry events
//	 * Modders: Don't use this method, use the one in
//	 * {@link tschipp.buildersbag.api.ModuleRegistry#registerModule} Registers a
//	 * module that gets instantiated with the given supplier The bag stages
//	 * determine by what stage the module is unlocked by default.
//	 * 
//	 * @param name
//	 *            the registry name of the module
//	 * @param supplier
//	 *            the supplier
//	 * @param bagStages
//	 *            the bag stages, ints ranging from 1 - 5 (inclusive)
//	 */
//	public static void addModule(ResourceLocation name, Supplier<IBagModule> supplier, int... bagStages)
//	{
//		modules.put(name, supplier);
//		Set<Integer> list = new HashSet<Integer>();
//		for (int i : bagStages)
//			if (i >= 1 && i <= 5)
//				list.add(i);
//		defaultModuleStages.put(name, list);
//	}

//	public static void sayHi(String modid)
//	{
//		Set<String> seenMods = BuildersBag.getSeenMods();
//		if (!seenMods.contains(modid))
//		{
//			BuildersBagConfig.addToCurrentConfig(modid);
//
//			try
//			{
//				FileWriter writer = new FileWriter(BuildersBag.seenModsFile, true);
//				writer.append(modid + "\n");
//				writer.close();
//			}
//			catch (IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}

	public static String[] getDefaultModulesForTier(int tier)
	{
		return new String[] {"buildersbag:random", "buildersbag:supplier"};
//		return REGISTRY_MODULES.getValues()
//		.stream()
//		.filter(type -> type.getBagLevels().contains(tier))
//		.map(type -> type.getRegistryName().toString())
//		.toArray(String[]::new);
	}

	public static List<Integer> getTiersForModule(ResourceLocation module)
	{
		BagModuleType<?> type = REGISTRY_MODULES.getValue(module);
		if(type == null)
			return Collections.emptyList();
		
		return type.getBagLevels();
	}

//	public static List<ResourceLocation> getModulesFromMod(String modid)
//	{
//		List<ResourceLocation> moduleL = new ArrayList<ResourceLocation>();
//		for (ResourceLocation module : modules.keySet())
//		{
//			if (module.getNamespace().equals(modid))
//			{
//				moduleL.add(module);
//			}
//		}
//		return moduleL;
//	}

	public static void registerCapabilities()
	{
		CapabilityManager.INSTANCE.register(IBagCap.class, new BagCapStorage(), BagCap::new);
	}
}
