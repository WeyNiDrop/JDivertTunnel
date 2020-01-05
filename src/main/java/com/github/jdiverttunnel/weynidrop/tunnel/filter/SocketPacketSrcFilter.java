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

package com.github.jdiverttunnel.weynidrop.tunnel.filter;

import com.github.ffalcinelli.jdivert.Packet;
import com.github.jdiverttunnel.weynidrop.common.constants.IpConstants;
import com.github.jdiverttunnel.weynidrop.common.utils.IpUtils;

public class SocketPacketSrcFilter implements WinDivertFilter {
    private String ip;
    private int port;

    public SocketPacketSrcFilter(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public Boolean match(Object obj) {
        Packet packet = (Packet) obj;
        return port == 0 ? packet.getSrcAddr().equals(ip) : packet.getSrcAddr().equals(ip) && packet.getSrcPort() == port;
    }

    @Override
    public String getFilter() {
        String version = IpUtils.checkIPVersion(ip);
        StringBuilder sb = new StringBuilder();
        sb.append(IpConstants.IPV6.equals(version) ? "(ipv6.SrcAddr == " : "(ip.SrcAddr == ").append(ip);
        if (port != 0) {
            sb.append(" and (tcp.SrcPort == ").append(port).append(" or udp.SrcPort == ").append(port).append(")");
        }
        sb.append(")");
        return sb.toString();
    }
}
