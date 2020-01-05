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

package com.github.jdiverttunnel.weynidrop.common.utils;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class SocketUtils {
    private SocketUtils(){}

    /**
     * 判断端口是否被占用
     * @param localAddr
     * @return
     */
    public static boolean udpPortIsRelease(InetSocketAddress localAddr){
        try {
            DatagramSocket datagramSocket = new DatagramSocket(localAddr);
            datagramSocket.close();
            return true;
        } catch (Exception e) {
        }
        return false;
    }

}
