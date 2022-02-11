package tschipp.buildersbag.common.caps;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
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
	public INBT writeNBT(Capability<IBagCap> capability, IBagCap instance, Direction side)
	{
		CompoundNBT tag = new CompoundNBT();
		CompoundNBT inventory = instance.getBlockInventory().serializeNBT();
		CompoundNBT selected = instance.getSelectedInventory().serializeNBT();
		ListNBT modules = new ListNBT();
		ListNBT palette = new ListNBT();

		for(ItemStack stack : instance.getPalette())
		{
			palette.add(stack.serializeNBT());
		}
		
		for(IBagModule module : instance.getModules())
		{
			modules.add(module.serializeNBT());
		}
		
		tag.put("inventory", inventory);
		tag.put("modules", modules);
		tag.put("palette", palette);
		tag.put("selected", selected);
		tag.putString("uuid", instance.getUUID());

		return tag;
	}

	@Override
	public void readNBT(Capability<IBagCap> capability, IBagCap instance, Direction side, INBT nbt)
	{	
		CompoundNBT tag = (CompoundNBT) nbt;
		CompoundNBT inventory = tag.getCompound("inventory");
		CompoundNBT selected = tag.getCompound("selected");
		ListNBT modules = tag.getList("modules", 10);
		ListNBT palette = tag.getList("palette", 10);
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
		for(int i = 0; i < modules.size(); i++)
		{
			CompoundNBT module = modules.getCompound(i);
			IBagModule mod = BuildersBagRegistry.createModule(new ResourceLocation(module.getString("name")));
			if(mod != null)
			{
				mod.deserializeNBT(module);
				parsedModules.add(mod);
			}
		}
		instance.setModules(parsedModules.toArray(new IBagModule[parsedModules.size()]));
		
		List<ItemStack> paletteList = new ArrayList<ItemStack>();
		for(int i = 0; i < palette.size(); i++)
		{
			CompoundNBT stacktag = palette.getCompound(i);
			ItemStack stack = ItemStack.of(stacktag);
			if(!stack.isEmpty())
				paletteList.add(stack);
		}
		instance.setPalette(paletteList);
	}

}
