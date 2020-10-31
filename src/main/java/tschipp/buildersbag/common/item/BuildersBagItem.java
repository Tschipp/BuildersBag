package tschipp.buildersbag.common.item;

import javax.annotation.Nonnull;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.api.ILittleIngredientInventory;
import com.creativemd.littletiles.common.api.ILittleIngredientSupplier;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.modules.LittleTilesModule;
import tschipp.buildersbag.compat.botania.BotaniaCompat;
import tschipp.buildersbag.compat.linear.LinearCompatManager;
import tschipp.buildersbag.network.client.PlayFailureSoundClient;
import tschipp.buildersbag.network.client.SyncBagCapClient;
import tschipp.buildersbag.network.client.SyncEnderchestToClient;
import vazkii.botania.api.item.IBlockProvider;

@InterfaceList(value = { @Interface(modid = "littletiles", iface = "com.creativemd.littletiles.common.api.ILittleIngredientSupplier"), @Interface(modid = "littletiles", iface = "com.creativemd.littletiles.common.api.ILittleIngredientInventory"), @Interface(modid = "botania", iface = "vazkii.botania.api.item.IBlockProvider"), @Interface(modid = "baubles", iface = "baubles.api.IBauble") })
public class BuildersBagItem extends Item implements ILittleIngredientSupplier, ILittleIngredientInventory, IBlockProvider, IBauble
{
	private int tier;

	public BuildersBagItem(int tier, String tiername)
	{
		String name = "builders_bag_tier_" + tiername;
		this.setUnlocalizedName(name);
		this.setRegistryName(name);
		this.setCreativeTab(CreativeTabs.TOOLS);
		this.setMaxStackSize(1);
		this.tier = tier;
		BuildersBag.proxy.setTEISR(this);
		ForgeRegistries.ITEMS.register(this);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		return new BagCapProvider(this.getTier());
	}

