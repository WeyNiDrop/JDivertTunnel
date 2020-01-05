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
import com.github.jdiverttunnel.weynidrop.common.cache.DnsCache;
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;
import com.github.jdiverttunnel.weynidrop.common.net.Socks5Proxy;
import com.github.jdiverttunnel.weynidrop.common.net.SocksSocket;
import com.github.jdiverttunnel.weynidrop.common.thread.ThreadManager;
import com.github.jdiverttunnel.weynidrop.socks5client.tcp.StreamPipeThread;
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;
import com.github.jdiverttunnel.weynidrop.common.net.SocksSocket;
import com.github.jdiverttunnel.weynidrop.socks5client.tcp.StreamPipeThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpHandler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpHandler.class);

    private Socket socket;
    private Socks5Proxy proxy;
    private StreamPipeThread pipeToServer;
    private StreamPipeThread pipeToClient;
    private AddressMapping addressMapping = null;

    public TcpHandler(Socket socket, Socks5Proxy proxy, AddressMapping addressMapping) {
        this.socket = socket;
        this.proxy = proxy;
        this.addressMapping = addressMapping;
    }

    public void close() {
        try {
            ThreadManager.getInstance().remove(this);
            AddrMappingCache.remove(addressMapping.getLocalInfo());

            if (pipeToServer != null) {
                pipeToServer.close();
            }
            if (pipeToClient != null) {
                pipeToClient.close();
            }
            socket.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.debug("clien关闭");

    }

    @Override
    public void run() {
        Socket proxySocket = null;
        try {
            //连接到socks5发送connect请求
            proxySocket = new SocksSocket(proxy);
            InetSocketAddress remoteAddr = addressMapping.getRemote();
            //use remote dns
            if (proxy.useRemoteDns()) {
                String domain = DnsCache.get(remoteAddr.getAddress().getHostAddress());
                if (domain != null) {
                    remoteAddr = new InetSocketAddress(domain, remoteAddr.getPort());
                }
            }
            LOGGER.debug("连接到服务器：{}", remoteAddr);
            proxySocket.connect(remoteAddr, proxy.getTimeOut());

            pipeToServer = new StreamPipeThread(socket.getInputStream(), proxySocket.getOutputStream());
            pipeToClient = new StreamPipeThread(proxySocket.getInputStream(), socket.getOutputStream());

            pipeToServer.start();
            pipeToClient.start();

            while (!proxySocket.isClosed() && !socket.isClosed()) {
                Thread.sleep(200);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            try {
                proxySocket.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        this.close();
    }
}
