package tschipp.buildersbag.common.modules;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.iterators.ChiselIterator;
import mod.chiselsandbits.chiseledblock.iterators.ChiselTypeIterator;
import mod.chiselsandbits.helpers.ActingPlayer;
import mod.chiselsandbits.helpers.BitOperation;
import mod.chiselsandbits.helpers.ContinousBits;
import mod.chiselsandbits.helpers.IContinuousInventory;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.helpers.VoxelRegionSrc;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemBitBag.BagPos;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.modes.ChiselMode;
import mod.chiselsandbits.network.ModPacket;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketChisel;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.ItemStackHandler;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.api.IBagCap;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.common.helper.BagHelper;
import tschipp.buildersbag.common.helper.CapHelper;
import tschipp.buildersbag.common.helper.InventoryHelper;
import tschipp.buildersbag.common.inventory.ItemHandlerWithPredicate;
import tschipp.buildersbag.network.client.SetHeldItemClient;

public class ChiselsBitsModule extends AbstractBagModule
{

	private static final ItemStack DISPLAY = new ItemStack(Item.getByNameOrId("chiselsandbits:chisel_iron"));
	private ItemHandlerWithPredicate handler = new ItemHandlerWithPredicate(1, (stack, slot) -> stack.getItem() instanceof ItemChisel);

	private static Field fromF, toF, placeF, sideF, modeF, handF, serverEntityF;

	static
	{
		fromF = ReflectionHelper.findField(PacketChisel.class, "from");
		toF = ReflectionHelper.findField(PacketChisel.class, "to");
		placeF = ReflectionHelper.findField(PacketChisel.class, "place");
		sideF = ReflectionHelper.findField(PacketChisel.class, "side");
		modeF = ReflectionHelper.findField(PacketChisel.class, "mode");
		handF = ReflectionHelper.findField(PacketChisel.class, "hand");
		serverEntityF = ReflectionHelper.findField(ModPacket.class, "serverEntity");

		fromF.setAccessible(true);
		toF.setAccessible(true);
		placeF.setAccessible(true);
		sideF.setAccessible(true);
		modeF.setAccessible(true);
		handF.setAccessible(true);
		serverEntityF.setAccessible(true);

	}

