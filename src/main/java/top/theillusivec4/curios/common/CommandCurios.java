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

package top.theillusivec4.curios.common;

import com.google.common.collect.Maps;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import top.theillusivec4.curios.api.CurioType;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;
import top.theillusivec4.curios.common.network.NetworkHandler;
import top.theillusivec4.curios.common.network.server.sync.SPacketSyncMap;

import java.util.SortedMap;

public class CommandCurios {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {

        LiteralArgumentBuilder<CommandSource> literalargumentbuilder = Commands.literal("curios")
                .requires(player -> player.hasPermissionLevel(ServerLifecycleHooks.getCurrentServer().getOpPermissionLevel()));

        literalargumentbuilder.then(Commands.literal("add")
                .then(Commands.argument("slot", StringArgumentType.string())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosAPI.getTypeIdentifiers(), builder))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> addSlotToPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "slot"), 1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> addSlotToPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "slot"), IntegerArgumentType.getInteger(context, "amount")))))));

        literalargumentbuilder.then(Commands.literal("remove")
                .then(Commands.argument("slot", StringArgumentType.string())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosAPI.getTypeIdentifiers(), builder))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> removeSlotFromPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "slot"), 1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(context -> removeSlotFromPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "slot"), IntegerArgumentType.getInteger(context, "amount")))))));

        literalargumentbuilder.then(Commands.literal("enable")
                .then(Commands.argument("slot", StringArgumentType.string())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosAPI.getTypeIdentifiers(), builder))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> enableSlotForPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "slot"))))));

        literalargumentbuilder.then(Commands.literal("disable")
                .then(Commands.argument("slot", StringArgumentType.string())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosAPI.getTypeIdentifiers(), builder))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> disableSlotForPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "slot"))))));

        literalargumentbuilder.then(Commands.literal("clear")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> clearSlotsForPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), ""))
                                .then(Commands.argument("slot", StringArgumentType.string())
                                        .suggests((ctx, builder) -> ISuggestionProvider.suggest(CuriosAPI.getTypeIdentifiers(), builder))
                                        .executes(context -> clearSlotsForPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "slot"))))));

        literalargumentbuilder.then(Commands.literal("reset")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> resetSlotsForPlayer(context.getSource(), EntityArgument.getPlayer(context, "player")))));

        dispatcher.register(literalargumentbuilder);
    }

    private static int addSlotToPlayer(CommandSource source, EntityPlayerMP playerMP, String slot, int amount) {
        CuriosAPI.addTypeSlotsToEntity(slot, amount, playerMP);
        source.sendFeedback(new TextComponentTranslation("commands.curios.add.success", amount, slot, playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeSlotFromPlayer(CommandSource source, EntityPlayerMP playerMP, String slot, int amount) {
        CuriosAPI.removeTypeSlotsFromEntity(slot, amount, playerMP);
        source.sendFeedback(new TextComponentTranslation("commands.curios.remove.success", amount, slot, playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int enableSlotForPlayer(CommandSource source, EntityPlayerMP playerMP, String slot) {
        CuriosAPI.enableTypeForEntity(slot, playerMP);
        source.sendFeedback(new TextComponentTranslation("commands.curios.enable.success", slot, playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int disableSlotForPlayer(CommandSource source, EntityPlayerMP playerMP, String slot) {
        CuriosAPI.disableTypeForEntity(slot, playerMP);
        source.sendFeedback(new TextComponentTranslation("commands.curios.disable.success", slot, playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int clearSlotsForPlayer(CommandSource source, EntityPlayerMP playerMP, String slot) {
        CuriosAPI.getCuriosHandler(playerMP).ifPresent(handler -> {
            SortedMap<String, CurioStackHandler> map = handler.getCurioMap();

            if (!slot.isEmpty() && map.get(slot) != null) {
                clear(map.get(slot));
            } else {

                for (String id : map.keySet()) {
                    clear(map.get(id));
                }
            }
        });

        if (slot.isEmpty()) {
            source.sendFeedback(new TextComponentTranslation("commands.curios.clearAll.success", playerMP.getDisplayName()), true);
        } else {
            source.sendFeedback(new TextComponentTranslation("commands.curios.clear.success", slot, playerMP.getDisplayName()), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int resetSlotsForPlayer(CommandSource source, EntityPlayerMP playerMP) {
        CuriosAPI.getCuriosHandler(playerMP).ifPresent(handler -> {
            SortedMap<String, CurioStackHandler> slots = Maps.newTreeMap();

            for (String id : CuriosAPI.getTypeIdentifiers()) {
                CurioType type = CuriosAPI.getType(id);

                if (type != null && type.isEnabled()) {
                    slots.put(id, new CurioStackHandler(type.getSize()));
                }
            }
            handler.setCurioMap(slots);
            NetworkHandler.INSTANCE.sendTo(new SPacketSyncMap(playerMP.getEntityId(), handler.getCurioMap()),
                    playerMP.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
        });
        source.sendFeedback(new TextComponentTranslation("commands.curios.reset.success", playerMP.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static void clear(CurioStackHandler stacks) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            stacks.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
