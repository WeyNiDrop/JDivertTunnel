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

import com.github.jdiverttunnel.weynidrop.common.cache.AddrMappingCache;
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;
import com.github.jdiverttunnel.weynidrop.common.enums.PacketTypeEnum;
import com.github.jdiverttunnel.weynidrop.tunnel.filter.WinDivertFilter;
import com.github.ffalcinelli.jdivert.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 〈处理本地中转服务器发出的所有数据包〉
 */
public class LocalProxyServerPacketProcessor extends AbstractPacketDirectionProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalProxyServerPacketProcessor.class);

    public LocalProxyServerPacketProcessor(WinDivertFilter filter) {
        super(filter);
    }

    @Override
    public void onTcp(Packet packet) {
        divertPacket(packet,PacketTypeEnum.TCP);
    }

    @Override
    public void onUdp(Packet packet) {
        divertPacket(packet,PacketTypeEnum.UDP);
    }

    private void divertPacket(Packet packet,PacketTypeEnum type) {
        try {
            AddressMapping addressMapping = AddrMappingCache.get(AddressMapping.getLocalInfo(new InetSocketAddress(packet.getDstAddr(),packet.getDstPort()),type));
            if (addressMapping != null) {
                packet.setSrcAddr(addressMapping.getRemote().getAddress().getHostAddress());
                packet.setSrcPort(addressMapping.getRemote().getPort());
                packet.recalculateChecksum();
            } else {
                LOGGER.error("未找到包路由：{}", packet);
            }
        } catch (Exception e) {
            LOGGER.error("从服务端转包到客户端失败：", e);
        }

    }

}
