package tschipp.buildersbag.common.helper;

import java.util.Collection;
import java.util.HashSet;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.Loader;
import tschipp.buildersbag.compat.gamestages.StageHelper;

public class FakePlayerCopy extends FakePlayer
{

	public Collection<String> stages = new HashSet<String>();
	public PlayerEntity original;
	
	public FakePlayerCopy(WorldServer world, GameProfile name, PlayerEntity toCopyFrom)
	{
		super(world, name);
		this.inventory.readFromNBT(toCopyFrom.inventory.writeToNBT(new NBTTagList()).copy());
		this.enderChest.loadInventoryFromNBT(toCopyFrom.getInventoryEnderChest().saveInventoryToNBT().copy());
		
		if(Loader.isModLoaded("gamestages"))
			stages = StageHelper.getStages(toCopyFrom);
		
		this.inventory.currentItem = toCopyFrom.inventory.currentItem;		
	}

}
