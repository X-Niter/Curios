/*
 * Copyright (C) 2018-2019  C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curios.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.capability.CuriosCapability;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.api.capability.ICurioItemHandler;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CuriosAPI {

    /**
     * @param stack The ItemStack to get the curio capability from
     * @return  LazyOptional of the curio capability attached to the ItemStack
     */
    public static LazyOptional<ICurio> getCurio(ItemStack stack) {
        return stack.getCapability(CuriosCapability.ITEM);
    }

    /**
     * @param entityLivingBase  The ItemStack to get the curio inventory capability from
     * @return  LazyOptional of the curio inventory capability attached to the entity
     */
    public static LazyOptional<ICurioItemHandler> getCuriosHandler(@Nonnull final EntityLivingBase entityLivingBase) {
        return entityLivingBase.getCapability(CuriosCapability.INVENTORY);
    }

    /**
     * @param identifier    The unique identifier for the {@link CurioType}
     * @return  The CurioType from the given identifier
     */
    @Nullable
    public static CurioType getType(String identifier) {
        return CuriosRegistry.idToType.get(identifier);
    }

    /**
     * @return  An unmodifiable list of all unique registered identifiers
     */
    public static ImmutableSet<String> getTypeIdentifiers() { return ImmutableSet.copyOf(CuriosRegistry.idToType.keySet()); }

    /**
     * Gets the first found ItemStack of the item type equipped in a curio slot, or null if no matches were found.
     * @param item              The item to find
     * @param entityLivingBase  The wearer of the item to be found
     * @return  An instance of {@link FinderData} indicating the identifier of the curio slot, slot index, and the ItemStack
     * of the first found ItemStack matching the parameters. Null if no matches were found.
     */
    @Nullable
    public static FinderData getCurioEquipped(Item item, @Nonnull final EntityLivingBase entityLivingBase) {
        FinderData found = getCuriosHandler(entityLivingBase).map(handler -> {
            Set<String> tags = getCurioTags(item);

            for (String id : tags) {
                CurioStackHandler stackHandler = handler.getStackHandler(id);

                if (stackHandler != null) {

                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);

                        if (!stack.isEmpty() && item == stack.getItem()) {
                            return new FinderData(id, i, stack);
                        }
                    }
                }
            }
            return new FinderData("", 0, ItemStack.EMPTY);
        }).orElse(new FinderData("", 0, ItemStack.EMPTY));

        if (!found.getIdentifier().isEmpty()) {
            return found;
        } else {
            return null;
        }
    }

    /**
     * Gets the first found ItemStack of the item type equipped in a curio slot that matches the filter, or null if no
     * matches were found.
     * @param filter            The filter to test the ItemStack against
     * @param entityLivingBase  The wearer of the item to be found
     * @return  An instance of {@link FinderData} indicating the identifier of the curio slot, slot index, and the ItemStack
     * of the first found ItemStack matching the parameters. Null if no matches were found.
     */
    @Nullable
    public static FinderData getCurioEquipped(Predicate<ItemStack> filter, @Nonnull final EntityLivingBase entityLivingBase) {
        FinderData found = getCuriosHandler(entityLivingBase).map(handler -> {

            for (String id : handler.getCurioMap().keySet()) {
                CurioStackHandler stackHandler = handler.getStackHandler(id);

                if (stackHandler != null) {

                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);

                        if (!stack.isEmpty() && filter.test(stack)) {
                            return new FinderData(id, i, stack);
                        }
                    }
                }
            }
            return new FinderData("", 0, ItemStack.EMPTY);
        }).orElse(new FinderData("", 0, ItemStack.EMPTY));

        if (!found.getIdentifier().isEmpty()) {
            return found;
        } else {
            return null;
        }
    }

    /**
     * Adds a single slot to the {@link CurioType} with the associated identifier
     * If the slot to be added is for a type that is not enabled on the entity, it will not be added. For adding slot(s)
     * for types that are not yet available, there must first be a call to {@link CuriosAPI#enableTypeForEntity(String, EntityLivingBase)}
     * @param id                The identifier of the CurioType
     * @param entityLivingBase  The holder of the slot(s)
     */
    public static void addTypeSlotToEntity(String id, final EntityLivingBase entityLivingBase) {
        addTypeSlotsToEntity(id, 1, entityLivingBase);
    }

    /**
     * Adds multiple slots to the {@link CurioType} with the associated identifier
     * If the slot to be added is for a type that is not enabled on the entity, it will not be added. For adding slot(s)
     * for types that are not yet available, there must first be a call to {@link CuriosAPI#enableTypeForEntity(String, EntityLivingBase)}
     * @param id                The identifier of the CurioType
     * @param amount            The number of slots to add
     * @param entityLivingBase  The holder of the slots
     */
    public static void addTypeSlotsToEntity(String id, int amount, final EntityLivingBase entityLivingBase) {
        getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.addCurioSlot(id, amount));
    }

    /**
     * Removes a single slot to the {@link CurioType} with the associated identifier
     * If the slot to be removed is the last slot available, it will not be removed. For the removal of the last slot,
     * please see {@link CuriosAPI#disableTypeForEntity(String, EntityLivingBase)}
     * @param id                The identifier of the CurioType
     * @param entityLivingBase  The holder of the slot(s)
     */
    public static void removeTypeSlotFromEntity(String id, final EntityLivingBase entityLivingBase) {
        removeTypeSlotsFromEntity(id, 1, entityLivingBase);
    }

    /**
     * Removes multiple slots to the {@link CurioType} with the associated identifier
     * If the slot to be removed is the last slot available, it will not be removed. For the removal of the last slot,
     * please see {@link CuriosAPI#disableTypeForEntity(String, EntityLivingBase)}
     * @param id                The identifier of the CurioType
     * @param entityLivingBase  The holder of the slot(s)
     */
    public static void removeTypeSlotsFromEntity(String id, int amount, final EntityLivingBase entityLivingBase) {
        getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.removeCurioSlot(id, amount));
    }

    /**
     * Adds a {@link CurioType} to the entity
     * The number of slots given is the type's default
     * @param id                The identifier of the CurioType
     * @param entityLivingBase  The holder of the slot(s)
     */
    public static void enableTypeForEntity(String id, final EntityLivingBase entityLivingBase) {
        getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.enableCurio(id));
    }

    /**
     * Removes a {@link CurioType} from the entity
     * @param id                The identifier of the CurioType
     * @param entityLivingBase  The holder of the slot(s)
     */
    public static void disableTypeForEntity(String id, final EntityLivingBase entityLivingBase) {
        getCuriosHandler(entityLivingBase).ifPresent(handler -> handler.disableCurio(id));
    }

    private static Map<Item, Set<String>> itemToTypes = new HashMap<>();
    private static Map<String, Tag<Item>> idToTag = new HashMap<>();

    /**
     * Retrieves a set of string identifiers from the curio tags associated with the given item
     * @param item  The item to retrieve curio tags for
     * @return      Unmodifiable list of unique curio identifiers associated with the item
     */
    public static ImmutableSet<String> getCurioTags(Item item) {

        if (idToTag.isEmpty()) {
            idToTag = ItemTags.getCollection().getTagMap().entrySet()
                    .stream()
                    .filter(map -> map.getKey().getNamespace().equals(Curios.MODID))
                    .collect(Collectors.toMap(entry -> entry.getKey().getPath(), Map.Entry::getValue));
            itemToTypes.clear();
        }

        if (itemToTypes.containsKey(item)) {
            return ImmutableSet.copyOf(itemToTypes.get(item));
        } else {
            Set<String> tags = Sets.newHashSet();

            for (String identifier : idToTag.keySet()) {

                if (idToTag.get(identifier).contains(item)) {
                    tags.add(identifier);
                }
            }
            itemToTypes.put(item, tags);
            return ImmutableSet.copyOf(tags);
        }
    }

    /**
     * Registers a resource location to be used as the slot overlay icon in the GUI
     * @param id                The identifier of the type of curio to be associated with the icon
     * @param resourceLocation  The resource location of the icon
     */
    public static void registerIcon(String id, @Nonnull ResourceLocation resourceLocation) {
        CuriosRegistry.iconQueues.computeIfAbsent(id, k -> new ConcurrentSet<>()).add(resourceLocation);
    }

    /**
     * @return  A map of identifiers and their registered icons
     */
    public static Map<String, ResourceLocation> getIcons() {
        return ImmutableMap.copyOf(CuriosRegistry.icons);
    }

    /**
     * Holder class for IMC message identifiers
     */
    public final static class IMC {

        public static final String REGISTER_TYPE = "register_type";
        public static final String MODIFY_TYPE = "modify_type";
    }

    public final static class FinderData {

        String identifier;
        int index;
        ItemStack stack;

        public FinderData(String identifier, int index, ItemStack stack) {
            this.identifier = identifier;
            this.index = index;
            this.stack = stack;
        }

        public String getIdentifier() {
            return identifier;
        }

        public int getIndex() {
            return index;
        }

        public ItemStack getStack() {
            return stack;
        }
    }
}
