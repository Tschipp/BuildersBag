package tschipp.buildersbag.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import tschipp.buildersbag.api.datastructures.RequirementListener;

public class BagModuleType<T extends IBagModule> extends ForgeRegistryEntry<BagModuleType<T>>
{
	private final Supplier<T> factory;
	private final Supplier<RequirementListener.Builder> listenerFactory;
	private final int[] bagLevels;

	private RequirementListener listener = null;

	private BagModuleType(Supplier<T> factory, Supplier<RequirementListener.Builder> listener, int[] bagLevels)
	{
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(listener);
		Preconditions.checkNotNull(bagLevels);

		this.factory = factory;
		this.listenerFactory = listener;

		Arrays.sort(bagLevels);
		Preconditions.checkArgument(Arrays.stream(bagLevels).distinct().allMatch(i -> i >= 1 && i <= 5), "Bag levels must be between 1 and 5!");
		this.bagLevels = Arrays.stream(bagLevels).distinct().toArray();
	}

	public static <T extends IBagModule> BagModuleType<T> create(ResourceLocation name, Supplier<T> factory, Supplier<RequirementListener.Builder> listenerFactory, int... levels)
	{
		return new BagModuleType<T>(factory, listenerFactory, levels).setRegistryName(name);
	}

	public T create()
	{
		return factory.get();
	}
	
	public List<Integer> getBagLevels()
	{
		return Arrays.stream(bagLevels).boxed().collect(Collectors.toList());
	}

	/*
	 * The listener gets created for the first time as the last step of the
	 * registration phase, when all recipes and blocks have been registred.
	 */
	// TODO: should have functionality to be reloaded, because (certain) recipes
	// can be reloaded.
	public RequirementListener getListener()
	{
		if (listener == null)
		{
			RequirementListener.Builder builder = listenerFactory.get();
			builder.setType(this);
			listener = builder.build();
		}
		return listener;
	}
}
