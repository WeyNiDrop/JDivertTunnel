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

import com.github.jdiverttunnel.weynidrop.common.cache.DnsCache;
import com.github.jdiverttunnel.weynidrop.common.constants.IpConstants;
import com.github.jdiverttunnel.weynidrop.common.utils.IpUtils;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * filter语法参考：https://reqrypt.org/windivert-doc-1.4.html#filter_language
 */
public class SocketPacketDstFilter implements WinDivertFilter {
    private Set<String> ips;
    private int port;

    private InetSocketAddress socketAddress;

    /**
     * @return the socketAddress
     */
    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public SocketPacketDstFilter(int port, String... ips) {
        this.ips = new HashSet<>();
        this.ips.addAll(Arrays.asList(ips));
        this.port = port;
    }

    public SocketPacketDstFilter(InetSocketAddress socketAddress) throws UnknownHostException {
        this.socketAddress = socketAddress;
        //判断是否是domain
        if (IpUtils.checkIPVersion(socketAddress.getHostString()) == null){
            ips = IpUtils.getAllIP(socketAddress.getHostString());
            if (ips == null){
                throw new UnknownHostException("unknown host name: " + socketAddress.getHostString());
            }
            //缓存dns记录
            ips.forEach(ip->DnsCache.put(ip,socketAddress.getHostString()));
        }else {
            ips = new HashSet<>();
            ips.add(socketAddress.getHostString());
        }
        this.port = socketAddress.getPort();
    }

    @Override
    public Boolean match(Object obj) {
        //如果只针对过滤后的数据包进行处理，没有特殊需求可以直接返回true
        //Packet packet = (Packet) obj;
        //return port == -1 ? ips.contains(packet.getDstAddr()) : packet.getDstAddr().equals(ips) && packet.getDstPort() == port;
        return true;
    }

    /**
     * 获取过滤规则 filter
     * @return String
     */
    @Override
    public String getFilter() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        int initLen = sb.length();
        ips.forEach(ip -> {
            String version = IpUtils.checkIPVersion(ip);
            if (sb.length() > initLen) {
                sb.append(" or ");
            }
            sb.append(IpConstants.IPV6.equals(version) ? "ipv6.DstAddr == " : "ip.DstAddr == ").append(ip);
        });
        sb.append(")");
        if (port != 0) {
            sb.append(" and (tcp.DstPort == ").append(port).append(" or udp.DstPort == ").append(port).append(")");
        } else {
            sb.append(" and (tcp or udp)");
        }
        return sb.toString();
    }
}
