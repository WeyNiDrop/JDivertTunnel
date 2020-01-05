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

@Deprecated
public class ByteUtils {
    /**
     * byte数组转int类型的对象 4字节
     *
     * @param bytes
     * @return
     */
    public static int byte4ToInt(Byte[] bytes) {
        return (bytes[0] & 0xff) << 24 | (bytes[1] & 0xff) << 16
                | (bytes[2] & 0xff) << 8 | (bytes[3] & 0xff);
    }

    /**
     * 2个字节 转化成int
     *
     */
    public static int byte2ToInteger(byte[] bs) {
        return ((bs[0] & 0xff) << 8 | (bs[1] & 0xff));
    }

    //单个byte转int
    public static int byteToInt(byte b) {
        int i = b;
        if (i < 0) {
            i += 256;
        }
        return i;
    }

    /**
     * 获取端口
     * @param bs
     * @return
     */
    public int portToBytes(byte[] bs) {
        return byte2ToInteger(bs);
    }

    /**
     * 端口转byte[2]数组
     * @param port
     * @return
     */
    public byte[] portToBytes(int port){
        return intTo2ByteArray(port);
    }


    /**
     * int转byte数组 4字节
     *
     * @param num
     * @return byte[]
     */
    public byte[] intTo4ByteArray(int num) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((num >> 24) & 0xff);
        bytes[1] = (byte) ((num >> 16) & 0xff);
        bytes[2] = (byte) ((num >> 8) & 0xff);
        bytes[3] = (byte) (num & 0xff);
        return bytes;
    }

    /**
     * 单个byte转int
     * @param b
     * @return
     */
    public static int byteToInteger(byte b){
        return 0xff & b;
    }


    /**
     * int 转2位byte
     *
     * @param i
     * @return byte[]
     */
    public static byte[] intTo2ByteArray(int i) {
        byte[] result = new byte[2];
        result[0] = (byte) ((i >> 8));
        result[1] = (byte) (i & 0xff);
        return result;
    }

}
