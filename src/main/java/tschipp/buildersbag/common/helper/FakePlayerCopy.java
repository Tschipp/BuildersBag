package tschipp.buildersbag.common.helper;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

public class FakePlayerCopy extends FakePlayer
{

	public FakePlayerCopy(WorldServer world, GameProfile name, EntityPlayer toCopyFrom)
	{
		super(world, name);
		this.inventory.readFromNBT(toCopyFrom.inventory.writeToNBT(new NBTTagList()));
		this.enderChest.loadInventoryFromNBT(toCopyFrom.getInventoryEnderChest().saveInventoryToNBT());
	}

}
