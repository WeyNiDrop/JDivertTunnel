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

package com.github.jdiverttunnel.weynidrop.socks5client.udp;

import com.github.jdiverttunnel.weynidrop.common.cache.AddrMappingCache;
import com.github.jdiverttunnel.weynidrop.common.cache.UdpConnectorMappingCache;
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;
import com.github.jdiverttunnel.weynidrop.common.net.Socks5Proxy;
import com.github.jdiverttunnel.weynidrop.common.net.Socks5DatagramSocket;
import com.github.jdiverttunnel.weynidrop.common.socks5.listener.Socks5CloseListener;
import com.github.jdiverttunnel.weynidrop.common.thread.ThreadManager;
import com.github.jdiverttunnel.weynidrop.common.cache.AddrMappingCache;
import com.github.jdiverttunnel.weynidrop.common.cache.UdpConnectorMappingCache;
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;
import com.github.jdiverttunnel.weynidrop.common.net.Socks5DatagramSocket;
import com.github.jdiverttunnel.weynidrop.common.net.Socks5Proxy;
import com.github.jdiverttunnel.weynidrop.common.thread.ThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;


public class Socks5UdpClient implements Runnable,Socks5CloseListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Socks5UdpClient.class);

    private Socks5Proxy proxy;
    private AddressMapping addressMapping;
    private DatagramSocket localProxyServer;
    private List<DatagramPacket> msgList;


    /**
     * 用于收发信息的udp datagramSocket
     */
    private Socks5DatagramSocket datagramSocket;

    private boolean canSend = false;
    private UdpPortReleaseHandler portReleaseHandler;

    public Socks5UdpClient(Socks5Proxy proxy, AddressMapping addressMapping, DatagramSocket localProxyServer) {
        this.proxy = proxy;
        this.addressMapping = addressMapping;
        this.localProxyServer = localProxyServer;
        msgList = new ArrayList<>(4);
    }


    public void close() {
        portReleaseHandler.close();
        datagramSocket.close();
        //移除路由
        UdpConnectorMappingCache.remove(addressMapping.getLocalInfo());
        AddrMappingCache.remove(addressMapping.getLocalInfo());

        Thread.currentThread().interrupt();
        ThreadManager.getInstance().remove(this);
    }

    @Override
    public void run() {
        try {
            datagramSocket = new Socks5DatagramSocket(proxy);
            //添加线程监听本地端口释放事件 listen local port release event
            portReleaseHandler = new UdpPortReleaseHandler((InetSocketAddress) datagramSocket.getLocalSocketAddress(), this);
            ThreadManager.getInstance().execute(portReleaseHandler);
            canSend = true;
            sendPacket();
            byte[] buffer = new byte[1024 * 2];
            //接受包并转发
            while (!datagramSocket.isSocksClosed() && !Thread.currentThread().isInterrupted()) {
                //receive packet from server
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);
                packet.setSocketAddress(addressMapping.getLocal());
                //replay packet
                localProxyServer.send(packet);
            }
        } catch (Exception e) {
            LOGGER.error("Socks5 Udp Client Error: ", e);
        }
        this.close();
    }

    //发送包到服务器
    private synchronized void sendPacket() {
        while (!msgList.isEmpty() && canSend) {
            DatagramPacket dp = msgList.remove(0);
            //设置正确的发送地址
            dp.setSocketAddress(addressMapping.getRemote());
            try {
                datagramSocket.send(dp);
            } catch (IOException e) {
                LOGGER.error("Socks5 Udp send fail: ", e);
            }
        }
    }

    public void send(DatagramPacket packet) {
        msgList.add(packet);
        sendPacket();
    }

    @Override
    public void onSocks5Close() {
        this.close();
    }
}
