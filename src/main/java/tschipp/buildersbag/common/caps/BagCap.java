package tschipp.buildersbag.common.caps;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.RegistryHandler;
import tschipp.buildersbag.common.config.BuildersBagConfig;
import tschipp.buildersbag.common.inventory.BagItemStackHandler;

public class BagCap implements IBagCap
{
	private IBagModule[] modules = new IBagModule[0];
	private ItemStackHandler inv = new ItemStackHandler(0);

	public BagCap()
	{
		this(1);
	}
	
	public BagCap(int tier)
	{
		switch (tier)
		{
		case 1:
			initModules(BuildersBagConfig.Settings.tier1Modules);
			inv = new BagItemStackHandler(BuildersBagConfig.Settings.tier1Slots, BuildersBagConfig.Settings.tier1StackSize);
			break;
		case 2:
			initModules(BuildersBagConfig.Settings.tier2Modules);
			inv = new BagItemStackHandler(BuildersBagConfig.Settings.tier2Slots, BuildersBagConfig.Settings.tier2StackSize);
			break;
		case 3:
			initModules(BuildersBagConfig.Settings.tier3Modules);
			inv = new BagItemStackHandler(BuildersBagConfig.Settings.tier3Slots, BuildersBagConfig.Settings.tier3StackSize);
			break;
		case 4:
			initModules(BuildersBagConfig.Settings.tier4Modules);
			inv = new BagItemStackHandler(BuildersBagConfig.Settings.tier4Slots, BuildersBagConfig.Settings.tier4StackSize);
			break;
		case 5:
			initModules(BuildersBagConfig.Settings.tier5Modules);
			inv = new BagItemStackHandler(BuildersBagConfig.Settings.tier5Slots, BuildersBagConfig.Settings.tier5StackSize);
			break;
		}
	}
	
	

	private void initModules(String[] modules)
	{
		List<IBagModule> moduleList = new ArrayList<IBagModule>();
		Lists.newArrayList(modules).stream().forEach(s -> {
			IBagModule module = RegistryHandler.getModule(new ResourceLocation(s));
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

}
