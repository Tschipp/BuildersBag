package tschipp.buildersbag.common.caps;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.BuildersBagRegistry;
import tschipp.buildersbag.common.inventory.BagItemStackHandler;
import tschipp.buildersbag.common.inventory.SelectedBlockHandler;

public class BagCapStorage implements IStorage<IBagCap>
{

	@Override
	public NBTBase writeNBT(Capability<IBagCap> capability, IBagCap instance, EnumFacing side)
	{
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagCompound inventory = instance.getBlockInventory().serializeNBT();
		NBTTagCompound selected = instance.getSelectedInventory().serializeNBT();
		NBTTagList modules = new NBTTagList();
		NBTTagList palette = new NBTTagList();

		for(ItemStack stack : instance.getPalette())
		{
			palette.appendTag(stack.serializeNBT());
		}
		
		for(IBagModule module : instance.getModules())
		{
			modules.appendTag(module.serializeNBT());
		}
		
		tag.setTag("inventory", inventory);
		tag.setTag("modules", modules);
		tag.setTag("palette", palette);
		tag.setTag("selected", selected);
		tag.setString("uuid", instance.getUUID());
		
		
		
		return tag;

	}

	@Override
	public void readNBT(Capability<IBagCap> capability, IBagCap instance, EnumFacing side, NBTBase nbt)
	{	
		NBTTagCompound tag = (NBTTagCompound) nbt;
		NBTTagCompound inventory = tag.getCompoundTag("inventory");
		NBTTagCompound selected = tag.getCompoundTag("selected");
		NBTTagList modules = tag.getTagList("modules", 10);
		NBTTagList palette = tag.getTagList("palette", 10);
		String uuid = tag.getString("uuid");
		
		if(uuid.isEmpty())
			uuid = UUID.randomUUID().toString();
		
		BagItemStackHandler handler = new BagItemStackHandler(0);
		handler.deserializeNBT(inventory);
		instance.setBlockInventory(handler);
		
		SelectedBlockHandler selectedHandler = new SelectedBlockHandler(1);
		selectedHandler.deserializeNBT(selected);
		instance.setSelectedInventory(selectedHandler);
		
		instance.setUUID(uuid);
		
		List<IBagModule> parsedModules = new ArrayList<IBagModule>();
		for(int i = 0; i < modules.tagCount(); i++)
		{
			NBTTagCompound module = modules.getCompoundTagAt(i);
			IBagModule mod = BuildersBagRegistry.getModule(new ResourceLocation(module.getString("name")));
			if(mod != null)
			{
				mod.deserializeNBT(module);
				parsedModules.add(mod);
			}
		}
		instance.setModules(parsedModules.toArray(new IBagModule[parsedModules.size()]));
		
		List<ItemStack> paletteList = new ArrayList<ItemStack>();
		for(int i = 0; i < palette.tagCount(); i++)
		{
			NBTTagCompound stacktag = palette.getCompoundTagAt(i);
			ItemStack stack = new ItemStack(stacktag);
			if(!stack.isEmpty())
				paletteList.add(stack);
		}
		instance.setPalette(paletteList);
	}

}
