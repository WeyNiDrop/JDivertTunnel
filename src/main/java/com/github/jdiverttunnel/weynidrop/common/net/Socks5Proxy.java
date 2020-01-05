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
import java.net.Proxy;
import java.net.SocketAddress;

public class Socks5Proxy extends Proxy {
    private String userName = null;
    private String passWord = null;
    private int timeOut = 10000;
    private boolean useRemoteDns = false;

    /**
     * Class extends java.net.Proxy,support udp associate in type socks5
     */
    public Socks5Proxy(SocketAddress sa) {
        this(sa, null, null);
    }

    public Socks5Proxy(SocketAddress sa, String userName, String passWord) {
        super(Type.SOCKS, sa);
        this.userName = userName;
        this.passWord = passWord;
    }


    /**
     * @return the useRemoteDns
     */
    public boolean useRemoteDns() {
        return useRemoteDns;
    }

    /**
     * @param useRemoteDns the useRemoteDns to set
     */
    public void useRemoteDns(boolean useRemoteDns) {
        this.useRemoteDns = useRemoteDns;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
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
     * copy socks5proxy without local info
     * @return
     */
    public Socks5Proxy copy() {
        return new Socks5Proxy(this.address(), userName, passWord);
    }
}
