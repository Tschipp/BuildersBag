package tschipp.buildersbag.compat.bbw;

import portablejim.bbw.BetterBuildersWandsMod;

public class BBWCompat
{
	public static void register()
	{	
		BetterBuildersWandsMod.instance.containerManager.register(new ContainerHandlerBuildersBag());
	}
}
