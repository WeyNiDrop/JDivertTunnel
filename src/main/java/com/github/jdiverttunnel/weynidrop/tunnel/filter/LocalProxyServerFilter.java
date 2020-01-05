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


import com.github.jdiverttunnel.weynidrop.common.constants.IpConstants;
import java.util.List;
import java.util.Map;

/**
 * 〈本地代理服务器的过滤规则〉
 */
public class LocalProxyServerFilter implements WinDivertFilter{
    private int proxyPort;
    private Map<String,List<String>> localIps;

    public LocalProxyServerFilter(int proxyPort, Map<String, List<String>> localIps) {
        this.proxyPort = proxyPort;
        this.localIps = localIps;
    }

    @Override
    public Boolean match(Object obj) {
        return true;
    }

    @Override
    public String getFilter() {
        List<String> ipv4s = localIps.get(IpConstants.IPV4);
        List<String> ipv6s = localIps.get(IpConstants.IPV6);
        StringBuilder sb = new StringBuilder();
        ipv4s.forEach(ip-> {
            sb.append(sb.length() == 0? "(":" or ");
            sb.append("ip.SrcAddr == ").append(ip);
        });
        ipv6s.forEach(ip-> {
            sb.append(sb.length() == 0? "(":" or ");
            sb.append("ipv6.SrcAddr == ").append(ip);
        });
        sb.append(") and (tcp.SrcPort == ").append(proxyPort).append(" or udp.SrcPort == ").append(proxyPort).append(")");
        return sb.toString();
    }
}
