package tschipp.buildersbag.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.item.Item;
import tschipp.buildersbag.BuildersBag;
import tschipp.buildersbag.api.RequirementListener.ItemCreationRequirements;

import java.util.*;

public class CreateableItemsManager {
    private Set<Item> currentlyCreateable = new HashSet<>();
    private Multimap<Item, ItemCreationRequirements> currentlyProvidedFrom = HashMultimap.create();
    private Map<ItemCreationRequirements, Tracker> activeRequirements = new HashMap<>();
    private Multimap<IngredientKey, Item> activeIngredients = HashMultimap.create();
    private IBagModule module;

    public CreateableItemsManager(IBagModule module) {
        this.module = module;
    }

    /*
     * Marks that item can be created somehow, keep track on what requirement(s)
     * it is based, sends notifications if new items are created.
     */
    public void add(BagComplex complex, ItemCreationRequirements req, IngredientKey item, Item notificationSource) {
        activeIngredients.put(item, notificationSource);
        Tracker track = activeRequirements.getOrDefault(req, new Tracker(req));
        if (track.add(item)) {
            currentlyCreateable.add(req.getOutput());
            currentlyProvidedFrom.put(req.getOutput(), req);
            complex.getInventory().addCraftable(req.getOutput(), module, req);
        }
        activeRequirements.put(req, track);
    }

    public void remove(BagComplex complex, ItemCreationRequirements req, IngredientKey item, Item notificationSource) {
        if (notificationSource.getRegistryName().toString().equals("minecraft:birch_planks"))
            System.out.println("Foo");

        activeIngredients.remove(item, notificationSource);

        if (!activeRequirements.containsKey(req))
            return;

        if (activeIngredients.get(item).isEmpty()) {
            Tracker track = activeRequirements.get(req);
            if (track.remove(item)) {
                activeRequirements.remove(req);
            }

            if (currentlyProvidedFrom.remove(req.getOutput(), req) && currentlyProvidedFrom.get(req.getOutput()).isEmpty()) {
                currentlyCreateable.remove(req.getOutput());
                complex.getInventory().removeCraftable(req.getOutput(), module, req);
                removePotentialCycles(complex, req.getOutput());

            }
        }

    }

    // Consider the case coal, coal block. If we use up the coal block, we will
    // never get notified that we don't have it anymore, because
    // it could still technically be created through the coal items. If we now
    // use those as well, we also won't get notified when they're
    // totally removed and we'll have a cycle without entrypoint. This function
    // finds and removes those cycles
    // TODO still seems broken with chisel
    public void removePotentialCycles(BagComplex complex, Item cycleStart) {
        BuildersBag.LOGGER.debug("Trying to remove cycle starting at " + cycleStart);

//		Stack<ItemCreationRequirements> stack = new Stack<>();
//		boolean removal = false;
//		start: for (ItemCreationRequirements req : currentlyProvidedFrom.get(cycleStart))
//		{
//			stack.clear();
//			stack.push(req);
//			for (IngredientKey ings : req.getRequirements())
//			{
//				for (Item active : activeIngredients.get(ings))
//				{
//					// for(ItemCreationRequirements it :
//					// currentlyProvidedFrom.get(active))
//					// {
//					if (removeCyclesRec(complex, stack, active))
//					{
//						removal = true;
//						break start;
//					}
//					// }
//				}
//			}
//		}
//
//		if(removal)
//		{
//			BuildersBag.LOGGER.info("Marking " + cycleStart + " as removed because of a cycle");
//			complex.notifyItemRemoved(cycleStart);
//
//			for(ItemCreationRequirements req : stack)
//				removePotentialCycles(complex, req.getOutput());
//		}

        Set<Item> connectedItems = new HashSet<>();
        addConnectedItems(connectedItems, cycleStart);

        boolean hasAny = false;
        for (Item it : connectedItems) {
            if (complex.getInventory().hasPhysical(it, 1) || complex.getInventory().isCraftableExcept(it, module)) {
                hasAny = true;
            }
        }

        if (!hasAny) {
            connectedItems.remove(cycleStart);
            for (Item it : connectedItems) {
                if (complex.getInventory().hasPhysical(it, 1) || complex.getInventory().isCraftableExcept(it, module)) {
                    BuildersBag.LOGGER.info("Marking " + it + " as removed because of a cycle");
                    complex.notifyItemRemoved(it);
                }
            }
        }

    }

    private void addConnectedItems(Set<Item> set, Item start) {
        for (ItemCreationRequirements req : currentlyProvidedFrom.get(start)) {
            for (IngredientKey ings : req.getRequirements()) {
                for (Item active : activeIngredients.get(ings)) {
                    if (!set.contains(active)) {
                        set.add(active);
                        addConnectedItems(set, active);
                    }
                }
            }
        }
    }

    // Return true if all cycles starting from this item are unfulfilled
    private boolean removeCyclesRec(BagComplex complex, Stack<ItemCreationRequirements> stack, Item current) {
        boolean hasAny = false;

        for (ItemCreationRequirements req : currentlyProvidedFrom.get(current)) {
            // Found cycle
            if (stack.contains(req)) {
                for (ItemCreationRequirements r : stack) {
                    if (complex.getInventory().hasPhysical(r.getOutput(), 1)) {
                        return false;
                    }
                }
//				ItemCreationRequirements rem;
//				do {
//					 rem = stack.pop();
//				} while(!stack.isEmpty() && rem != req);
//				
//				stack.push(req);

            } else {
                // No cycle yet
                stack.push(req);
                for (IngredientKey ings : req.getRequirements()) {
                    for (Item active : activeIngredients.get(ings)) {
                        // for(ItemCreationRequirements it :
                        // currentlyProvidedFrom.get(active))
                        // {
                        hasAny &= removeCyclesRec(complex, stack, active);
                        // }
                    }
                }
            }
        }

        // Doesn't have any item of the cycle physically, remove the
        // entire cycle
        if (hasAny) {
            BuildersBag.LOGGER.info("Found unsatisfied Cycle: " + stack);

            return true;
        }

        return false;
    }

    static class Tracker {

        private final Set<IngredientKey> requirements;
        private final Set<IngredientKey> active = new HashSet<IngredientKey>();

        public Tracker(ItemCreationRequirements req) {
            requirements = req.getRequirements();
        }

        /*
         * marks item as available. Returns true if all are available
         */
        public boolean add(IngredientKey item) {
            active.add(item);
            if (active.containsAll(requirements))
                return true;
            return false;
        }

        /*
         * mark item as unavailable. Returns true if none are available.
         */
        public boolean remove(IngredientKey item) {
            active.remove(item);
            if (active.isEmpty())
                return true;
            return false;
        }
    }
}
