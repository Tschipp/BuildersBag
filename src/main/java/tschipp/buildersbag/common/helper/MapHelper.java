package tschipp.buildersbag.common.helper;

import java.util.Map;
import java.util.Map.Entry;

public class MapHelper
{
	@SuppressWarnings("unchecked")
	public static <K, V extends Number> void add(Map<K, V> map, K key, V value)
	{
		if(value.doubleValue() == 0)
			return;
		
		Number n = map.get(key);
		if (n == null)
			n = 0;
		if (n instanceof Integer)
			n = n.intValue() + value.intValue();
		else
			n = n.doubleValue() + value.doubleValue();
		map.put(key, (V) n);
	}

	/**
	 * Returns actual amount removed
	 */
	@SuppressWarnings("unchecked")
	public static <K, V extends Number> V removeAtMost(Map<K, V> map, K key, V max)
	{
		Number n = map.get(key);
		if (n == null)
			return (V) Integer.valueOf(0);
		Number toRemove;
		if (n instanceof Integer)
		{
			toRemove = Math.min(n.intValue(), max.intValue());
			n = n.intValue() - toRemove.intValue();
		}
		else
		{
			toRemove = Math.min(n.doubleValue(), max.doubleValue());
			n = n.doubleValue() - toRemove.doubleValue();
		}
		map.put(key, (V) n);
		return (V) toRemove;
	}

	public static <K, V extends Number> void merge(Map<K, V> map, Map<K, V> into)
	{
		for (Entry<K, V> entry : map.entrySet())
		{
			if (entry.getValue().doubleValue() != 0)
				add(into, entry.getKey(), entry.getValue());
		}
	}
}
