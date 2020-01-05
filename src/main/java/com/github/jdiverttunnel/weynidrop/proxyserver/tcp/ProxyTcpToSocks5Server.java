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

package com.github.jdiverttunnel.weynidrop.proxyserver.tcp;

import com.github.jdiverttunnel.weynidrop.common.cache.AddrMappingCache;
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;
import com.github.jdiverttunnel.weynidrop.common.enums.PacketTypeEnum;
import com.github.jdiverttunnel.weynidrop.common.net.Socks5Proxy;
import com.github.jdiverttunnel.weynidrop.common.thread.ThreadManager;
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;
import com.github.jdiverttunnel.weynidrop.common.enums.PacketTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 〈代理服务器，作用是将所有收到的连接请求封装为socks5发送到socks5代理服务器〉
 *
 */
public class ProxyTcpToSocks5Server implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyTcpToSocks5Server.class);

    private int port;
    private final Socks5Proxy proxy;

    private ServerSocket serverSocket;

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }


    public ProxyTcpToSocks5Server(int port, Socks5Proxy proxy) {
        this.port = port;
        this.proxy = proxy;
    }

    public void close() {
        try {
            ThreadManager.getInstance().remove(this);
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
                AddressMapping addressMapping = AddrMappingCache.get(AddressMapping.getLocalInfo(socketAddress, PacketTypeEnum.TCP));
                if (addressMapping == null) {
                    LOGGER.debug("没有找到包路由");
                } else {
                    TcpHandler tcpHandler = new TcpHandler(socket, proxy, addressMapping);
                    ThreadManager.getInstance().execute(tcpHandler);
                }
            }
        } catch (Exception e) {
            LOGGER.error("tcp中转服务器异常：", e);
        } finally {
            LOGGER.info("tcp中转服务器关闭");
            this.close();
        }
    }
}
