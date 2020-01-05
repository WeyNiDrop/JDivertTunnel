package com.github.jdiverttunnel.weynidrop.tunnel.filter;

import com.github.ffalcinelli.jdivert.Packet;
import com.github.jdiverttunnel.weynidrop.common.constants.IpConstants;
import com.github.jdiverttunnel.weynidrop.common.utils.IpUtils;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * filter 实现对中国ip的过滤
 */
public class OutboundChineseIpFilter implements WinDivertFilter {
    private Set<String> ips;

    public OutboundChineseIpFilter(InetSocketAddress proxyAddress) {
        if (!IpUtils.isIp4(proxyAddress.getHostString()) && !IpUtils.isIp6(proxyAddress.getHostString())) {
            ips = IpUtils.getAllIP(proxyAddress.getHostString());
        } else {
            ips = new HashSet<>();
            ips.add(proxyAddress.getHostString());
        }
    }

    @Override
    public Boolean match(Object obj) {
        Packet packet = (Packet) obj;
        Boolean isChineseIp = IpUtils.isChineseMainLandIp(packet.getDstAddr());
        if (isChineseIp == null) {
            //特殊ip 如内网和循环ip，不做处理
            return false;
        }
        return isChineseIp;
    }

    @Override
    public String getFilter() {
        StringBuilder sb = new StringBuilder();
        ips.forEach(ip -> {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            String version = IpUtils.checkIPVersion(ip);
            sb.append(IpConstants.IPV6.equals(version) ? "ipv6.DstAddr != " : "ip.DstAddr != ").append(ip);
        });
        return "outbound and (tcp or udp)";
    }
}
