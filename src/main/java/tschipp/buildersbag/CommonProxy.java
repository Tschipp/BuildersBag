package tschipp.buildersbag;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import tschipp.buildersbag.api.IngredientMapper;
import tschipp.buildersbag.common.BuildersBagRegistry;
import tschipp.buildersbag.network.client.GrowItemClient;
import tschipp.buildersbag.network.client.ModifyCacheClient;
import tschipp.buildersbag.network.client.PlayFailureSoundClient;
import tschipp.buildersbag.network.client.SetHeldItemClient;
import tschipp.buildersbag.network.client.SetWorkStateClient;
import tschipp.buildersbag.network.client.SyncBagCapClient;
import tschipp.buildersbag.network.client.SyncBagCapInventoryClient;
import tschipp.buildersbag.network.client.UpdateCacheClient;
import tschipp.buildersbag.network.server.CompactBagServer;
import tschipp.buildersbag.network.server.ModifyPaletteServer;
import tschipp.buildersbag.network.server.OpenBaubleBagServer;
import tschipp.buildersbag.network.server.RequestBagUpdateServer;
import tschipp.buildersbag.network.server.RequestCacheUpdateServer;
import tschipp.buildersbag.network.server.SetSelectedBlockServer;
import tschipp.buildersbag.network.server.SyncBagCapServer;
import tschipp.buildersbag.network.server.SyncItemStackServer;
import tschipp.buildersbag.network.server.SyncModuleStateServer;

@EventBusSubscriber(bus = Bus.MOD, modid = BuildersBag.MODID)
public class CommonProxy implements IProxy
{

	private static int packetID = 0;

	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event)
	{
		String version = BuildersBag.info.getVersion().toString();

		BuildersBag.network = NetworkRegistry.newSimpleChannel(new ResourceLocation(BuildersBag.MODID, "buildersbagpackets"), () -> version, version::equals, version::equals);

		BuildersBag.network.registerMessage(packetID++, SyncItemStackServer.class, SyncItemStackServer::toBytes, SyncItemStackServer::new, SyncItemStackServer::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		BuildersBag.network.registerMessage(packetID++, SyncModuleStateServer.class, SyncModuleStateServer::toBytes, SyncModuleStateServer::new, SyncModuleStateServer::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		BuildersBag.network.registerMessage(packetID++, SyncBagCapClient.class, SyncBagCapClient::toBytes, SyncBagCapClient::new, SyncBagCapClient::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		BuildersBag.network.registerMessage(packetID++, SetHeldItemClient.class, SetHeldItemClient::toBytes, SetHeldItemClient::new, SetHeldItemClient::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		BuildersBag.network.registerMessage(packetID++, SyncBagCapInventoryClient.class, SyncBagCapInventoryClient::toBytes, SyncBagCapInventoryClient::new, SyncBagCapInventoryClient::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		BuildersBag.network.registerMessage(packetID++, OpenBaubleBagServer.class, OpenBaubleBagServer::toBytes, OpenBaubleBagServer::new, OpenBaubleBagServer::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		BuildersBag.network.registerMessage(packetID++, GrowItemClient.class, GrowItemClient::toBytes, GrowItemClient::new, GrowItemClient::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		BuildersBag.network.registerMessage(packetID++, SyncBagCapServer.class, SyncBagCapServer::toBytes, SyncBagCapServer::new, SyncBagCapServer::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		BuildersBag.network.registerMessage(packetID++, CompactBagServer.class, CompactBagServer::toBytes, CompactBagServer::new, CompactBagServer::handle,  Optional.of(NetworkDirection.PLAY_TO_SERVER));
		BuildersBag.network.registerMessage(packetID++, RequestCacheUpdateServer.class, RequestCacheUpdateServer::toBytes, RequestCacheUpdateServer::new, RequestCacheUpdateServer::handle,  Optional.of(NetworkDirection.PLAY_TO_SERVER));
		BuildersBag.network.registerMessage(packetID++, UpdateCacheClient.class, UpdateCacheClient::toBytes, UpdateCacheClient::new, UpdateCacheClient::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		BuildersBag.network.registerMessage(packetID++, ModifyCacheClient.class, ModifyCacheClient::toBytes, ModifyCacheClient::new, ModifyCacheClient::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		BuildersBag.network.registerMessage(packetID++, SetWorkStateClient.class, SetWorkStateClient::toBytes, SetWorkStateClient::new, SetWorkStateClient::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		BuildersBag.network.registerMessage(packetID++, RequestBagUpdateServer.class, RequestBagUpdateServer::toBytes, RequestBagUpdateServer::new, RequestBagUpdateServer::handle,  Optional.of(NetworkDirection.PLAY_TO_SERVER));
		BuildersBag.network.registerMessage(packetID++, PlayFailureSoundClient.class, PlayFailureSoundClient::toBytes, PlayFailureSoundClient::new, PlayFailureSoundClient::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		BuildersBag.network.registerMessage(packetID++, SetSelectedBlockServer.class, SetSelectedBlockServer::toBytes, SetSelectedBlockServer::new, SetSelectedBlockServer::handle,  Optional.of(NetworkDirection.PLAY_TO_SERVER));
		BuildersBag.network.registerMessage(packetID++, ModifyPaletteServer.class, ModifyPaletteServer::toBytes, ModifyPaletteServer::new, ModifyPaletteServer::handle,  Optional.of(NetworkDirection.PLAY_TO_SERVER));

//		NetworkRegistry.INSTANCE.registerGuiHandler(BuildersBag.instance, new BagGuiHandler());
		BuildersBagRegistry.registerCapabilities();
		IngredientMapper.init();
		
//		BuildersBagConfig.setDefaultsOnFirstLoad();

//		if (ModList.get().isLoaded("betterbuilderswands"))
//			BBWCompat.register();

//		if (ModList.get().isLoaded("botania"))
//			BotaniaCompat.register();

//		if (!ModList.get().isLoaded("crafttweaker"))
//			CraftingHandler.generateRecipes();

//		if (ModList.get().isLoaded("chiselsandbits"))
//		{
//			FMLEmbeddedChannel server = NetworkRegistry.INSTANCE.getChannel("ChiselsAndBits", Dist.DEDICATED_SERVER);
//			NetworkEventFiringHandler handler = server.pipeline().get(NetworkEventFiringHandler.class);
//			try
//			{
//				FMLEventChannel fmleventChannel = (FMLEventChannel) eventChannel.get(handler);
//				fmleventChannel.register(new ChiselEvents());
//			}
//			catch (IllegalArgumentException | IllegalAccessException e)
//			{
//				e.printStackTrace();
//			}
//		}
//
//		if (ModList.get().isLoaded("linear"))
//			LinearCompatManager.register();
	}

	@Override
	public PlayerEntity getPlayer()
	{
		return null;
	}

	@Override
	public World getWorld()
	{
		return null;
	}

	@Override
	public void changeWorkState(String uuid, PlayerEntity player, boolean start)
	{
		BuildersBag.network.send(PacketDistributor.PLAYER.with(() ->  (ServerPlayerEntity) player), new SetWorkStateClient(uuid, start));
	}

}
