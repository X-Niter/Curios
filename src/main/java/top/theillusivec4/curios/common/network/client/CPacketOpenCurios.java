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

package top.theillusivec4.curios.common.network.client;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import top.theillusivec4.curios.common.inventory.CurioContainerHandler;

import java.util.function.Supplier;

public class CPacketOpenCurios {

    private float oldMouseX;
    private float oldMouseY;

    public CPacketOpenCurios(float oldMouseX, float oldMouseY) {
        this.oldMouseX = oldMouseX;
        this.oldMouseY = oldMouseY;
    }

    public static void encode(CPacketOpenCurios msg, PacketBuffer buf) {
        buf.writeFloat(msg.oldMouseX);
        buf.writeFloat(msg.oldMouseY);
    }

    public static CPacketOpenCurios decode(PacketBuffer buf) {
        return new CPacketOpenCurios(buf.readFloat(), buf.readFloat());
    }

    public static void handle(CPacketOpenCurios msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            EntityPlayerMP sender = ctx.get().getSender();

            if (sender != null) {
                NetworkHooks.openGui(sender, new CurioContainerHandler(), buf -> {
                    buf.writeFloat(msg.oldMouseX);
                    buf.writeFloat(msg.oldMouseY);
                });
            }
        });
    }
}
