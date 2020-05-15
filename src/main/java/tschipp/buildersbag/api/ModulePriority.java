package tschipp.buildersbag.api;

public enum ModulePriority
{
	HIGHEST(4),
	HIGH(3),
	NORMAL(2),
	LOW(1),
	LOWEST(0);

	private int val;

	private ModulePriority(int val)
	{
		this.val = val;
	}

	public int getVal()
	{
		return val;
	}

}
