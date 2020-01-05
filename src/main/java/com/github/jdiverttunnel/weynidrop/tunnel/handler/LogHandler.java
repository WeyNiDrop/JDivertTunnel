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
import com.github.jdiverttunnel.weynidrop.tunnel.filter.DefaultFilter;
import com.github.jdiverttunnel.weynidrop.tunnel.filter.WinDivertFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogHandler implements PacketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogHandler.class);

    @Override
    public WinDivertFilter getFilter() {
        return DefaultFilter.DEFAULT;
    }

    @Override
    public void onReceivePacket(Packet packet) {
        LOGGER.debug("捕获数据包：{}", packet);
    }

    @Override
    public void onCatchException(Exception e) {
        LOGGER.error("WinDivert异常：", e);
    }
}
