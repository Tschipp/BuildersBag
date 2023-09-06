package tschipp.buildersbag.common.item;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.BagComplex;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.client.rendering.BagItemStackRenderer;
import tschipp.buildersbag.common.BuildersBagRegistry;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.inventory.ContainerBag;
import tschipp.buildersbag.network.client.PlayFailureSoundClient;
import tschipp.buildersbag.network.client.SyncBagCapClient;

//@InterfaceList(value = { @Interface(modid = "littletiles", iface = "com.creativemd.littletiles.common.api.ILittleIngredientSupplier"), @Interface(modid = "littletiles", iface = "com.creativemd.littletiles.common.api.ILittleIngredientInventory"), @Interface(modid = "botania", iface = "vazkii.botania.api.item.IBlockProvider"), @Interface(modid = "baubles", iface = "baubles.api.IBauble") })
public class BuildersBagItem extends Item implements INamedContainerProvider
/*
 * implements ILittleIngredientSupplier, ILittleIngredientInventory,
 * IBlockProvider, IBauble
 */
{
	private int tier;

	public BuildersBagItem(int tier, String tiername)
	{
		super(new Item.Properties().tab(ItemGroup.TAB_TOOLS).stacksTo(1).setISTER(() -> () -> new BagItemStackRenderer()));

		String name = "builders_bag_tier_" + tiername;
		this.setRegistryName(name);
		this.tier = tier;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt)
	{
		if(BagCapProvider.BAG_CAPABILITY == null) //Cap not yet initialized
			return null;
			
		return new BagCapProvider(this.getTier());
	}

	@Override
	public boolean shouldOverrideMultiplayerNbt()
	{
		return true;
	}

	@Override
	public CompoundNBT getShareTag(ItemStack stack)
	{
		CompoundNBT sub = new CompoundNBT();
		CompoundNBT nbttags = super.getShareTag(stack);
		if (nbttags != null)
			sub.put("nbt", nbttags);

		IBagCap cap = CapHelper.getBagCap(stack);

		if (cap != null)
		{
			CompoundNBT bagcap = (CompoundNBT) BagCapProvider.BAG_CAPABILITY.getStorage().writeNBT(BagCapProvider.BAG_CAPABILITY, cap, null);
			sub.put("bagcap", bagcap);
		}
		return sub;
	}

	@Override
	public void readShareTag(ItemStack stack, CompoundNBT nbt)
	{
		if (nbt != null)
		{
			if (nbt.contains("nbt"))
				super.readShareTag(stack, nbt.getCompound("nbt"));

			if (nbt.contains("bagcap"))
				BagCapProvider.BAG_CAPABILITY.getStorage().readNBT(BagCapProvider.BAG_CAPABILITY, CapHelper.getBagCap(stack), null, nbt.getCompound("bagcap"));
		}
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
	{
		ItemStack stack = player.getItemInHand(hand);

		if (!world.isClientSide && player
				.isShiftKeyDown() /*
									 * && (ModList.get().isLoaded("linear") ?
									 * LinearCompatManager.doDragCheck(player) :
									 * true)
									 */) // TODO Linear
			openGui((ServerPlayerEntity) player, -1, false, hand);

		return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
	}

//	@Override
//	public ActionResultType useOn(ItemUseContext context)
//	{
//		PlayerEntity player = context.getPlayer();
//		Direction facing = context.getClickedFace();
//		Hand hand = context.getHand();
//		ItemStack stack = context.getItemInHand();
//		World world = context.getLevel();
//		BlockPos pos = context.getClickedPos();
//
//		if (player == null)
//			return ActionResultType.FAIL;
//
//		if (player.isShiftKeyDown())
//			return ActionResultType.PASS;
//
//		if (!world.isClientSide)
//		{
//			IBagCap bag = CapHelper.getBagCap(stack);
//			BagComplex complex = bag.getComplex();
//
//			ItemStack selectedStack = bag.getSelectedItem();
//		}
//	}

	@Override
	public ActionResultType useOn(ItemUseContext context)
	{
		PlayerEntity player = context.getPlayer();
		Direction facing = context.getClickedFace();
		Hand hand = context.getHand();
		ItemStack stack = context.getItemInHand();
		World world = context.getLevel();
		BlockPos pos = context.getClickedPos();

		if (player == null)
			return ActionResultType.FAIL;

		if (player.isShiftKeyDown())
			return ActionResultType.PASS;

		IBagCap bag = CapHelper.getBagCap(stack);

		if (!world.isClientSide)
		{
			FakePlayer fake = new FakePlayer((ServerWorld) world, player.getGameProfile());
			fake.xRot = player.xRot;
			fake.yRot = player.yRot;
			fake.setPos(0, 0, 0);

			ItemStack placementStack = ItemStack.EMPTY;

			for (IBagModule module : BagHelper.getSortedModules(bag))
			{
				if (module.isEnabled() && module.isDominating())
				{
					placementStack = module.getBlock(bag, player);
					break;
				}
			}

			if (placementStack.isEmpty())
			{
				placementStack = bag.getSelectedInventory().getStackInSlot(0).copy();
			}

			if (placementStack.isEmpty())
			{
				// Send these via packet
				player.displayClientMessage(new TranslationTextComponent("buildersbag.noblock").withStyle(TextFormatting.RED), true);
				BuildersBag.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new PlayFailureSoundClient());
				return ActionResultType.FAIL;
			}

			ItemStack requestedStack = placementStack.copy();
			Item placementItem = placementStack.getItem();

//			fake.setItemInHand(hand, placementStack);
//			fake.setPos(player.getX(), player.getY(), player.getZ());

			Block block = Block.byItem(placementStack.getItem());
			boolean canPlace = block.defaultBlockState().canSurvive(world, world.getBlockState(pos).getMaterial().isReplaceable() ? pos : pos.relative(facing));
			boolean canEdit = player.mayUseItemAt(pos, facing, placementStack);
			boolean enoughSpace = world.isUnobstructed(world.getBlockState(pos), pos.relative(facing), ISelectionContext.of(player));
			boolean b = canEdit && canPlace && enoughSpace;

			if (!b)
			{
				player.displayClientMessage(new TranslationTextComponent("buildersbag.cantplace", requestedStack.getHoverName()).withStyle(TextFormatting.RED), true);
				BuildersBag.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new PlayFailureSoundClient());
				return ActionResultType.FAIL;
			}

			BagComplex complex = bag.getComplex();

			if (!player.isCreative())
			{
				int removed = complex.take(placementItem, 1, player);
				complex.runCreateableTests();
				if (removed < 1)
				{
					player.displayClientMessage(new TranslationTextComponent("buildersbag.nomaterials", requestedStack.getHoverName()).withStyle(TextFormatting.RED), true);
					BuildersBag.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new PlayFailureSoundClient());
					return ActionResultType.FAIL;
				}
				placementStack = placementStack.copy();
			}
			else
				placementStack = placementStack.copy();

			fake.setItemInHand(hand, placementStack);
			fake.setPos(player.getX(), player.getY(), player.getZ());

			ActionResultType result = placementStack.useOn(new ItemUseContext(fake, hand, new BlockRayTraceResult(context.getClickLocation(), facing, pos, context.isInside())));

			if (result != ActionResultType.CONSUME)
			{
				complex.add(placementItem, 1, player);
				player.displayClientMessage(new TranslationTextComponent("buildersbag.cantplace", requestedStack.getHoverName()).withStyle(TextFormatting.RED), true);
				BuildersBag.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new PlayFailureSoundClient());
				return ActionResultType.FAIL;
			}
			else
				player.swing(hand);

			BuildersBag.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SyncBagCapClient(bag, hand));

			return result;

		}
		else
			return ActionResultType.SUCCESS;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return slotChanged;
	}

	public int getTier()
	{
		return tier;
	}

	@Override
	public Container createMenu(int windowID, PlayerInventory inventory, PlayerEntity player)
	{
		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();

		if (main.getItem() instanceof BuildersBagItem)
			return new ContainerBag(windowID, player, main, Hand.MAIN_HAND);
		else if (off.getItem() instanceof BuildersBagItem)
			return new ContainerBag(windowID, player, off, Hand.OFF_HAND);

		return null;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("buildersbag.name");
	}

	public static void openGui(ServerPlayerEntity player, int slot, boolean isBauble, @Nullable Hand hand)
	{
		NetworkHooks.openGui(player, BuildersBagRegistry.TIER_1, buf -> {
			CompoundNBT tag = new CompoundNBT();
			tag.putInt("slot", slot);
			if (hand != null)
				tag.putBoolean("hand", hand == Hand.MAIN_HAND ? true : false);
			tag.putBoolean("isBauble", isBauble);
			buf.writeNbt(tag);
		});

	}

	// /*
	// * LITTLE TILES COMPAT TODO
	// */
	// @Optional.Method(modid = "littletiles")
	// @Override
	// public void requestIngredients(ItemStack stack, LittleIngredients
	// ingredients, @Nonnull LittleIngredients overflow, LittleInventory
	// inventory)
	// {
	// IBagCap bag = CapHelper.getBagCap(stack);
	//
	// if (bag.hasModuleAndEnabled("buildersbag:littletiles"))
	// {
	// LittleTilesModule.provideLittleIngredients(stack, ingredients, overflow,
	// inventory.getPlayer(), inventory);
	// }
	// }
	//
	// @Optional.Method(modid = "littletiles")
	// @Override
	// public LittleIngredients getInventory(ItemStack stack)
	// {
	// return new LittleIngredients();
	// }
	//
	// @Optional.Method(modid = "littletiles")
	// @Override
	// public void setInventory(ItemStack stack, LittleIngredients ing,
	// LittleInventory inv)
	// {
	// if (stack.getItem() instanceof BuildersBagItem && !inv.isSimulation())
	// {
	// IBagCap bag = CapHelper.getBagCap(stack);
	//
	// if (bag.hasModuleAndEnabled("buildersbag:littletiles"))
	// {
	// LittleTilesModule.addIngredients(stack, ing, inv.getPlayer());
	// }
	// }
	// }
	//
	// @Optional.Method(modid = "littletiles")
	// @Override
	// public void collect(HashMapList<String, ItemStack> list, ItemStack stack,
	// PlayerEntity player)
	// {
	// if (stack.getItem() instanceof BuildersBagItem)
	// {
	// IBagCap bag = CapHelper.getBagCap(stack);
	//
	// if (bag.hasModuleAndEnabled("buildersbag:littletiles"))
	// {
	// LittleTilesModule.setAvailableIngredients(list, stack, bag, player);
	// }
	// }
	// }
	//
	// /*
	// * BOTANIA COMPAT
	// */
	// @Optional.Method(modid = "botania")
	// @Override
	// public boolean provideBlock(PlayerEntity player, ItemStack requestor,
	// ItemStack stack, Block block, int meta, boolean doit)
	// {
	// return BotaniaCompat.provideBlock(player, requestor, stack, block, meta,
	// doit);
	// }
	//
	// @Optional.Method(modid = "botania")
	// @Override
	// public int getBlockCount(PlayerEntity player, ItemStack requestor,
	// ItemStack stack, Block block, int meta)
	// {
	// return BotaniaCompat.getBlockCount(player, requestor, stack, block,
	// meta);
	// }
	//
	// /*
	// * BAUBLES COMPAT
	// */
	// @Optional.Method(modid = "baubles")
	// @Override
	// public BaubleType getBaubleType(ItemStack itemstack)
	// {
	// return BaubleType.BELT;
	// }

}
