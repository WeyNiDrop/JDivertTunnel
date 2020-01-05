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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * This class extends client Socket, Enable support socks5 associate udp proxy
 */
public
class SocksSocket extends Socket implements SocksConsts {
    private Socks5Proxy socks5Proxy = null;
    private InetSocketAddress udpAssociateAddress = null;
    private InetSocketAddress destAddress = null;
    private int udpBindPort;

    public SocksSocket(Socks5Proxy socks5Proxy) {
        this(socks5Proxy, 0);
    }

    public SocksSocket(Socks5Proxy socks5Proxy, int udpBindPort) {
        this.socks5Proxy = socks5Proxy;
        this.udpBindPort = udpBindPort;
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        socks5Connect(endpoint, socks5Proxy.getTimeOut());
    }

    /**
     * @return the destAddress
     */
    public InetSocketAddress getDestAddress() {
        return destAddress;
    }

    /**
     * @param destAddress the destAddress to set
     */
    public void setDestAddress(InetSocketAddress destAddress) {
        this.destAddress = destAddress;
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        socks5Connect(endpoint, 10000);
    }

    /**
     * 连接到远程服务器
     * @param endpoint 远程服务器地址
     * @param timeout 连接超时时间
     * @throws IOException e
     */
    private void socks5Connect(SocketAddress endpoint, int timeout) throws IOException {
        if (socks5Proxy == null) {
            throw new IOException("Invalid Proxy");
        }
        if (udpBindPort > 65535 || udpBindPort < 0) {
            throw new IOException("Invalid bind port");
        }
        this.destAddress = (InetSocketAddress) endpoint;
        super.connect(socks5Proxy.address(), timeout);
        BufferedOutputStream out = new BufferedOutputStream(this.getOutputStream(), 512);
        InputStream in = this.getInputStream();

        out.write(PROTO_VERS);
        out.write(2);
        out.write(NO_AUTH);
        out.write(USER_PASSW);
        out.flush();
        byte[] data = new byte[2];
        int i = in.read(data);
        if (i != 2 || ((int) data[0]) != PROTO_VERS) {
            throw new SocketException("Socks5 Version Invalid");
        }
        if (((int) data[1]) == NO_METHODS) {
            throw new SocketException("SOCKS : No acceptable methods");
        }
        if (!authenticate(data[1], in, out)) {
            throw new SocketException("SOCKS : authentication failed");
        }
        //send command
        out.write(PROTO_VERS);
        if (udpBindPort == 0) {
            //tcp connect
            out.write(CONNECT);
            out.write(0);
            /*Domain/ipv4/ipv6*/
            if (socks5Proxy.useRemoteDns() || destAddress.isUnresolved()) {
                out.write(DOMAIN_NAME);
                System.out.println("connect to doamin:"+destAddress.getHostName());
                out.write(destAddress.getHostName().length());
                try {
                    out.write(destAddress.getHostName().getBytes("ISO-8859-1"));
                } catch (java.io.UnsupportedEncodingException uee) {
                    assert false;
                }
                out.write((destAddress.getPort() >> 8) & 0xff);
                out.write((destAddress.getPort() >> 0) & 0xff);
            } else if (destAddress.getAddress() instanceof Inet6Address) {
                out.write(IPV6);
                out.write(destAddress.getAddress().getAddress());
            } else {
                out.write(IPV4);
                out.write(destAddress.getAddress().getAddress());
            }
            out.write((destAddress.getPort() >> 8) & 0xff);
            out.write((destAddress.getPort() >> 0) & 0xff);
        } else {
            //udp associate
            InetSocketAddress localBandAddress = new InetSocketAddress(udpBindPort);
            out.write(UDP_ASSOC);
            out.write(0);
            out.write(IPV4);
            out.write(localBandAddress.getAddress().getAddress());
            out.write((localBandAddress.getPort() >> 8) & 0xff);
            out.write((localBandAddress.getPort() >> 0) & 0xff);
        }
        out.flush();
        data = new byte[4];
        i = in.read(data);
        if (i != 4) {
            throw new SocketException("Reply from SOCKS server has bad length");
        }
        SocketException ex = null;
        byte[] addr = null;
        switch (data[1]) {
            case REQUEST_OK:
                // success!
                switch (data[3]) {
                    case IPV4:
                        addr = new byte[4];
                        i = in.read(addr);
                        if (i != 4) {
                            throw new SocketException("Reply from SOCKS server badly formatted");
                        }
                        data = new byte[2];
                        i = in.read(data);
                        if (i != 2) {
                            throw new SocketException("Reply from SOCKS server badly formatted");
                        }
                        break;
                    case DOMAIN_NAME:
                        int len = in.read();
                        byte[] host = new byte[len];
                        i = in.read(host);
                        if (i != len) {
                            throw new SocketException("Reply from SOCKS server badly formatted");
                        }
                        data = new byte[2];
                        i = in.read(data);
                        if (i != 2) {
                            throw new SocketException("Reply from SOCKS server badly formatted");
                        }
                        break;
                    case IPV6:
                        addr = new byte[16];
                        i = in.read(addr);
                        if (i != 16) {
                            throw new SocketException("Reply from SOCKS server badly formatted");
                        }
                        data = new byte[2];
                        i = in.read(data);
                        if (i != 2) {
                            throw new SocketException("Reply from SOCKS server badly formatted");
                        }
                        break;
                    default:
                        ex = new SocketException("Reply from SOCKS server contains wrong code");
                        break;
                }
                if (udpBindPort > 0) {
                    udpAssociateAddress = new InetSocketAddress(InetAddress.getByAddress(addr), ((data[0] & 0xff) << 8 | (data[1] & 0xff)));
                }
                break;
            case GENERAL_FAILURE:
                ex = new SocketException("SOCKS server general failure");
                break;
            case NOT_ALLOWED:
                ex = new SocketException("SOCKS: Connection not allowed by ruleset");
                break;
            case NET_UNREACHABLE:
                ex = new SocketException("SOCKS: Network unreachable");
                break;
            case HOST_UNREACHABLE:
                ex = new SocketException("SOCKS: Host unreachable");
                break;
            case CONN_REFUSED:
                ex = new SocketException("SOCKS: Connection refused");
                break;
            case TTL_EXPIRED:
                ex = new SocketException("SOCKS: TTL expired");
                break;
            case CMD_NOT_SUPPORTED:
                ex = new SocketException("SOCKS: Command not supported");
                break;
            case ADDR_TYPE_NOT_SUP:
                ex = new SocketException("SOCKS: address type not supported");
                break;
        }
        if (ex != null) {
            in.close();
            out.close();
            throw ex;
        }
    }

