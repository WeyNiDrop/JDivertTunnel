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

package com.github.jdiverttunnel.weynidrop;

import com.github.jdiverttunnel.weynidrop.common.constants.IpConstants;
import com.github.jdiverttunnel.weynidrop.common.net.Socks5Proxy;
import com.github.jdiverttunnel.weynidrop.common.thread.ThreadManager;
import com.github.jdiverttunnel.weynidrop.proxyserver.tcp.ProxyTcpToSocks5Server;
import com.github.jdiverttunnel.weynidrop.proxyserver.udp.ProxyUdpToSocks5Server;
import com.github.jdiverttunnel.weynidrop.tunnel.WinDivertThread;
import com.github.jdiverttunnel.weynidrop.tunnel.filter.LocalProxyServerFilter;
import com.github.jdiverttunnel.weynidrop.tunnel.filter.OutboundChineseIpFilter;
import com.github.jdiverttunnel.weynidrop.tunnel.filter.OutboundNotChineseIpFilter;
import com.github.jdiverttunnel.weynidrop.tunnel.filter.SocketPacketDstFilter;
import com.github.jdiverttunnel.weynidrop.tunnel.handler.LogHandler;
import com.github.jdiverttunnel.weynidrop.tunnel.processor.ClientPacketProcessor;
import com.github.jdiverttunnel.weynidrop.tunnel.processor.LocalProxyServerPacketProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 〈注意：socks5代理服务器终端不可以在本地，否则会导致数据包死循环，流程: request->windivert->proxyserver->sock5server(if s5server at local ->windivert....dead)->dstserver〉
 */
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private int PROXY_SERVER_PORT;
    private Socks5Proxy proxy = null;

    @SuppressWarnings("all")
    public void run(String[] args) {
        //初始化配置参数
        initConfig();
        if (proxy == null || IpConstants.LOCAL_IP_ALL == null) {
            LOGGER.error("初始化失败");
            return;
        }
        startLocalSever(PROXY_SERVER_PORT, proxy);

        //需要代理的目标IP和端口，端口为0则代理目标IP所有端口
        List<InetSocketAddress> proxyList = new ArrayList<>();
        proxyList.add(new InetSocketAddress("github.com", 0));

        startLocalServerTunnel(PROXY_SERVER_PORT, IpConstants.LOCAL_IP_ALL);

        startClientTunnel(PROXY_SERVER_PORT,proxyList);

//        //启动中转非中国大陆流量
//        startClientTunnelNotChina(PROXY_SERVER_PORT, (InetSocketAddress) proxy.address());
//        //启动只中转中国大陆流量
//        startClientTunnelOnlyChina(PROXY_SERVER_PORT, (InetSocketAddress) proxy.address());
        while (!ThreadManager.getInstance().isShutdown()) {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    /**
     * 停止
     */
    public void stop() {
        ThreadManager.getInstance().shutDown();
    }


    /**
     * 启动本地中转服务器回传数据重定向
     *
     * @param proxyPort local proxy port
     * @param localIps  local ip list
     */
    private void startLocalServerTunnel(int proxyPort, Map<String, List<String>> localIps) {
        //处理本地代理回传包filter
        LocalProxyServerFilter localProxyServerFilter = new LocalProxyServerFilter(proxyPort, localIps);
        //启动本地代理数据包重定向
        WinDivertThread localServerDivert = new WinDivertThread(localProxyServerFilter.getFilter(), new LocalProxyServerPacketProcessor(localProxyServerFilter));
        localServerDivert.appendHandler(new LogHandler());
        ThreadManager.getInstance().execute(localServerDivert);
    }

    /**
     * 启动本机数据包请求重定向
     *
     * @param proxyList 需要重定向的ip或host集合 port为-1时重定向所有端口，否则只重定向指定端口
     */
    private void startClientTunnel(int proxyPort, List<InetSocketAddress> proxyList) {
        proxyList.forEach(address -> {
            try {
                SocketPacketDstFilter filter = new SocketPacketDstFilter(address);
                WinDivertThread clientTunnelThread = new WinDivertThread(filter.getFilter(), new ClientPacketProcessor(filter, proxyPort));
                clientTunnelThread.appendHandler(new LogHandler());
                ThreadManager.getInstance().execute(clientTunnelThread);
            } catch (UnknownHostException e) {
                LOGGER.error("解析域名失败：", e);
            }
        });
    }

    /**
     * 启动本机数据包请求重定向,只重定向中国大陆ip,暂不支持远程dns解析
     */
    private void startClientTunnelOnlyChina(int proxyPort, InetSocketAddress proxyAddress) {
        OutboundChineseIpFilter filter = new OutboundChineseIpFilter(proxyAddress);
        WinDivertThread clientTunnelThread = new WinDivertThread(filter.getFilter(), new ClientPacketProcessor(filter, proxyPort));
        ThreadManager.getInstance().execute(clientTunnelThread);
    }

    /**
     * 启动本机数据包请求重定向,只重定向非中国大陆ip,暂不支持远程dns解析
     */
    private void startClientTunnelNotChina(int proxyPort, InetSocketAddress proxyAddress) {
        OutboundNotChineseIpFilter filter = new OutboundNotChineseIpFilter(proxyAddress);
        WinDivertThread clientTunnelThread = new WinDivertThread(filter.getFilter(), new ClientPacketProcessor(filter, proxyPort));
        ThreadManager.getInstance().execute(clientTunnelThread);
    }

    /**
     * 启动本地中转服务器
     */
    private void startLocalSever(int port, Socks5Proxy proxy) {
        //启动tcp中转服务器
        ProxyTcpToSocks5Server tcpToSocks5Server = new ProxyTcpToSocks5Server(port, proxy);
        ThreadManager.getInstance().execute(tcpToSocks5Server);
        //启动udp中转服务器
        ProxyUdpToSocks5Server udpToSocks5Server = new ProxyUdpToSocks5Server(port, proxy);
        ThreadManager.getInstance().execute(udpToSocks5Server);
    }


    /**
     * 加载配置文件的配置
     */
    private void initConfig() {
        try {
            Properties properties = new Properties();
            // 使用ClassLoader加载properties配置文件生成对应的输入流
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
            // 使用properties对象加载输入流
            properties.load(in);
            PROXY_SERVER_PORT = Integer.parseInt((String) properties.getOrDefault("proxyPort", "8960"));

            String server = properties.getProperty("socks5.server");
            int serverPort = Integer.parseInt(properties.getProperty("socks5.serverPort"));
            String timeOut = properties.getProperty("socks5.timeOut");
            String username = properties.getProperty("socks5.username");
            String password = properties.getProperty("socks5.password");
            boolean userRemoteDns = Boolean.parseBoolean(properties.getProperty("socks5.useRemoteDns"));
            proxy = new Socks5Proxy(new InetSocketAddress(server, serverPort), username, password);
            if (timeOut != null && !timeOut.isEmpty()) {
                proxy.setTimeOut(Integer.parseInt(timeOut));
            }
            //是否使用远程dns解析
            proxy.useRemoteDns(userRemoteDns);
        } catch (Exception e) {
            LOGGER.error("加载配置失败：", e);
        }
    }


    public static void main(String[] args) {
        Application application = new Application();
        application.run(args);
    }
}
