package tschipp.buildersbag.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistryEntry;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.RequirementListener.Builder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BagModuleType<T extends IBagModule> extends ForgeRegistryEntry<BagModuleType<?>>
{
	private final Supplier<T> factory;
	private final Function<Event, RequirementListener.Builder> listenerEventFactory;
	private final Supplier<RequirementListener.Builder> listenerFactory;
	private final int[] bagLevels;

	private RequirementListener listener = null;

	private BagModuleType(Supplier<T> factory, Supplier<RequirementListener.Builder> listenerFactory, Function<Event, RequirementListener.Builder> listenerEventFactory, Class<? extends Event> eventClass, int[] bagLevels)
	{
		Preconditions.checkArgument(!(listenerFactory == null && listenerEventFactory == null));
		// Preconditions.checkNotNull(listener);
		Preconditions.checkNotNull(bagLevels);

		this.factory = factory;
		this.listenerFactory = listenerFactory;
		this.listenerEventFactory = listenerEventFactory;

		Arrays.sort(bagLevels);
		Preconditions.checkArgument(Arrays.stream(bagLevels).distinct().allMatch(i -> i >= 1 && i <= 5), "Bag levels must be between 1 and 5!");
		this.bagLevels = Arrays.stream(bagLevels).distinct().toArray();

		if (eventClass != null)
		{
			MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, eventClass, (event) -> {
				listener = null;
				RequirementListener.Builder builder = this.listenerEventFactory.apply(event);
				builder.setType(this);
				listener = builder.build();
			});
		}

	}

	@SuppressWarnings("unchecked")
	public static <T extends IBagModule, E extends Event> BagModuleType<T> create(ResourceLocation name, Supplier<T> factory, Function<E, RequirementListener.Builder> listenerEventFactory, Class<E> eventClass, int... levels)
	{
		return (BagModuleType<T>) new BagModuleType<T>(factory, null, (Function<Event, Builder>) listenerEventFactory, eventClass, levels).setRegistryName(name);
	}

	public static <T extends IBagModule> BagModuleType<T> create(ResourceLocation name, Supplier<T> factory, Supplier<RequirementListener.Builder> listenerFactory, int... levels)
	{
		return (BagModuleType<T>) new BagModuleType<T>(factory, listenerFactory, null, null, levels).setRegistryName(name);
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
			if (listenerFactory == null)
			{
				BuildersBag.LOGGER.error("Requirement Listener factory was null!");
				return null;
			}

			RequirementListener.Builder builder = listenerFactory.get();
			builder.setType(this);
			listener = builder.build();
		}
		return listener;
	}
}
