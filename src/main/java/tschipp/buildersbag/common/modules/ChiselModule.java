package tschipp.buildersbag.common.modules;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ObjectHolder;
import team.chisel.api.IChiselItem;
import team.chisel.api.carving.CarvingUtils;
import team.chisel.api.carving.ICarvingGroup;
import tschipp.buildersbag.api.AbstractBagModule;
import tschipp.buildersbag.api.BagComplex;
import tschipp.buildersbag.api.BagInventory;
import tschipp.buildersbag.api.BagModuleType;
import tschipp.buildersbag.api.CreateableItemsManager;
import tschipp.buildersbag.api.IBagModule;
import tschipp.buildersbag.api.IngredientKey;
import tschipp.buildersbag.api.IngredientMapper;
import tschipp.buildersbag.api.RequirementListener;
import tschipp.buildersbag.api.RequirementListener.ItemCreationRequirements;
import tschipp.buildersbag.common.BuildersBagRegistry;
import tschipp.buildersbag.common.inventory.ItemHandlerWithPredicate;

public class ChiselModule extends AbstractBagModule
{
	private ItemHandlerWithPredicate handler = new ItemHandlerWithPredicate(1, (stack, slot) -> stack.getItem() instanceof IChiselItem);
	private static ItemStack DISPLAY;
	private final CreateableItemsManager manager;
	
	@ObjectHolder("chisel:iron_chisel")
	public static final Item CHISEL_ITEM = null;

	public ChiselModule()
	{
		manager = new CreateableItemsManager(this);
	}

	// @Override
	// public NonNullList<ItemStack> getPossibleStacks(IBagCap bag, PlayerEntity
	// player)
	// {
	// NonNullList<ItemStack> providedSacks =
	// BagHelper.getAllAvailableStacksExcept(bag, player, this);
	// NonNullList<ItemStack> list = NonNullList.create();
	//
	// ItemStack chisel = handler.getStackInSlot(0);
	//
	// if (chisel.isEmpty() || !validTinkersChisel(chisel))
	// return list;
	//
	// Set<ICarvingGroup> groups = new HashSet<ICarvingGroup>();
	//
	// for (ItemStack stack : providedSacks)
	// {
	// if (!stack.isEmpty())
	// {
	// ICarvingGroup group = CarvingUtils.getChiselRegistry().getGroup(stack);
	// if (group != null)
	// groups.add(group);
	// }
	// }
	//
	// for (ICarvingGroup group : groups)
	// {
	// for (ICarvingVariation variation : group)
	// {
	// list.add(variation.getStack());
	// }
	// }
	//
	// return list;
	// }

	@Override
	public ItemStackHandler getInventory()
	{
		return handler;
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT tag = super.serializeNBT();
		tag.put("Inventory", handler.serializeNBT());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		super.deserializeNBT(nbt);
		handler.deserializeNBT(nbt.getCompound("Inventory"));
	}

	@Override
	public boolean doesntUseOwnInventory()
	{
		return false;
	}

	@Override
	public ItemStack getDisplayItem()
	{
		if (DISPLAY == null)
			DISPLAY = new ItemStack(CHISEL_ITEM);
		return DISPLAY;
	}

	// @Override
	// public NonNullList<ItemStack> createStackWithCount(ItemStack stack, int
	// count, IBagCap bag, PlayerEntity player)
	// {
	// NonNullList list = NonNullList.create();
	//
	// ICarvingGroup group = CarvingUtils.getChiselRegistry().getGroup(stack);
	// if (group == null)
	// return list;
	//
	// ItemStack chisel = handler.getStackInSlot(0);
	//
	// if (chisel.isEmpty() || !validTinkersChisel(chisel))
	// return list;
	//
	// NonNullList<ItemStack> availableBlocks =
	// InventoryHelper.getStacks(bag.getBlockInventory());
	// NonNullList<ItemStack> createableBlocks =
	// BagHelper.getAllAvailableStacksExcept(bag, player, this);
	//
	// NonNullList<ItemStack> providedVariants = NonNullList.create();
	//
	// for (ICarvingVariation variant : group)
	// {
	// while (providedVariants.size() < count)
	// {
	// ItemStack available = ItemStack.EMPTY;
	// if (!(available = ItemHelper.containsStack(variant.getStack(),
	// availableBlocks)).isEmpty())
	// {
	// providedVariants.add(available.splitStack(1));
	// }
	// else
	// break;
	// }
	// }
	//
	// for (ICarvingVariation variant : group)
	// {
	// if (providedVariants.size() < count &&
	// !ItemHelper.containsStack(variant.getStack(),
	// createableBlocks).isEmpty()) //This extra availability check is for
	// performance. Remove if causes issues
	// {
	// NonNullList<ItemStack> provided =
	// BagHelper.getOrProvideStackWithCount(variant.getStack(), count -
	// providedVariants.size(), bag, player, this);
	// providedVariants.addAll(provided);
	// }
	// }
	//
	// if (!providedVariants.isEmpty())
	// {
	// if (!player.level.isClientSide)
	// {
	// for (int i = 0; i < providedVariants.size(); i++)
	// {
	// if (!validTinkersChisel(chisel) || chisel.hurt(1, new Random(),
	// (ServerPlayerEntity) player))
	// {
	// list.add(stack.copy());
	//
	// if
	// (!chisel.getItem().getRegistryName().toString().equals("tcomplement:chisel"))
	// chisel.shrink(1);
	//
	// for (int j = i; j < providedVariants.size(); j++)
	// {
	// BagHelper.addStack(providedVariants.get(j), bag, player);
	// }
	//
	// break;
	// }
	// else
	// list.add(stack.copy());
	// }
	// }
	//
	// return list;
	// }
	//
	// return NonNullList.create();
	// }

