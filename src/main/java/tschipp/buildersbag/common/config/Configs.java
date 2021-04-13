package tschipp.buildersbag.common.config;

import tschipp.buildersbag.common.BuildersBagRegistry;

public class Configs {
	
	public static class Settings
	{
		@Config.RangeInt(min = 1, max = 54)
		@Config.RequiresMcRestart
		@Comment("Slots of the tier 1 bag")
		public int tier1Slots = 9;
		
		@Config.RequiresMcRestart
		@Comment("Modules of the tier 1 bag")
		public String[] tier1Modules = BuildersBagRegistry.getDefaultModulesForTier(1);
		
		
		
		@Config.RangeInt(min = 1, max = 54)
		@Config.RequiresMcRestart
		@Comment("Slots of the tier 2 bag")
		public int tier2Slots = 18;
		
		@Config.RequiresMcRestart
		@Comment("Modules of the tier 2 bag")
		public String[] tier2Modules = BuildersBagRegistry.getDefaultModulesForTier(2);
		
		
		
		@Config.RangeInt(min = 1, max = 54)
		@Config.RequiresMcRestart
		@Comment("Slots of the tier 3 bag")
		public int tier3Slots = 18;
		
		@Config.RequiresMcRestart
		@Comment("Modules of the tier 3 bag")
		public String[] tier3Modules = BuildersBagRegistry.getDefaultModulesForTier(3);
		
		
		
		@Config.RangeInt(min = 1, max = 54)
		@Config.RequiresMcRestart
		@Comment("Slots of the tier 4 bag")
		public int tier4Slots = 27;
		
		@Config.RequiresMcRestart
		@Comment("Modules of the tier 4 bag")
		public String[] tier4Modules = BuildersBagRegistry.getDefaultModulesForTier(4);
		
		
		
		@Config.RangeInt(min = 1, max = 54)
		@Config.RequiresMcRestart
		@Comment("Slots of the tier 5 bag")
		public int tier5Slots = 36;
		
		@Config.RequiresMcRestart
		@Comment("Modules of the tier 5 bag")
		public String[] tier5Modules = BuildersBagRegistry.getDefaultModulesForTier(5);

		@Config.RangeInt(min = 1, max = 100)
		@Comment("Maximum steps (crafting steps, chisel steps) that a block can use in order to be created.")
		public int maximumRecursionDepth = 30;
		
		@Config.RequiresMcRestart
		@Comment("If recipes should be added that update a bag's modules (needed when new mods are added which add modules)")
		public boolean addUpdateRecipes = false;
		
		@Comment("Whether the bag should render spinning gears while it is performing work")
		public boolean drawWorkingState = true;

		@Comment("Whether a sound should be played if a block can't be placed")
		public boolean playFailSounds = true;
		
		@Comment("Whether a sound should be played if a pick-block action was successful")
		public boolean playPickBlockSounds = true;
	}

}
