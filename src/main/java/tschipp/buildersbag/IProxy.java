package tschipp.buildersbag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface IProxy
{
	public PlayerEntity getPlayer();

	public World getWorld();

	public void changeWorkState(String uuid, PlayerEntity player, boolean start);
	
	default Dist getSide()
	{
		return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER ? Dist.DEDICATED_SERVER : Dist.CLIENT;
	}
}