	@Override
	public NonNullList<ItemStack> getCompactedStacks(NonNullList<ItemStack> toCompact, PlayerEntity player)
	{
		// TODO Chisel Compaction
		if (!isEnabled())
			return toCompact;

		ItemStack chisel = handler.getStackInSlot(0);
		if (chisel.isEmpty() || !validTinkersChisel(chisel))
			return toCompact;

		NonNullList<ItemStack> compacted = NonNullList.create();

		return compacted;
	}

	public static boolean validTinkersChisel(ItemStack stack)
	{
		if (stack.getItem().getRegistryName().toString().equals("tcomplement:chisel"))
		{
			return stack.getDamageValue() != stack.getMaxDamage();
		}
		else
			return true;
	}

	@Override
	public CreateableItemsManager getCreateableItemsManager()
	{
		return manager;
	}

	@Override
	public int createItems(ItemCreationRequirements req, int count, BagComplex complex, PlayerEntity player)
	{
		int created = 0;

		BagInventory inv = complex.getInventory();

		Set<Item> physical = complex.getPhysicalItems();
		ItemStack chisel = handler.getStackInSlot(0);
		if (chisel.isEmpty() || !validTinkersChisel(chisel))
			return 0;
		
		physical:
		for (IngredientKey ing : req.getRequirements())
		{
			Set<Item> filtered = Streams.stream(ing).filter(physical::contains).collect(Collectors.toSet());
			for (Item it : filtered)
			{
				int made = complex.take(it, Math.min(count-created, Math.min(inv.getPhysical(it), chisel.getMaxDamage() - chisel.getDamageValue())), player);
				created += made;
				
				chisel.hurtAndBreak(made, player, (e) -> {});
				
				if (chisel.isEmpty() || !validTinkersChisel(chisel))
					return created;
				
				if(created >= count)
					break physical;
			}
		}
		
		craftable:
		for (IngredientKey ing : req.getRequirements())
		{
			if(created >= count)
				break craftable;
			
			for (Item it : ing.sortedBiased(complex, BuildersBagRegistry.MODULE_CHISEL))
			{
//				System.out.println(chisel.getMaxDamage() - chisel.getDamageValue());
				int made = complex.take(it, Math.min(count-created, chisel.getMaxDamage() - chisel.getDamageValue()), player);
				created += made;
				
				chisel.hurtAndBreak(made, player, (e) -> {e.broadcastBreakEvent(EquipmentSlotType.MAINHAND);});
				
				if (chisel.isEmpty() || !validTinkersChisel(chisel))
					return created;
				
				if(created >= count)
					break craftable;
			}
		}

		return created;
	}
	
	@Override
	public BagModuleType<? extends IBagModule> getType()
	{
		return BuildersBagRegistry.MODULE_CHISEL;
	}

	public static RequirementListener.Builder createRecipeListener(TagsUpdatedEvent event)
	{
		RequirementListener.Builder builder = RequirementListener.builder();

		List<ICarvingGroup> groups = CarvingUtils.chisel.getGroups();
		for (ICarvingGroup g : groups)
		{
			ITag<Item> tag = g.getItemTag();

			IngredientKey key = IngredientKey.of(tag.getValues());

			// Add a mapping for item -> IngredientKey
			for (Item i : tag.getValues())
			{
				IngredientMapper.addMapping(i, key);
				builder.add(i, null, key);
			}
		}

		return builder;
	}

}
