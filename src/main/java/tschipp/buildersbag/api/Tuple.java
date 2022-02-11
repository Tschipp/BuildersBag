package tschipp.buildersbag.api;

public class Tuple<A, B>
{
	private A a;
	private B b;

	public Tuple(A aIn, B bIn)
	{
		this.a = aIn;
		this.b = bIn;
	}

	/**
	 * Get the first Object in the Tuple
	 */
	public A getFirst()
	{
		return this.a;
	}

	/**
	 * Get the second Object in the Tuple
	 */
	public B getSecond()
	{
		return this.b;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
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
		Tuple other = (Tuple) obj;
		if (a == null)
		{
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null)
		{
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return "<" + a.toString() + ", " + b.toString() + ">";
	}

}