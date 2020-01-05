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
import com.github.ffalcinelli.jdivert.Packet;
import com.github.ffalcinelli.jdivert.WinDivert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * 〈一句话功能简述〉
 * 〈功能详细描述〉
 *
 * @author 88469401
 * @date 2019/11/28
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class TunnelTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TunnelTest.class);
    @Test
    public void getAllIp() throws UnknownHostException {
        String hostname = "Unknown";
        try
        {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
            for (InetAddress inetAddress : InetAddress.getAllByName(hostname)) {
                String ipAddr = inetAddress.getHostAddress();
                if (inetAddress instanceof Inet4Address) {
                    System.out.println("v4:" + ipAddr);
                } else if (inetAddress instanceof Inet6Address) {
                    int index = ipAddr.indexOf('%');
                    if (index > 0) {
                        ipAddr = ipAddr.substring(0, index);
                    }
                    System.out.println("v6:" + ipAddr);
                }
            }
        }
        catch (UnknownHostException ex)
        {
            System.out.println("Hostname can not be resolved");
        }
        System.out.println("The computer name is: " + hostname);
    }

    @Test
    public void tunnel(){
        Boolean isRunning = true;
        WinDivert winDivert = new WinDivert("tcp and (tcp.SrcPort==8080 or tcp.DstPort == 1080)");
        try {
            winDivert.open();
            while (isRunning) {
                // read a single packet
                Packet packet = winDivert.recv();
                System.out.println(packet);
                if (packet.getDstPort() == 1080){
                    packet.setDstPort(8080);
                }else{
                    packet.setSrcPort(1080);
                }
                packet.recalculateChecksum();
                winDivert.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void socks5CLientTest() throws InterruptedException, IOException {
//        Socks5Config socks5Config = new Socks5Config("127.0.0.1",1080);
//        SimpleAddress address = new SimpleAddress("127.0.0.1",1111);
//        Socks5TcpClient client = new Socks5TcpClient(socks5Config,address);
////        client.connect();
//        client.send("test".getBytes());
//        client.outFlush();
//        Thread.sleep(5000);
//        System.out.println("over");
//        client.close();
//        client = null;
    }

    @Test
    public void testThread(){
        System.out.println("main函数开始执行");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                System.out.println("===task start===");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("===task finish===");
                return 3;
            }
        }, executor);
        future.thenAccept(e -> System.out.println(e));
        System.out.println("main函数执行结束");
    }

    @Test
    public void proxyTest() throws InterruptedException {
        InetSocketAddress socketAddress = new InetSocketAddress("192.168.64.128",1080);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS,socketAddress);

        int len = 0;
        byte[] buffer = new byte[1024];
        for (int i = 0;i<10;i++) {
            long old = System.currentTimeMillis();

            Socket socket = new Socket(proxy);
//            Socket socket = new Socket();

            SocketAddress dst = new InetSocketAddress("192.168.64.128", 1111);
            try {
                socket.connect(dst);
                socket.getOutputStream().write("test".getBytes());
                socket.getOutputStream().flush();

                while ((len = socket.getInputStream().read(buffer,0,buffer.length))!=-1){
                    String msg = new String(buffer);
//                    System.out.println("get msg from server：" + msg);
                }
                System.out.println(System.currentTimeMillis() - old);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Thread.sleep(100);
        }
    }
}
