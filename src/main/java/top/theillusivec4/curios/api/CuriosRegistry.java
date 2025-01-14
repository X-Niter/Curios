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

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.imc.CurioIMCMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public class CuriosRegistry {

    static Map<String, CurioType> idToType = new HashMap<>();
    static ConcurrentMap<String, ConcurrentSet<ResourceLocation>> iconQueues = new ConcurrentHashMap<>();
    static Map<String, ResourceLocation> icons = new HashMap<>();

    public static void processCurioTypes(Stream<InterModComms.IMCMessage> register, Stream<InterModComms.IMCMessage> modify) {
        register
                .filter(msg -> msg.getMessageSupplier().get() instanceof CurioIMCMessage)
                .map(msg -> (CurioIMCMessage) msg.getMessageSupplier().get())
                .forEach(msg -> processType(msg, true));

        modify
                .filter(msg -> msg.getMessageSupplier().get() instanceof CurioIMCMessage)
                .map(msg -> (CurioIMCMessage) msg.getMessageSupplier().get())
                .forEach(msg -> processType(msg, false));
    }

    public static void processIcons() {

        if (!icons.isEmpty()) {
            icons = new HashMap<>();
        }
        iconQueues.forEach((k, v) -> {

            if (!icons.containsKey(k)) {
                List<ResourceLocation> sortedList = new ArrayList<>(v);
                Collections.sort(sortedList);
                icons.put(k, sortedList.get(sortedList.size() - 1));
            }
        });
    }

    private static void processType(CurioIMCMessage message, boolean create) {
        String identifier = message.getIdentifier();

        if (idToType.containsKey(identifier)) {
            CurioType presentType = idToType.get(identifier);

            if (message.getSize() > presentType.getSize()) {
                presentType.defaultSize(message.getSize());
            }

            if (!message.isEnabled() && presentType.isEnabled()) {
                presentType.enabled(false);
            }

            if (message.isHidden() && !presentType.isHidden()) {
                presentType.hide(true);
            }

        } else if (create) {
            idToType.put(identifier, new CurioType(identifier)
                    .defaultSize(message.getSize())
                    .enabled(message.isEnabled())
                    .hide(message.isHidden()));
        }
    }
}
