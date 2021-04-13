package tschipp.buildersbag.compat.gamestages;

import java.lang.reflect.Method;
import java.util.Collection;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import tschipp.buildersbag.common.helper.FakePlayerCopy;

public class StageHelper
{
	private static Class<?> gameStageHelper;
	private static Class<?> iStageHelper;

	private static Method getPlayerData;
	private static Method hasStage;

	private static boolean usesNewVersion = true;

	private static Class<?> playerDataHandler;
	private static Class<?> iStageData;

	private static Method getStageData;
	private static Method hasUnlockedStage;

	static
	{
		if (ModList.get().isLoaded("gamestages"))
		{
			try
			{
				gameStageHelper = Class.forName("net.darkhax.gamestages.GameStageHelper");
				iStageData = Class.forName("net.darkhax.gamestages.data.IStageData");

				getPlayerData = ReflectionHelper.findMethod(gameStageHelper, "getPlayerData", null, PlayerEntity.class);
				hasStage = ReflectionHelper.findMethod(iStageData, "hasStage", null, String.class);

			} catch (Exception e)
			{
				try
				{
					playerDataHandler = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler");
					iStageData = Class.forName("net.darkhax.gamestages.capabilities.PlayerDataHandler$IStageData");

					getStageData = ReflectionHelper.findMethod(playerDataHandler, "getStageData", null, PlayerEntity.class);
					hasUnlockedStage = ReflectionHelper.findMethod(iStageData, "hasUnlockedStage", null, String.class);

					usesNewVersion = false;

				} catch (Exception ex)
				{
				}
			}
		}
	}

	public static String getItemStage(ItemStack stack)
	{
		if(ModList.get().isLoaded("itemstages"))
			return ItemStageHelper.getItemStage(stack);
		return "";
	}
	
	public static Tuple<String, IBlockState> getOreStage(IBlockState block)
	{
		if(ModList.get().isLoaded("orestages"))
			return OreStageHelper.getOreStage(block);
		return new Tuple("", Blocks.AIR.getDefaultState());
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
			
			if(player instanceof FakePlayerCopy)
			{
				return ((FakePlayerCopy) player).stages.contains(stage);
			}
			
			if (usesNewVersion)
			{
				try
				{
					Object stageData = getPlayerData.invoke(null, player);
					boolean has = (boolean) hasStage.invoke(stageData, stage);

					return has;
				} catch (Exception e)
				{
					return true;
				}
			} else
			{
				try
				{
					Object stageData = getStageData.invoke(null, player);
					boolean has = (boolean) hasUnlockedStage.invoke(stageData, stage);

					return has;
				} catch (Exception e)
				{
					return true;
				}
			}
		}

		return true;
	}
	
	
	

}