	@Override
	public boolean getShareTag()
	{
		return true;
	}

	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack)
	{
		NBTTagCompound sub = new NBTTagCompound();
		NBTTagCompound nbttags = super.getNBTShareTag(stack);
		if (nbttags != null)
			sub.setTag("nbt", nbttags);

		IBagCap cap = CapHelper.getBagCap(stack);

		if (cap != null)
		{
			NBTTagCompound bagcap = (NBTTagCompound) BagCapProvider.BAG_CAPABILITY.getStorage().writeNBT(BagCapProvider.BAG_CAPABILITY, cap, null);
			sub.setTag("bagcap", bagcap);
		}
		return sub;
	}

	@Override
	public void readNBTShareTag(ItemStack stack, NBTTagCompound nbt)
	{
		if (nbt != null)
		{
			if (nbt.hasKey("nbt"))
				super.readNBTShareTag(stack, nbt.getCompoundTag("nbt"));

			if (nbt.hasKey("bagcap"))
				BagCapProvider.BAG_CAPABILITY.getStorage().readNBT(BagCapProvider.BAG_CAPABILITY, CapHelper.getBagCap(stack), null, nbt.getCompoundTag("bagcap"));
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);

		if (player.isSneaking() && (Loader.isModLoaded("linear") ? LinearCompatManager.doDragCheck(player) : true))
			player.openGui(BuildersBag.instance, 0, world, hand == EnumHand.MAIN_HAND ? 1 : 0, 0, 0);

		if (!world.isRemote)
			BuildersBag.network.sendTo(new SyncEnderchestToClient(player), (EntityPlayerMP) player);

		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (player.isSneaking())
			return EnumActionResult.PASS;

		ItemStack stack = player.getHeldItem(hand);
		IBagCap bag = CapHelper.getBagCap(stack);

		if (!world.isRemote)
		{
			FakePlayer fake = new FakePlayer((WorldServer) world, player.getGameProfile());
			fake.rotationPitch = player.rotationPitch;
			fake.rotationYaw = player.rotationYaw;
			fake.setPosition(0, 0, 0);

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
				player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("buildersbag.noblock")), true);
				BuildersBag.network.sendTo(new PlayFailureSoundClient(),  (EntityPlayerMP) player);
				return EnumActionResult.FAIL;
			}

			ItemStack requestedStack = placementStack.copy();

			fake.setHeldItem(hand, placementStack);
			fake.setPosition(player.posX, player.posY, player.posZ);

			Block block = Block.getBlockFromItem(placementStack.getItem());
			boolean canPlace = world.mayPlace(block, world.getBlockState(pos).getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing), false, facing, player);
			boolean canEdit = player.canPlayerEdit(pos, facing, placementStack);
			boolean b = canEdit && canPlace;

			if (!b)
			{
				player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocalFormatted("buildersbag.cantplace", requestedStack.getDisplayName())), true);
				BuildersBag.network.sendTo(new PlayFailureSoundClient(),  (EntityPlayerMP) player);
				return EnumActionResult.FAIL;
			}

			if (!player.isCreative())
				placementStack = BagHelper.getOrProvideStack(placementStack, bag, player, null);
			else
				placementStack = placementStack.copy();

			BagHelper.resetRecursionDepth(player);

			if (placementStack.isEmpty())
			{
				player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocalFormatted("buildersbag.nomaterials", requestedStack.getDisplayName())), true);
				BuildersBag.network.sendTo(new PlayFailureSoundClient(),  (EntityPlayerMP) player);
				return EnumActionResult.FAIL;
			}

			fake.setPosition(0, 0, 0);
			fake.setHeldItem(hand, placementStack);
			fake.setPosition(player.posX, player.posY, player.posZ);

			EnumActionResult result = placementStack.onItemUse(fake, world, pos, hand, facing, hitX, hitY, hitZ);

			if (result != EnumActionResult.SUCCESS)
				BagHelper.addStack(placementStack, bag, player);
			else
				player.swingArm(hand);

			BuildersBag.network.sendTo(new SyncBagCapClient(bag, hand), (EntityPlayerMP) player);
			BuildersBag.network.sendTo(new SyncEnderchestToClient(player), (EntityPlayerMP) player);

			
			return result;

		}
		else
			return EnumActionResult.SUCCESS;
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

	/*
	 * LITTLE TILES COMPAT
	 */
	@Optional.Method(modid = "littletiles")
	@Override
	public void requestIngredients(ItemStack stack, LittleIngredients ingredients, @Nonnull LittleIngredients overflow, LittleInventory inventory)
	{
		IBagCap bag = CapHelper.getBagCap(stack);

		if (bag.hasModuleAndEnabled("buildersbag:littletiles"))
		{
			LittleTilesModule.provideLittleIngredients(stack, ingredients, overflow, inventory.getPlayer(), inventory);
		}
	}

	@Optional.Method(modid = "littletiles")
	@Override
	public LittleIngredients getInventory(ItemStack stack)
	{
		return new LittleIngredients();
	}

	@Optional.Method(modid = "littletiles")
	@Override
	public void setInventory(ItemStack stack, LittleIngredients ing, LittleInventory inv)
	{
		if (stack.getItem() instanceof BuildersBagItem && !inv.isSimulation())
		{
			IBagCap bag = CapHelper.getBagCap(stack);

			if (bag.hasModuleAndEnabled("buildersbag:littletiles"))
			{
				LittleTilesModule.addIngredients(stack, ing, inv.getPlayer());
			}
		}
	}

	@Optional.Method(modid = "littletiles")
	@Override
	public void collect(HashMapList<String, ItemStack> list, ItemStack stack, EntityPlayer player)
	{
		if (stack.getItem() instanceof BuildersBagItem)
		{
			IBagCap bag = CapHelper.getBagCap(stack);

			if (bag.hasModuleAndEnabled("buildersbag:littletiles"))
			{
				LittleTilesModule.setAvailableIngredients(list, stack, bag, player);
			}
		}
	}

	/*
	 * BOTANIA COMPAT
	 */
	@Optional.Method(modid = "botania")
	@Override
	public boolean provideBlock(EntityPlayer player, ItemStack requestor, ItemStack stack, Block block, int meta, boolean doit)
	{
		return BotaniaCompat.provideBlock(player, requestor, stack, block, meta, doit);
	}

	@Optional.Method(modid = "botania")
	@Override
	public int getBlockCount(EntityPlayer player, ItemStack requestor, ItemStack stack, Block block, int meta)
	{
		return BotaniaCompat.getBlockCount(player, requestor, stack, block, meta);
	}

	/*
	 * BAUBLES COMPAT
	 */
	@Optional.Method(modid = "baubles")
	@Override
	public BaubleType getBaubleType(ItemStack itemstack)
	{
		return BaubleType.BELT;
	}

}
