package tschipp.buildersbag.common.helper;

import java.util.Collection;
import java.util.HashSet;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.ModList;
import tschipp.buildersbag.compat.gamestages.StageHelper;

public class FakePlayerCopy extends FakePlayer
{

	public Collection<String> stages = new HashSet<String>();
	public PlayerEntity original;
	
	public FakePlayerCopy(ServerWorld world, GameProfile name, PlayerEntity toCopyFrom)
	{
		super(world, name);
		this.inventory.load(toCopyFrom.inventory.save(new ListNBT()).copy());
//		this.enderChest.loadInventoryFromNBT(toCopyFrom.getEnderChestInventory().saveInventoryToNBT().copy());
		
		if(ModList.get().isLoaded("gamestages"))
			stages = StageHelper.getStages(toCopyFrom);
		
		this.inventory.selected = toCopyFrom.inventory.selected;		
	}

}
