package tschipp.buildersbag.api;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.codehaus.plexus.util.FileUtils;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.Event;
import tschipp.buildersbag.BuildersBag;

public class RequirementListener
{
	/*
	 * Contains Map with Ingredient as key and RequirementItem as Value. Is a
	 * multimap, so keeps track of all RequitementItems where the value item is
	 * used in.
	 */

	private final Multimap<IngredientKey, ItemCreationRequirements> listeners;
	private final BagModuleType<?> moduleType;

	public void notifyAdded(Item item, BagComplex complex)
	{
		IBagModule module = complex.getModule(moduleType);
		if (module == null)
		{
			BuildersBag.LOGGER.error("Complex didn't contain expected Bag Module");
			return;
		}

		if (!module.isEnabled())
			return;
		
		for (IngredientKey key : IngredientMapper.getKeys(item))
		{
			Collection<ItemCreationRequirements> updated = listeners.get(key);
			if (updated.isEmpty())
				continue;

			for (ItemCreationRequirements req : updated)
				module.getCreateableItemsManager().add(complex, req, key, item);
		}
	}

	public void notifyRemoved(Item item, BagComplex complex)
	{

		IBagModule module = complex.getModule(moduleType);
		if (module == null)
		{
			BuildersBag.LOGGER.error("Complex didn't contain expected Bag Module");
			return;
		}

		if (!module.isEnabled())
			return;
		
		for (IngredientKey key : IngredientMapper.getKeys(item))
		{
			Collection<ItemCreationRequirements> updated = listeners.get(key);
			if (updated.isEmpty())
				continue;

			for (ItemCreationRequirements req : updated)
				module.getCreateableItemsManager().remove(complex, req, key, item);
		}
	}

	public void printGraph()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("digraph g1 {\n");

		sb.append("size=\"30,10\";\r\n" + "    page=\"8.5,11\";\r\n" + "    ratio=fill;\n");

		for (Entry<IngredientKey, ItemCreationRequirements> entry : listeners.entries())
		{
			ItemCreationRequirements req = entry.getValue();
			String s = "\"Ingredient: " + entry.getKey().toString() + "\" -> \"Output: " + req.getOutput() + "\"\n";
			if (!sb.toString().contains(s))
				sb.append(s);
		}
		sb.append("}");
		try
		{
			FileUtils.fileWrite(new File("graph.txt"), sb.toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private <T extends Event> RequirementListener(Multimap<IngredientKey, ItemCreationRequirements> listeners, BagModuleType<?> type)
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
		private com.google.common.collect.ImmutableMultimap.Builder<IngredientKey, ItemCreationRequirements> builder = ImmutableMultimap.builder();
		private BagModuleType<?> moduleType;

		public <T> Builder add(Item created, @Nullable T meta, IngredientKey... required)
		{
			ItemCreationRequirements reqItem = new ItemCreationRequirements(created, meta, required);
			for (IngredientKey req : required)
			{
				builder.put(req, reqItem);
			}

			return this;
		}

		public <T> Builder add(Item created, @Nullable T meta, Collection<IngredientKey> required)
		{
			ItemCreationRequirements reqItem = new ItemCreationRequirements(created, meta, required.toArray(new IngredientKey[required.size()]));
			for (IngredientKey req : required)
			{
				builder.put(req, reqItem);
			}

			return this;
		}

		/*
		 * For internal use only
		 */
		void setType(BagModuleType<?> t)
		{
			moduleType = t;
		}

		public RequirementListener build()
		{
			return new RequirementListener(builder.build(), moduleType);
		}

	}

	public static class ItemCreationRequirements
	{
		private final Item output;
		private final Set<IngredientKey> requirements;
		private final Object meta;

		private <T> ItemCreationRequirements(Item output, @Nullable T meta, IngredientKey... requirements)
		{
			this.output = output;
			com.google.common.collect.ImmutableSet.Builder<IngredientKey> builder = ImmutableSet.builder();

			for (IngredientKey i : requirements)
				builder.add(i);

			this.meta = meta;

			this.requirements = builder.build();
		}

		public Item getOutput()
		{
			return output;
		}

		public Set<IngredientKey> getRequirements()
		{
			return requirements;
		}

		@SuppressWarnings("unchecked")
		public <T> T getMeta()
		{
			return (T) meta;
		}

		@Override
		public String toString()
		{
			return requirements + " -> " + output;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((meta == null) ? 0 : meta.hashCode());
			result = prime * result + ((output == null) ? 0 : output.hashCode());
			result = prime * result + ((requirements == null) ? 0 : requirements.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ItemCreationRequirements other = (ItemCreationRequirements) obj;
			if (meta == null)
			{
				if (other.meta != null)
					return false;
			}
			else if (!meta.equals(other.meta))
				return false;
			if (output == null)
			{
				if (other.output != null)
					return false;
			}
			else if (!output.equals(other.output))
				return false;
			if (requirements == null)
			{
				if (other.requirements != null)
					return false;
			}
			else if (!requirements.equals(other.requirements))
				return false;
			return true;
		}	
	}
}
