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

package top.theillusivec4.curios.client.gui;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuiEventHandler {

    @SubscribeEvent
    public void onInventoryGui(GuiScreenEvent.InitGuiEvent.Post evt) {

        if (evt.getGui() instanceof GuiInventory) {
            GuiInventory gui = (GuiInventory)evt.getGui();
            evt.addButton(new GuiButtonCurios(gui,44, gui.getGuiLeft() + 26, gui.height / 2 - 75,
                    14, 14, 50, 0, 14, GuiContainerCurios.CURIO_INVENTORY));
        }
    }
}