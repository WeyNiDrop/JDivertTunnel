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

package com.github.jdiverttunnel.weynidrop.tunnel.handler;

import com.github.ffalcinelli.jdivert.Packet;
import com.github.jdiverttunnel.weynidrop.tunnel.filter.WinDivertFilter;

public interface PacketHandler {
    /**
     * filter packet such as udp,tcp,icmp
     * @return WinDivertFilter
     */
    WinDivertFilter getFilter();

    /**
     * 捕获数据包
     * @param packet
     */
    void onReceivePacket(Packet packet);

    /**
     * 发生异常
     * @param e
     */
    void onCatchException(Exception e);
}
