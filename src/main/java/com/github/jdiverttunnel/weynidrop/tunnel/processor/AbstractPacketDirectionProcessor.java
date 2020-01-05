/*
 * Copyright (c) WeyNiDrop 2020.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.jdiverttunnel.weynidrop.tunnel.processor;

import com.github.jdiverttunnel.weynidrop.tunnel.filter.DefaultFilter;
import com.github.jdiverttunnel.weynidrop.tunnel.filter.WinDivertFilter;
import com.github.jdiverttunnel.weynidrop.tunnel.handler.PacketHandler;
import com.github.ffalcinelli.jdivert.Packet;

/**
 * 〈筛选指定来源的包，交给processor处理〉
 */
public abstract class AbstractPacketDirectionProcessor implements PacketHandler, PacketTypeProcessor {
    private WinDivertFilter filter;

    protected AbstractPacketDirectionProcessor(WinDivertFilter filter) {
        if (filter == null){
            filter = DefaultFilter.DEFAULT;
        }
        this.filter = filter;
    }

    @Override
    public WinDivertFilter getFilter() {
        return filter;
    }

    @Override
    public void onReceivePacket(Packet packet) {
        if (packet.isTcp()){
            this.onTcp(packet);
        }
        if (packet.isUdp()){
            this.onUdp(packet);
        }
    }

    @Override
    public void onCatchException(Exception e) {

    }

    @Override
    public void onTcp(Packet packet) {

    }

    @Override
    public void onUdp(Packet packet) {

    }
}
