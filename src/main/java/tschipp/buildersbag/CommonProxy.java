package tschipp.buildersbag;

import java.lang.reflect.Field;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkEventFiringHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import tschipp.buildersbag.common.BuildersBagRegistry;
import tschipp.buildersbag.common.config.BuildersBagConfig;
import tschipp.buildersbag.common.crafting.CraftingHandler;
import tschipp.buildersbag.common.inventory.BagGuiHandler;
import tschipp.buildersbag.compat.bbw.BBWCompat;
import tschipp.buildersbag.compat.botania.BotaniaCompat;
import tschipp.buildersbag.compat.chisel.ChiselEvents;
import tschipp.buildersbag.compat.linear.LinearCompatManager;
import tschipp.buildersbag.network.client.GrowItemClient;
import tschipp.buildersbag.network.client.ModifyCacheClient;
import tschipp.buildersbag.network.client.PlayFailureSoundClient;
import tschipp.buildersbag.network.client.SetHeldItemClient;
import tschipp.buildersbag.network.client.SetWorkStateClient;
import tschipp.buildersbag.network.client.SyncBagCapClient;
import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;
import tschipp.buildersbag.network.client.SyncBagCapServer;
import tschipp.buildersbag.network.client.SyncEnderchestToClient;
import tschipp.buildersbag.network.client.UpdateCacheClient;
import tschipp.buildersbag.network.server.CompactBagServer;
import tschipp.buildersbag.network.server.ModifyPaletteServer;
import tschipp.buildersbag.network.server.OpenBaubleBagServer;
import tschipp.buildersbag.network.server.RequestBagUpdateServer;
import tschipp.buildersbag.network.server.RequestCacheUpdateServer;
import tschipp.buildersbag.network.server.SetSelectedBlockServer;
import tschipp.buildersbag.network.server.SyncItemStackServer;
import tschipp.buildersbag.network.server.SyncModuleStateServer;

public class CommonProxy
{

	private static Field eventChannel;

	static
	{
		eventChannel = ReflectionHelper.findField(NetworkEventFiringHandler.class, "eventChannel");
		eventChannel.setAccessible(true);
	}

	public void preInit(FMLPreInitializationEvent event)
	{
		BuildersBag.network = NetworkRegistry.INSTANCE.newSimpleChannel(BuildersBag.MODID);
		BuildersBag.network.registerMessage(SyncItemStackServer.class, SyncItemStackServer.class, 0, Side.SERVER);
		BuildersBag.network.registerMessage(SyncModuleStateServer.class, SyncModuleStateServer.class, 1, Side.SERVER);
		BuildersBag.network.registerMessage(SyncBagCapClient.class, SyncBagCapClient.class, 2, Side.CLIENT);
		BuildersBag.network.registerMessage(SetHeldItemClient.class, SetHeldItemClient.class, 3, Side.CLIENT);
		BuildersBag.network.registerMessage(SyncBagCapInventoryClient.class, SyncBagCapInventoryClient.class, 4, Side.CLIENT);
		BuildersBag.network.registerMessage(OpenBaubleBagServer.class, OpenBaubleBagServer.class, 5, Side.SERVER);
		BuildersBag.network.registerMessage(GrowItemClient.class, GrowItemClient.class, 6, Side.CLIENT);
		BuildersBag.network.registerMessage(SyncEnderchestToClient.class, SyncEnderchestToClient.class, 7, Side.CLIENT);
		BuildersBag.network.registerMessage(SyncBagCapServer.class, SyncBagCapServer.class, 8, Side.SERVER);
		BuildersBag.network.registerMessage(CompactBagServer.class, CompactBagServer.class, 9, Side.SERVER);
		BuildersBag.network.registerMessage(RequestCacheUpdateServer.class, RequestCacheUpdateServer.class, 10, Side.SERVER);
		BuildersBag.network.registerMessage(UpdateCacheClient.class, UpdateCacheClient.class, 11, Side.CLIENT);
		BuildersBag.network.registerMessage(ModifyCacheClient.class, ModifyCacheClient.class, 12, Side.CLIENT);
		BuildersBag.network.registerMessage(SetWorkStateClient.class, SetWorkStateClient.class, 13, Side.CLIENT);
		BuildersBag.network.registerMessage(RequestBagUpdateServer.class, RequestBagUpdateServer.class, 14, Side.SERVER);
		BuildersBag.network.registerMessage(PlayFailureSoundClient.class, PlayFailureSoundClient.class, 15, Side.CLIENT);
		BuildersBag.network.registerMessage(SetSelectedBlockServer.class, SetSelectedBlockServer.class, 16, Side.SERVER);
		BuildersBag.network.registerMessage(ModifyPaletteServer.class, ModifyPaletteServer.class, 17, Side.SERVER);

		NetworkRegistry.INSTANCE.registerGuiHandler(BuildersBag.instance, new BagGuiHandler());
		BuildersBagRegistry.registerModules();

		BuildersBagConfig.setDefaultsOnFirstLoad();

		BuildersBagRegistry.registerCapabilities();
		BuildersBagRegistry.registerItems();
	}

	public void init(FMLInitializationEvent event)
	{
		if (Loader.isModLoaded("betterbuilderswands"))
			BBWCompat.register();

		if (Loader.isModLoaded("botania"))
			BotaniaCompat.register();
	}

	public void postInit(FMLPostInitializationEvent event)
	{
		if (!Loader.isModLoaded("crafttweaker"))
			CraftingHandler.generateRecipes();

		if (Loader.isModLoaded("chiselsandbits"))
		{
			FMLEmbeddedChannel server = NetworkRegistry.INSTANCE.getChannel("ChiselsAndBits", Side.SERVER);
			NetworkEventFiringHandler handler = server.pipeline().get(NetworkEventFiringHandler.class);
			try
			{
				FMLEventChannel fmleventChannel = (FMLEventChannel) eventChannel.get(handler);
				fmleventChannel.register(new ChiselEvents());
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

		if (Loader.isModLoaded("linear"))
			LinearCompatManager.register();

	}

	public PlayerEntity getPlayer()
	{
		return null;
	}

	public void setTEISR(Item item)
	{
	}

	public Side getSide()
	{
		return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER ? Side.SERVER : Side.CLIENT;
	}

	public void startWorking(String uuid, PlayerEntity player)
	{
		BuildersBag.network.sendTo(new SetWorkStateClient(uuid, true), (ServerPlayerEntity) player);
	}

	public void stopWorking(String uuid, PlayerEntity player)
	{
		BuildersBag.network.sendTo(new SetWorkStateClient(uuid, false), (ServerPlayerEntity) player);
	}

}
