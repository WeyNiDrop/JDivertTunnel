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

import com.github.jdiverttunnel.weynidrop.common.net.Socks5Proxy;
import com.github.jdiverttunnel.weynidrop.common.net.Socks5DatagramSocket;
import com.github.jdiverttunnel.weynidrop.common.socks5.Socks5ProxyHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

public class UdpTunnelTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpTunnelTest.class);

    @Test
    public void sendData() throws IOException {
//        Socks5Config socks5Config = new Socks5Config("192.168.64.128",1080);
//        Proxy proxy = new Proxy(Proxy.Type.SOCKS,socks5Config.getServerAddr());

        DatagramSocket datagramSocket = new DatagramSocket();

        System.out.println(datagramSocket.getLocalAddress().getHostAddress());
        System.out.println(datagramSocket.getLocalPort());
//        DatagramSocket datagramSocket2 = new DatagramSocket(1111);
        String message = "hello this is udp client";
        //数据报包:类似于集装箱，用来存储所有的数据信息
        DatagramPacket dp = new DatagramPacket(
                message.getBytes(),   //数据都是已字节数据进行发送的，因此需要将数据进行转换
                message.length(), //发送数据的长度
                InetAddress.getByName("fe80::5d91:eb48:7d3c:bb98"),  //目标ip地址
                1112   //目标端口号
        );
        datagramSocket.send(dp);  //数据包通过码头DatagramSocket发送出去
    }
}
