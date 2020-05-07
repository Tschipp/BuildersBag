package tschipp.buildersbag.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.caps.BagCap;
import tschipp.buildersbag.common.caps.BagCapStorage;
import tschipp.buildersbag.common.item.BuildersBagItem;
import tschipp.buildersbag.common.modules.ChiselModule;
import tschipp.buildersbag.common.modules.ChiselsBitsModule;
import tschipp.buildersbag.common.modules.CraftingModule;
import tschipp.buildersbag.common.modules.LittleTilesModule;
import tschipp.buildersbag.common.modules.RandomnessModule;
import tschipp.buildersbag.common.modules.SupplierModule;

@EventBusSubscriber(modid = BuildersBag.MODID)
public class RegistryHandler
{

	private static Map<ResourceLocation, Supplier<IBagModule>> modules = new HashMap<ResourceLocation, Supplier<IBagModule>>();

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
		addModule(new ResourceLocation(BuildersBag.MODID, "random"), RandomnessModule::new);
		addModule(new ResourceLocation(BuildersBag.MODID, "crafting"), CraftingModule::new);
		addModule(new ResourceLocation(BuildersBag.MODID, "supplier"), SupplierModule::new);
		
		if (Loader.isModLoaded("chisel"))
			addModule(new ResourceLocation(BuildersBag.MODID, "chisel"), ChiselModule::new);
		
		if (Loader.isModLoaded("littletiles"))
			addModule(new ResourceLocation(BuildersBag.MODID, "littletiles"), LittleTilesModule::new);
		
		if (Loader.isModLoaded("chiselsandbits"))
			addModule(new ResourceLocation(BuildersBag.MODID, "chiselsandbits"), ChiselsBitsModule::new);
	}

	@Nullable
	public static IBagModule getModule(ResourceLocation loc)
	{
		Supplier<IBagModule> sup = modules.get(loc);
		if (sup != null)
			return sup.get();

		return null;
	}

	public static void addModule(ResourceLocation name, Supplier<IBagModule> supplier)
	{
		modules.put(name, supplier);
	}

	public static void registerCapabilities()
	{
		CapabilityManager.INSTANCE.register(IBagCap.class, new BagCapStorage(), BagCap::new);
	}

}
