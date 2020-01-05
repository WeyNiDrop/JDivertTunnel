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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

/**
 * 支持udp的proxy
 */
public class EnUdpProxy extends Proxy {
    private SocketAddress local = null;
    private String userName = null;
    private String passWord = null;

    /**
     * Class extends java.net.Proxy,support udp associate in type socks5
     */
    public EnUdpProxy(Type type, SocketAddress sa) {
        this(type, sa,-1,null,null);
    }

    public EnUdpProxy(Type type, SocketAddress sa, int localPort) {
        this(type, sa,localPort,null,null);
    }

    public EnUdpProxy(Type type, SocketAddress sa, String userName, String passWord) {
        this(type, sa,-1,userName,passWord);
    }

    public EnUdpProxy(Type type, SocketAddress sa, int localPort, String userName, String passWord) {
        super(type, sa);
        this.userName = userName;
        this.passWord = passWord;
        if (localPort > -1){
            this.local = new InetSocketAddress(localPort);
        }
    }

    /**
     * @param local the local to set
     */
    public void setLocal(SocketAddress local) {
        this.local = local;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the passWord
     */
    public String getPassWord() {
        return passWord;
    }

    /**
     * @return the local
     */
    public SocketAddress localAddress() {
        return local;
    }

    @Override
    public String toString() {
        return super.toString() + "@" + local;
    }
}
