package tschipp.buildersbag.api;

import java.util.function.Supplier;

import net.minecraft.util.ResourceLocation;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.common.BuildersBagRegistry;

public class ModuleRegistry
{
	/**
	 * Registers a module that gets instantiated with the given supplier
	 * The bag stages determine by what stage the module is unlocked by default.
	 * @param modid the modid of the mod that is registering this module (!= "buildersbag")
	 * @param moduleName the registry name of the module, with the same domain as the modid of the mod that is registering it
	 * @param supplier the supplier
	 * @param bagStages the bag stages, ints ranging from 1 - 5 (inclusive)
	 */
	public static void registerModule(String modid, ResourceLocation moduleName, Supplier<IBagModule> supplier, int... bagStages)
	{
		if(!modid.equals(moduleName.getNamespace()))
			BuildersBag.LOGGER.warn("Invalid domain for bag module " + moduleName + "! Please make sure the mod registering the module uses its own modid as the domain!");
			
		BuildersBagRegistry.addModule(moduleName, supplier, bagStages);
		BuildersBagRegistry.sayHi(modid);
	}
}
