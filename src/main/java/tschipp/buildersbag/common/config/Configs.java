package tschipp.buildersbag.common.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

public class Configs {
	
	public static class Settings
	{
		@Config.RangeInt(min = 1, max = 54)
		@Config.RequiresMcRestart
		@Comment("Slots of the tier 1 bag")
		public int tier1Slots = 9;
		
		@Config.RangeInt(min = 1, max = 512)
		@Config.RequiresMcRestart
		@Comment("Stack size of the tier 1 bag")
		public int tier1StackSize = 64;
		
		@Config.RequiresMcRestart
		@Comment("Modules of the tier 1 bag")
		public String[] tier1Modules = new String[] {"buildersbag:random"};
		
		
		
		@Config.RangeInt(min = 1, max = 54)
		@Config.RequiresMcRestart
		@Comment("Slots of the tier 2 bag")
		public int tier2Slots = 18;
		
		@Config.RangeInt(min = 1, max = 512)
		@Config.RequiresMcRestart
		@Comment("Stack size of the tier 2 bag")
		public int tier2StackSize = 64;
		
		@Config.RequiresMcRestart
		@Comment("Modules of the tier 2 bag")
		public String[] tier2Modules = new String[] {"buildersbag:random"};
		
		
		
		@Config.RangeInt(min = 1, max = 54)
		@Config.RequiresMcRestart
		@Comment("Slots of the tier 3 bag")
		public int tier3Slots = 27;
		
		@Config.RangeInt(min = 1, max = 512)
		@Config.RequiresMcRestart
		@Comment("Stack size of the tier 3 bag")
		public int tier3StackSize = 64;
		
		@Config.RequiresMcRestart
		@Comment("Modules of the tier 3 bag")
		public String[] tier3Modules = new String[] {"buildersbag:random", "buildersbag:littletiles", "buildersbag:chiselsandbits"};
		
		
		
		@Config.RangeInt(min = 1, max = 54)
		@Config.RequiresMcRestart
		@Comment("Slots of the tier 4 bag")
		public int tier4Slots = 27;
		
		@Config.RangeInt(min = 1, max = 512)
		@Config.RequiresMcRestart
		@Comment("Stack size of the tier 4 bag")
		public int tier4StackSize = 96;
		
		@Config.RequiresMcRestart
		@Comment("Modules of the tier 4 bag")
		public String[] tier4Modules = new String[] {"buildersbag:random", "buildersbag:littletiles", "buildersbag:chiselsandbits", "buildersbag:crafting"};
		
		
		
		@Config.RangeInt(min = 1, max = 54)
		@Config.RequiresMcRestart
		@Comment("Slots of the tier 5 bag")
		public int tier5Slots = 27;
		
		@Config.RangeInt(min = 1, max = 512)
		@Config.RequiresMcRestart
		@Comment("Stack size of the tier 5 bag")
		public int tier5StackSize = 128;
		
		@Config.RequiresMcRestart
		@Comment("Modules of the tier 5 bag")
		public String[] tier5Modules = new String[] {"buildersbag:random", "buildersbag:littletiles", "buildersbag:chiselsandbits", "buildersbag:crafting"};

	}

}
