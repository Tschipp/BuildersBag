package tschipp.buildersbag.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.caps.BagCapProvider;
import tschipp.buildersbag.common.caps.IBagCap;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;

import static tschipp.buildersbag.common.helper.InventoryHelper.*;

public class BuildersBagItem extends Item
{

	private int tier;

	public BuildersBagItem(int tier)
	{
		String name = "builders_bag_tier_" + tier;
		this.setUnlocalizedName(name);
		this.setRegistryName(name);
		this.setCreativeTab(CreativeTabs.TOOLS);
		this.setMaxStackSize(1);
		this.tier = tier;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		return new BagCapProvider(((BuildersBagItem) stack.getItem()).getTier());
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);

		if (player.isSneaking())
			player.openGui(BuildersBag.instance, 0, world, hand == EnumHand.MAIN_HAND ? 1 : 0, 0, 0);

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			ItemStack stack = player.getHeldItem(hand);
			IBagCap bag = CapHelper.getBagCap(stack);
			FakePlayer fake = new FakePlayer((WorldServer) world, player.getGameProfile());

			ItemStack placementStack = ItemStack.EMPTY;

			for (IBagModule module : bag.getModules())
			{
				if (module.isEnabled() && module.isDominating())
				{
					placementStack = module.getBlock(bag);
					break;
				}
			}

			if (placementStack.isEmpty())
			{
				placementStack = bag.getSelectedInventory().getStackInSlot(0).copy();
			}

			if (!player.isCreative())
			{
				placementStack = InventoryHelper.getOrProvideStack(placementStack, bag, player, null);

				if (placementStack.isEmpty())
				{
					return EnumActionResult.FAIL;
				}
			}
			else
				placementStack = placementStack.copy();

			ItemStack placementCopy = placementStack.copy();
			
			fake.setHeldItem(hand, placementStack);
			fake.rotationPitch = player.rotationPitch;
			fake.prevRotationYaw = player.rotationYaw;

			EnumActionResult result = placementStack.onItemUse(fake, world, pos, hand, facing, hitX, hitY, hitZ);
			if(result != EnumActionResult.SUCCESS)
				InventoryHelper.addStack(placementCopy, bag, player);
			
			return result;
				
		} else
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

}
