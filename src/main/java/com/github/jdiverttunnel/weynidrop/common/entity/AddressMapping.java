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

package com.github.jdiverttunnel.weynidrop.common.entity;

import com.github.jdiverttunnel.weynidrop.common.enums.PacketTypeEnum;

import java.net.InetSocketAddress;

public class AddressMapping {
    private InetSocketAddress local;
    private InetSocketAddress remote;
    private PacketTypeEnum type;


    /**
     * @return the type
     */
    public PacketTypeEnum getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(PacketTypeEnum type) {
        this.type = type;
    }

    /**
     * @return the local
     */
    public InetSocketAddress getLocal() {
        return local;
    }

    /**
     * @param local the local to set
     */
    public void setLocal(InetSocketAddress local) {
        this.local = local;
    }

    /**
     * @return the remote
     */
    public InetSocketAddress getRemote() {
        return remote;
    }

    /**
     * @param remote the remote to set
     */
    public void setRemote(InetSocketAddress remote) {
        this.remote = remote;
    }

    public AddressMapping(InetSocketAddress local, InetSocketAddress remote,PacketTypeEnum type) {
        this.local = local;
        this.remote = remote;
        this.type = type;
    }

    public AddressMapping(String localIp, int localPort,String remoteIp, int remotePort,PacketTypeEnum type) {
        this.local = new InetSocketAddress(localIp, localPort);
        this.remote = new InetSocketAddress(remoteIp, remotePort);
        this.type = type;
    }

    public AddressMapping(String localIp, int localPort,PacketTypeEnum type) {
        this.local = new InetSocketAddress(localIp, localPort);
        this.remote = null;
        this.type = type;
    }

    public String getLocalInfo(){
        return getLocalInfo(local,type);
    }

    public static String getLocalInfo(InetSocketAddress address,PacketTypeEnum type){
        return address.toString() + type;
    }

    @Override
    public String toString() {
        return "AddressMapping{" +
                "local=" + local.toString() +
                ", remote=" + remote.toString() +
                '}';
    }
}
