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

package com.github.jdiverttunnel.weynidrop.proxyserver.udp;

import com.github.jdiverttunnel.weynidrop.common.cache.AddrMappingCache;
import com.github.jdiverttunnel.weynidrop.common.cache.UdpConnectorMappingCache;
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;
import com.github.jdiverttunnel.weynidrop.common.enums.PacketTypeEnum;
import com.github.jdiverttunnel.weynidrop.common.net.Socks5Proxy;
import com.github.jdiverttunnel.weynidrop.common.thread.ThreadManager;
import com.github.jdiverttunnel.weynidrop.socks5client.udp.Socks5UdpClient;
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;
import com.github.jdiverttunnel.weynidrop.common.enums.PacketTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * 〈代理服务器，作用是将所有收到的连接请求封装为socks5发送到socks5代理服务器〉
 */
public class ProxyUdpToSocks5Server implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyUdpToSocks5Server.class);

    private int port;
    private Socks5Proxy proxy;

    private DatagramSocket server;

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }


    public ProxyUdpToSocks5Server(int port, Socks5Proxy proxy) {
        this.port = port;
        this.proxy = proxy;
    }

    public void stop() {
        ThreadManager.getInstance().remove(this);
        server.close();
        LOGGER.info("udp proxy server stop");
    }

    @Override
    public void run() {
        try {
            server = new DatagramSocket(port);
            byte[] buffer = new byte[1024 * 2];
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                server.receive(packet);
                String localAddInfo = AddressMapping.getLocalInfo((InetSocketAddress) packet.getSocketAddress(), PacketTypeEnum.UDP);
                AddressMapping addressMapping = AddrMappingCache.get(localAddInfo);
                if (addressMapping == null) {
                    LOGGER.error("错误的连接，没有找到地址映射");
                } else {
                    Socks5UdpClient connector = UdpConnectorMappingCache.get(localAddInfo);
                    if (connector == null) {
                        //启动新的socks5client
                        connector = new Socks5UdpClient(proxy, addressMapping, server);
                        //添加路由
                        UdpConnectorMappingCache.put(localAddInfo, connector);
                        ThreadManager.getInstance().execute(connector);
                    }
                    //转发数据
                    connector.send(packet);
                }
            }
        } catch (Exception e) {
            LOGGER.error("udp中转服务器启动异常：", e);
        } finally {
            LOGGER.info("udp中转服务器关闭");
            stop();
        }
    }
}
