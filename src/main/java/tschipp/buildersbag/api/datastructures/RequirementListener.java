package tschipp.buildersbag.api.datastructures;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import net.minecraft.item.Item;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.BagModuleType;
import tschipp.buildersbag.api.IBagModule;

public class RequirementListener
{
	/*
	 * Contains Map with Item as key and RequirementItem as Value.
	 * Is a multimap, so keeps track of all RequitementItems where the value item is used in.
	 */

	private final Multimap<Item, RequirementItem> listeners;
	private final BagModuleType<?> moduleType;

	public void notifyAdded(Item item, BagComplex complex)
	{
		Collection<RequirementItem> updated = listeners.get(item);
		if (updated.isEmpty())
			return;

		IBagModule module = complex.getModule(moduleType);
		if (module == null)
		{
			BuildersBag.LOGGER.error("Complex didn't contain expected Bag Module");
			return;
		}
		
		if(!module.isEnabled())
			return;
			
		for (RequirementItem req : updated)
			module.getCreateableItemsManager().add(complex, req, item);
	}

	public void notifyRemoved(Item item, BagComplex complex)
	{
		Collection<RequirementItem> updated = listeners.get(item);
		if (updated.isEmpty())
			return;

		IBagModule module = complex.getModule(moduleType);
		if (module == null)
		{
			BuildersBag.LOGGER.error("Complex didn't contain expected Bag Module");
			return;
		}
		
		if(!module.isEnabled())
			return;

		for (RequirementItem req : updated)
			module.getCreateableItemsManager().remove(complex, req,  item);
	}

	private RequirementListener(Multimap<Item, RequirementItem> listeners, BagModuleType<?> type)
	{
		this.listeners = listeners;
		this.moduleType = type;
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static class Builder
	{
		private com.google.common.collect.ImmutableMultimap.Builder<Item, RequirementItem> builder = ImmutableMultimap.builder();
		private BagModuleType<?> moduleType;

		public Builder add(Item created, Item... required)
		{
			RequirementItem reqItem = new RequirementItem(created, required);
			for (Item req : required)
			{
				builder.put(req, reqItem);
			}

			return this;
		}

		/*
		 * For internal use only
		 */
		public void setType(BagModuleType<?> t)
		{
			moduleType = t;
		}

		public RequirementListener build()
		{
			return new RequirementListener(builder.build(), moduleType);
		}
	}

	public static class RequirementItem
	{
		private final Item output;
		private final Set<Item> requirements;

		private RequirementItem(Item output, Item... requirements)
		{
			this.output = output;
			com.google.common.collect.ImmutableSet.Builder<Item> builder = ImmutableSet.builder();

			for (Item i : requirements)
				builder.add(i);

			this.requirements = builder.build();
		}

		public Item getOutput()
		{
			return output;
		}

		public Set<Item> getRequirements()
		{
			return requirements;
		}
	}
}
