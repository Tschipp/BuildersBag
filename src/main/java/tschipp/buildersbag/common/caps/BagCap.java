package tschipp.buildersbag.common.caps;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.datastructures.BagComplex;
import tschipp.buildersbag.common.BuildersBagRegistry;
import tschipp.buildersbag.common.config.Configs;
import tschipp.buildersbag.common.inventory.BagItemStackHandler;
import tschipp.buildersbag.common.inventory.SelectedBlockHandler;

public class BagCap implements IBagCap
{
	private IBagModule[] modules = new IBagModule[0];
	private ItemStackHandler inv = new ItemStackHandler(0);
	private ItemStackHandler selected = new SelectedBlockHandler(1);
	private String uuid;
	private List<ItemStack> palette = new ArrayList<ItemStack>();
	private BagComplex complex = null;
	
	public BagCap()
	{
		this(1);
		
		this.uuid = "";
	}
	
	public BagCap(int tier)
	{
		reInit(tier);
		
		this.complex = new BagComplex(this);
	}
	
	private void initModules(List<? extends String> modules)
	{
		List<IBagModule> moduleList = new ArrayList<IBagModule>();
		Lists.newArrayList(modules).stream().distinct().sorted().forEach(s -> {
			IBagModule module = BuildersBagRegistry.createModule(new ResourceLocation(s));
			if (module != null)
				moduleList.add(module);
		});
		this.modules = moduleList.toArray(new IBagModule[moduleList.size()]);
	}

	@Override
	public ItemStackHandler getBlockInventory()
	{
		return inv;
	}

	@Override
	public IBagModule[] getModules()
	{
		return modules;
	}

	@Override
	public void setBlockInventory(ItemStackHandler handler)
	{
		this.inv = handler;
	}

	@Override
	public void setModules(IBagModule[] modules)
	{
		this.modules = modules;
	}

	@Override
	public void setSelectedInventory(ItemStackHandler handler)
	{
		this.selected = handler;
	}

	@Override
	public ItemStackHandler getSelectedInventory()
	{
		return selected;
	}

	@Override
	public boolean hasModuleAndEnabled(String name)
	{
		for(IBagModule m : modules)
			if(m.getName().equals(name) && m.isEnabled())
				return true;
		return false;
	}

	@Override
	public void transferDataFromCap(IBagCap from)
	{
		this.selected = from.getSelectedInventory();
		ItemStackHandler oldInv = from.getBlockInventory();
		
		for(int i = 0; i < oldInv.getSlots(); i++)
		{
			if(i < inv.getSlots())
			{
				inv.setStackInSlot(i, oldInv.getStackInSlot(i));
			}
		}
		
		for(int i = 0; i < modules.length; i++)
		{
			IBagModule module = modules[i];
			
			for(IBagModule fromModule : from.getModules())
			{
				if(module.getName().equals(fromModule.getName()))
				{
					modules[i] = fromModule;
					break;
				}
			}
		}
		
	}

	@Override
	public void reInit(int tier)
	{
		modules = null;
		switch (tier)
		{
		case 1:
			initModules(Configs.Settings.tier1Modules.get());
			inv = new BagItemStackHandler(Configs.Settings.tier1Slots.get());
			break;
		case 2:
			initModules(Configs.Settings.tier2Modules.get());
			inv = new BagItemStackHandler(Configs.Settings.tier2Slots.get());
			break;
		case 3:
			initModules(Configs.Settings.tier3Modules.get());
			inv = new BagItemStackHandler(Configs.Settings.tier3Slots.get());
			break;
		case 4:
			initModules(Configs.Settings.tier4Modules.get());
			inv = new BagItemStackHandler(Configs.Settings.tier4Slots.get());
			break;
		case 5:
			initModules(Configs.Settings.tier5Modules.get());
			inv = new BagItemStackHandler(Configs.Settings.tier5Slots.get());
			break;
		}
	}

	@Override
	public IBagCap copy()
	{
		BagCap newcap = new BagCap();
		newcap.inv = new ItemStackHandler(this.inv.getSlots());
		newcap.modules = new IBagModule[this.modules.length];
		newcap.selected.setStackInSlot(0, this.selected.getStackInSlot(0).copy());
		
		for(int i = 0; i < this.inv.getSlots(); i++)
			newcap.inv.setStackInSlot(i, this.inv.getStackInSlot(i).copy());
		
		for(int i = 0; i < this.modules.length; i++)
		{
			IBagModule newModule = BuildersBagRegistry.createModule(new ResourceLocation(this.modules[i].getName()));
			newModule.deserializeNBT(this.modules[i].serializeNBT());
			newcap.modules[i] = newModule;
		}
		
		return newcap;
	}

	@Override
	public String getUUID()
	{
		return uuid;
	}

	@Override
	public void setUUID(String uuid)
	{
		this.uuid = uuid;
	}

	@Override
	public List<ItemStack> getPalette()
	{
		return this.palette;
	}

	@Override
	public void setPalette(List<ItemStack> list)
	{
		this.palette = list;
	}

	@Override
	public BagComplex getComplex()
	{
		return complex;
	}
}
