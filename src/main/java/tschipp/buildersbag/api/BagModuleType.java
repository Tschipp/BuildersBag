package tschipp.buildersbag.api;

import com.google.common.base.Supplier;

import net.minecraftforge.registries.ForgeRegistryEntry;
import tschipp.buildersbag.api.datastructures.RequirementListener;

public class BagModuleType<T extends IBagModule> extends ForgeRegistryEntry<BagModuleType<T>>
{
	private final Supplier<T> factory;
	private final Supplier<RequirementListener.Builder> listenerFactory;
	
	private RequirementListener listener = null;
	
	private BagModuleType(Supplier<T> factory, Supplier<RequirementListener.Builder> listener)
	{
		this.factory = factory;
		this.listenerFactory = listener;
	}
	
	public static <T extends IBagModule> BagModuleType<T> create(Supplier<T> factory, Supplier<RequirementListener.Builder> listenerFactory)
	{
		return new BagModuleType<T>(factory, listenerFactory);
	}
	
	public T create()
	{
		return factory.get();
	}
	
	/*
	 * The listener gets created for the first time as the last step of the registration phase, when all recipes and blocks have been registred.
	 */
	public RequirementListener getListener()
	{
		if(listener == null)
		{
			RequirementListener.Builder builder = listenerFactory.get();
			builder.setType(this);
			listener = builder.build();
		}
		return listener;
	}
}
