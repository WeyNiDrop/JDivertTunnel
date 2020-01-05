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
import com.github.jdiverttunnel.weynidrop.common.enums.PacketTypeEnum;
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;
import com.github.jdiverttunnel.weynidrop.tunnel.filter.WinDivertFilter;
import com.github.ffalcinelli.jdivert.Packet;
import com.github.ffalcinelli.jdivert.headers.Tcp;
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;
import com.github.jdiverttunnel.weynidrop.common.enums.PacketTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientPacketProcessor extends AbstractPacketDirectionProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientPacketProcessor.class);
    private int proxyPort;

    public ClientPacketProcessor(WinDivertFilter filter, int proxyPort) {
        //指向服务器ip端口的过滤器
        super(filter);
        this.proxyPort = proxyPort;
    }

    @Override
    public void onTcp(Packet packet) {
        if (packet.getTcp().is(Tcp.Flag.SYN)) {
            //记录本地包mapping
            AddressMapping addressMapping = new AddressMapping(packet.getSrcAddr(), packet.getSrcPort(), packet.getDstAddr(), packet.getDstPort(), PacketTypeEnum.TCP);
            AddrMappingCache.put(addressMapping.getLocalInfo(), addressMapping);
        }
        divertToProxy(packet);
    }

    @Override
    public void onUdp(Packet packet) {
        //记录本地包mapping
        AddressMapping addressMapping = new AddressMapping(packet.getSrcAddr(),packet.getSrcPort(),packet.getDstAddr(),packet.getDstPort(),PacketTypeEnum.UDP);
        AddrMappingCache.put(addressMapping.getLocalInfo(),addressMapping);
        divertToProxy(packet);
    }

    /**
     * 设置包方向到服务器ip
     * @param packet
     */
    private void divertToProxy(Packet packet){
        try {
            packet.setDstAddr(packet.getSrcAddr());
            packet.setDstPort(proxyPort);
            packet.recalculateChecksum();
        }catch (Exception e){
            LOGGER.error("从客户端转包到服务端失败：",e);
        }
    }
}