    private boolean authenticate(byte method, InputStream in,
                                 BufferedOutputStream out) throws IOException {
        // No Authentication required. We're done then!
        if (method == NO_AUTH) {
            return true;
        }
        /*
         * use proxy username and password
         */
        if (method == USER_PASSW) {
            String userName = socks5Proxy.getUserName();
            String password = socks5Proxy.getPassWord();

            if (userName == null) {
                return false;
            }
            out.write(1);
            out.write(userName.length());
            try {
                out.write(userName.getBytes("ISO-8859-1"));
            } catch (java.io.UnsupportedEncodingException uee) {
                assert false;
            }
            if (password != null) {
                out.write(password.length());
                try {
                    out.write(password.getBytes("ISO-8859-1"));
                } catch (java.io.UnsupportedEncodingException uee) {
                    assert false;
                }
            } else {
                out.write(0);
            }
            out.flush();
            byte[] data = new byte[2];
            int i = in.read(data);
            if (i != 2 || data[1] != 0) {
                /* RFC 1929 specifies that the connection MUST be closed if
                   authentication fails */
                out.close();
                in.close();
                return false;
            }
            /* Authentication succeeded */
            return true;
        }
        return false;
    }

    /**
     * get socks5 udp AssociateAddress
     *
     * @return SocketAddress
     */
    public SocketAddress getUdpAssociateAddress() {
        return this.udpAssociateAddress;
    }
}

