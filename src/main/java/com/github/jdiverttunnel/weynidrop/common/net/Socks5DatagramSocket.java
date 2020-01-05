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

package com.github.jdiverttunnel.weynidrop.common.net;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

public class Socks5DatagramSocket extends DatagramSocket {
    private static final byte IPV4 = 0x01;
    private static final byte DOMAIN_NAME = 0x03;
    private static final byte IPV6 = 0x04;
    private static final byte DEFAULT_0X00 = 0x00;

    private SocksSocket socket;


    public Socks5DatagramSocket(Socks5Proxy proxy) throws IOException {
        this(new InetSocketAddress(0), proxy);
    }

    protected Socks5DatagramSocket(DatagramSocketImpl impl, Socks5Proxy proxy) throws IOException {
        super(impl);
        initSocks5(proxy);
    }

    public Socks5DatagramSocket(SocketAddress bindaddr, Socks5Proxy proxy) throws IOException {
        super(bindaddr);
        initSocks5(proxy);
    }

    public Socks5DatagramSocket(int port, Socks5Proxy proxy) throws IOException {
        super(port);
        initSocks5(proxy);
    }

    public Socks5DatagramSocket(int port, InetAddress laddr, Socks5Proxy proxy) throws IOException {
        super(port, laddr);
        initSocks5(proxy);
    }

    private void initSocks5(Socks5Proxy proxy) throws IOException {
        //set local bind port
        socket = new SocksSocket(proxy,getLocalPort());
        //you can connect any correct destServer at here
        socket.connect(proxy.address());
        //connect to udp associate address
        this.connect(socket.getUdpAssociateAddress());
    }


    @Override
    public void close() {
        super.close();
        if (!socket.isClosed()) {
            //sync close socket
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            service.schedule(() -> {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                socket = null;
            }, 500, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * return socks5 status
     */
    public boolean isSocksClosed(){
        return socket.isClosed();
    }


    @Override
    public void send(DatagramPacket p) throws IOException {
        if (socket.isClosed()){
            throw new SocketException("Socks5 closed");
        }
        encodePacket(p);
        super.send(p);
    }


    @Override
    public synchronized void receive(DatagramPacket p) throws IOException {
        if (socket.isClosed()){
            throw new SocketException("Socks5 closed");
        }
        super.receive(p);
        decodePacket(p);
    }

    /**
     * socks 5 packet encode
     *
     * @param p
     */
    private void encodePacket(DatagramPacket p) throws IOException {
        InetSocketAddress dest = (InetSocketAddress) p.getSocketAddress();
        byte addrType;
        byte[] addrBytes;
        if (dest.isUnresolved()) {
            addrType = DOMAIN_NAME;
            addrBytes = new byte[1 + dest.getHostName().length()];
            addrBytes[0] = (byte) dest.getHostName().length();
            System.arraycopy(dest.getHostName().getBytes("ISO-8859-1"), 0, addrBytes, 1, addrBytes.length - 1);
        } else if (dest.getAddress() instanceof Inet4Address) {
            addrType = IPV4;
            addrBytes = dest.getAddress().getAddress();
        } else if (dest.getAddress() instanceof Inet6Address) {
            addrType = IPV6;
            addrBytes = dest.getAddress().getAddress();
        } else {
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(6 + addrBytes.length + p.getData().length);
        buffer.put(DEFAULT_0X00);
        buffer.put(DEFAULT_0X00);
        buffer.put(DEFAULT_0X00);
        buffer.put(addrType);
        buffer.put(addrBytes);
        buffer.put((byte) ((dest.getPort() >> 8) & 0xff));
        buffer.put((byte) ((dest.getPort() >> 0) & 0xff));
        buffer.put(p.getData());
        p.setData(buffer.array());
        //set to udp assoc address
        p.setSocketAddress(socket.getUdpAssociateAddress());
        buffer.clear();
    }

    /**
     * socks 5 packet decode
     *
     * @param p packet
     */
    private void decodePacket(DatagramPacket p) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(p.getData());
        if (buffer.get() != DEFAULT_0X00) {
            throw new SocketException("SOCKS version error");
        }
        if (buffer.get() != DEFAULT_0X00) {
            throw new SocketException("SOCKS version error");
        }
        if (buffer.get() != DEFAULT_0X00) {
            throw new SocketException("SOCKS fragment is not supported");
        }
        switch (buffer.get()) {
            case DOMAIN_NAME:
                int addrlen = buffer.get();
                buffer.position(buffer.position() + addrlen);
                break;
            case IPV4:
                buffer.position(buffer.position() + 4);
                break;
            case IPV6:
                buffer.position(buffer.position() + 16);
                break;
            default:
                throw new SocketException("SOCKS addr error");
        }
        byte[] data = new byte[buffer.limit() - buffer.position()];
        buffer.get(data);
        p.setData(data);
    }

}
