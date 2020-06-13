package tschipp.buildersbag.compat.gamestages;

import net.darkhax.orestages.api.OreTiersAPI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.Tuple;

public class OreStageHelper
{

	public static Tuple<String, IBlockState> getOreStage(IBlockState block)
	{
		if(OreTiersAPI.hasReplacement(block))
		{
			return OreTiersAPI.getStageInfo(block);
		}
		
		return new Tuple("", Blocks.AIR.getDefaultState());
	}

}
