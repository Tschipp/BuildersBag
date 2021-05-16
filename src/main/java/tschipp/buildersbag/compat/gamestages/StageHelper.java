package tschipp.buildersbag.compat.gamestages;

import java.util.Collection;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.ModList;

public class StageHelper // TODO
{

	public static String getItemStage(ItemStack stack)
	{
		// if(ModList.get().isLoaded("itemstages"))
		// return ItemStageHelper.getItemStage(stack);
		return "";
	}

	public static Tuple<String, BlockState> getOreStage(BlockState block)
	{
		// if(ModList.get().isLoaded("orestages"))
		// return OreStageHelper.getOreStage(block);
		return new Tuple<String, BlockState>("", Blocks.AIR.defaultBlockState());
	}

	public static Collection<String> getStages(PlayerEntity player)
	{
		IStageData data = GameStageHelper.getPlayerData(player);
		return data.getStages();
	}

	public static boolean hasStage(PlayerEntity player, String stage)
	{
		if (ModList.get().isLoaded("gamestages"))
		{
			if (stage.isEmpty())
				return true;

			return GameStageHelper.hasStage(player, stage);

			// if(player instanceof FakePlayerCopy) //Obsolete
			// {
			// return ((FakePlayerCopy) player).stages.contains(stage);
			// }

		}

		return true;
	}

}
