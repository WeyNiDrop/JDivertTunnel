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

import com.github.jdiverttunnel.weynidrop.tunnel.filter.SocketPacketSrcFilter;
import com.github.ffalcinelli.jdivert.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 〈过滤指定ip端口发出的数据包处理 暂时不使用〉
 */
@Deprecated
public class ServerPacketProcessor extends AbstractPacketDirectionProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPacketProcessor.class);

    public ServerPacketProcessor(String ip, int port) {
        //过滤服务器发出的包
        super(new SocketPacketSrcFilter(ip,port));
    }

    @Override
    public void onTcp(Packet packet) {

    }

    @Override
    public void onUdp(Packet packet) {

    }

}