	public ChiselsBitsModule()
	{
		super("buildersbag:chiselsandbits");

	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks(IBagCap bag, EntityPlayer player)
	{
		return NonNullList.create();
	}

	@Override
	public boolean doesntUseOwnInventory()
	{
		return false;
	}

	@Override
	public ItemStackHandler getInventory()
	{
		return handler;
	}

	@Override
	public ItemStack getDisplayItem()
	{
		return DISPLAY;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound tag = super.serializeNBT();
		tag.setTag("Inventory", handler.serializeNBT());
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		super.deserializeNBT(nbt);
		handler.deserializeNBT(nbt.getCompoundTag("Inventory"));
	}

	public static void checkAndProvideBits(PacketChisel packet, EntityPlayer player)
	{
		try
		{
			BitLocation from = (BitLocation) fromF.get(packet);
			BitLocation to = (BitLocation) toF.get(packet);
			BitOperation place = (BitOperation) placeF.get(packet);
			EnumFacing side = (EnumFacing) sideF.get(packet);
			ChiselMode mode = (ChiselMode) modeF.get(packet);
			EnumHand hand = (EnumHand) handF.get(packet);
			World world = player.world;

			final int minX = Math.min(from.blockPos.getX(), to.blockPos.getX());
			final int maxX = Math.max(from.blockPos.getX(), to.blockPos.getX());
			final int minY = Math.min(from.blockPos.getY(), to.blockPos.getY());
			final int maxY = Math.max(from.blockPos.getY(), to.blockPos.getY());
			final int minZ = Math.min(from.blockPos.getZ(), to.blockPos.getZ());
			final int maxZ = Math.max(from.blockPos.getZ(), to.blockPos.getZ());

			if (!(player.getHeldItem(hand).getItem() instanceof ItemChiseledBit))
				return;

			int placeStateID = place.usesBits() ? ItemChiseledBit.getStackState(player.getHeldItem(hand)) : 0;
			int missingBits = 0;
			int totalBits = 0;
			int placedBits = 0;

			int extra = 0;

			ActingPlayer acting = ActingPlayer.testingAs(player, hand);

			for (int xOff = minX; xOff <= maxX; ++xOff)
			{
				for (int yOff = minY; yOff <= maxY; ++yOff)
				{
					for (int zOff = minZ; zOff <= maxZ; ++zOff)
					{
						final BlockPos pos = new BlockPos(xOff, yOff, zOff);
						VoxelBlob vb = null;

						TileEntity te = world.getTileEntity(pos);
						if (te != null && te instanceof TileEntityBlockChiseled)
						{
							vb = ((TileEntityBlockChiseled) te).getBlob();
						} else if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos))
							continue;

						ChiselIterator it = getIterator(new VoxelRegionSrc(world, pos, 1), pos, place, mode, from, to, side);

						int bitCount = 0;

						while (it.hasNext())
						{
							if (vb != null && ((vb.get(it.x(), it.y(), it.z()) != Block.getStateId(Blocks.AIR.getDefaultState()) && !place.usesChisels()) || (vb.get(it.x(), it.y(), it.z()) == placeStateID && place.usesChisels())))
							{
								continue;
							} else
								bitCount++;

						}

						IContinuousInventory bits = new ContinousBits(acting, pos, placeStateID);

						int bitsLeft = bitCount;

						for (int i = 0; i < bitCount; i++)
						{
							if (bits.isValid())
							{
								if (bits.useItem(1))
								{
									bitsLeft--;
									placedBits++;
								}
							} else
								break;
						}

						missingBits += bitsLeft;
						totalBits += bitCount;
					}
				}
			}

			boolean needsExtra = false;

			IContinuousInventory bits = new ContinousBits(acting, player.getPosition(), placeStateID);

			if (missingBits == 0 && (!bits.isValid() || !bits.useItem(1)))
			{
				needsExtra = true;
			}

			if (missingBits > 0 || needsExtra)
			{
				boolean sendPacket = false;
				boolean provide = false;

				IBlockState state = Block.getStateById(placeStateID);
				int meta = state.getBlock().getMetaFromState(state);

				ItemStack required = new ItemStack(state.getBlock(), 1, meta);

				double blocks = missingBits / (16.0 * 16.0 * 16.0) + (needsExtra ? 1 : 0);
				int rounded = (int) Math.ceil(blocks);

				NonNullList<ItemStack> bags = InventoryHelper.getBagsInInventory(player);
				for (ItemStack bag : bags)
				{
					if (!bag.isEmpty())
					{
						IBagCap cap = CapHelper.getBagCap(bag);
						if (cap.hasModuleAndEnabled("buildersbag:chiselsandbits"))
						{
							IBagModule chiselModule = BagHelper.getModule("buildersbag:chiselsandbits", cap);
							ItemStackHandler inv = chiselModule.getInventory();
							ItemStack chisel = inv.getStackInSlot(0);

							if (!chisel.isEmpty())
							{
								NonNullList<ItemStack> provided = BagHelper.getOrProvideStackWithCount(required, rounded, cap, player, null);
								rounded -= provided.size();

								if (chisel.attemptDamageItem(provided.size() * 16 * 16 * 16, new Random(), (EntityPlayerMP) player))
									chisel.shrink(1);

								ItemStack providedBits = ItemChiseledBit.createStack(placeStateID, (provided.size() * 16 * 16 * 16) + (needsExtra ? 0 : placedBits - 64), true);
								EntityItem entityBits = new EntityItem(world, player.posX, player.posY, player.posZ, providedBits);


								List<BagPos> bitBags = ItemBitBag.getBags(player.inventory);
								for (BagPos i : bitBags)
								{
									ItemStack remainder = i.inv.insertItem(entityBits.getItem());
									final int changed = ModUtil.getStackSize(providedBits) - ModUtil.getStackSize(entityBits.getItem());
									entityBits.setItem(remainder);
								}

								if (provided.size() > 0)
								{
									sendPacket = true;
									provide = true;
								}

								if (rounded <= 0)
									break;
							}
						}
					}
				}

				if (sendPacket)
				{

					if (provide)
					{

						
						ItemStack providedBits = ItemChiseledBit.createStack(placeStateID, 64, true);
						player.setHeldItem(hand, providedBits);
						BuildersBag.network.sendTo(new SetHeldItemClient(providedBits, hand), (EntityPlayerMP) player);
					}

					player.addTag("chiselPacket");
					NetworkRouter.instance.sendToServer(packet);
				}
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private static ChiselIterator getIterator(final VoxelRegionSrc vb, final BlockPos pos, final BitOperation place, ChiselMode mode, BitLocation from, BitLocation to, EnumFacing side)
	{
		if (mode == ChiselMode.DRAWN_REGION)
		{
			final int bitX = pos.getX() == from.blockPos.getX() ? from.bitX : 0;
			final int bitY = pos.getY() == from.blockPos.getY() ? from.bitY : 0;
			final int bitZ = pos.getZ() == from.blockPos.getZ() ? from.bitZ : 0;

			final int scaleX = (pos.getX() == to.blockPos.getX() ? to.bitX : 15) - bitX + 1;
			final int scaleY = (pos.getY() == to.blockPos.getY() ? to.bitY : 15) - bitY + 1;
			final int scaleZ = (pos.getZ() == to.blockPos.getZ() ? to.bitZ : 15) - bitZ + 1;

			return new ChiselTypeIterator(VoxelBlob.dim, bitX, bitY, bitZ, scaleX, scaleY, scaleZ, side);
		}

		return ChiselTypeIterator.create(VoxelBlob.dim, from.bitX, from.bitY, from.bitZ, vb, mode, side, place.usePlacementOffset());
	}

	@Override
	public NonNullList<ItemStack> createStackWithCount(ItemStack stack, int count, IBagCap bag, EntityPlayer player)
	{
		return NonNullList.create();
	}

}
