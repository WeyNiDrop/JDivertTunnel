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

import com.github.ffalcinelli.jdivert.Packet;
import com.github.ffalcinelli.jdivert.exceptions.WinDivertException;
import com.github.ffalcinelli.jdivert.headers.Tcp;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.Arrays;

@Deprecated
public class WindivertPacketUtil {
    private WindivertPacketUtil(){}

    /**
     * repacket old packet to add the dest info,support TCP/UDP IPV4/IPV6 The new packet style : addrtype(4 0r 6) 1byte - addr 4/16byte - port 2byte - old data
     * 将目标主机和端口信息重打包进当前数据包 支持TCP/UDP IPV4/IPV6 重打包后数据格式: 地址类型(4或6) 1byte -地址 4/16byte -端口 2byte -原数据
     * @param packet 原数据包 old packet
     * @return new packet with dest info
     * @throws WinDivertException
     */
    @Deprecated
    public static Packet encodePacketWithDestInfo(Packet packet) {
        if (packet.getPayload() == null || packet.getPayload().length == 0) {
            return packet;
        }
        if (packet.isTcp()&&!packet.getTcp().is(Tcp.Flag.PSH)){
            return packet;
        }
        //头部包含的其他协议所占字节长度
        int otherLen = 20;
        //取出头数据和数据域
        byte[] header = Arrays.copyOf(packet.getRaw(),packet.getHeadersLength());
        byte[] data = packet.getPayload();
        if (packet.isUdp()){
            otherLen = header.length - packet.getUdp().getHeaderLength();
        }else if(packet.isTcp()){
            otherLen = header.length - packet.getTcp().getHeaderLength();
        }
        //获取目标地址及目标端口 封包格式 地址类型1byte-地址4/16byte-端口2byte-原数据
        byte type = 0x004;
        byte[] addr;
        if (packet.isIpv6()) {
            type = 0x006;
            addr = packet.getIpv6().getDstAddr().getAddress();
        } else {
            addr = packet.getIpv4().getDstAddr().getAddress();
        }

        byte[] port = ByteUtils.intTo2ByteArray(packet.getDstPort());
        //构建新的数据byte[]
        byte[] nowData = new byte[1 + addr.length + port.length + data.length];
        nowData[0] = type;
        System.arraycopy(addr, 0, nowData, 1, addr.length);
        System.arraycopy(port, 0, nowData, 1 + addr.length, port.length);
        System.arraycopy(data, 0, nowData, 1 + addr.length + port.length, data.length);

        byte[] raw = new byte[header.length + nowData.length];
        System.arraycopy(header, 0, raw, 0, header.length);
        System.arraycopy(nowData, 0, raw, header.length, nowData.length);
        //取得实际包长度，长度为raw取掉ip协议后长度
        short lenth = (short)(raw.length - otherLen);
        byte[] lenBytes = ByteUtils.intTo2ByteArray(lenth);
        if (packet.isUdp()) {
            //重设udp长度
            raw[otherLen + 4] = lenBytes[0];
            raw[otherLen + 5] = lenBytes[1];
        }
        //重构数据包
        packet = new Packet(raw, packet.getWinDivertAddress());
        //重设ip协议中的包长度
        if (packet.isIpv6()){
            packet.getIpv6().setPayloadLength((short)nowData.length);
        }else if(packet.isIpv4()){
            packet.getIpv4().setTotalLength(raw.length);
        }
        if (packet.isUdp()) {
            packet.getUdp().setLength(lenth);
        }
        return packet;
    }

    public static Packet encodeUdpPacketWithDestInfo(Packet packet) throws WinDivertException {
        if (!packet.isUdp()){
            return packet;
        }

        //取出头数据和数据域
        byte[] header = Arrays.copyOf(packet.getRaw(),packet.getHeadersLength());
        byte[] data = packet.getPayload();
        //ipv4 or ipv6 headLen
        int otherLen = header.length - packet.getUdp().getHeaderLength();

        //获取目标地址及目标端口 封包格式 地址类型1byte-地址4/16byte-端口2byte-原数据
        byte type = 0x004;
        byte[] addr;
        if (packet.isIpv6()) {
            type = 0x006;
            addr = packet.getIpv6().getDstAddr().getAddress();
        } else {
            addr = packet.getIpv4().getDstAddr().getAddress();
        }

        byte[] port = ByteUtils.intTo2ByteArray(packet.getDstPort());
        //构建新的数据byte[]
        byte[] nowData = new byte[1 + addr.length + port.length + data.length];
        nowData[0] = type;
        System.arraycopy(addr, 0, nowData, 1, addr.length);
        System.arraycopy(port, 0, nowData, 1 + addr.length, port.length);
        System.arraycopy(data, 0, nowData, 1 + addr.length + port.length, data.length);

        byte[] raw = new byte[header.length + nowData.length];
        System.arraycopy(header, 0, raw, 0, header.length);
        System.arraycopy(nowData, 0, raw, header.length, nowData.length);
        //取得实际包长度，长度为raw取掉ip协议后长度
        short lenth = (short)(raw.length - otherLen);
        byte[] lenBytes = ByteUtils.intTo2ByteArray(lenth);
        //重设udp长度
        raw[otherLen + 4] = lenBytes[0];
        raw[otherLen + 5] = lenBytes[1];
        //重构数据包
        packet = new Packet(raw, packet.getWinDivertAddress());
        //重设ip协议中的包长度
        if (packet.isIpv6()){
            packet.getIpv6().setPayloadLength((short)nowData.length);
        }else if(packet.isIpv4()){
            packet.getIpv4().setTotalLength(raw.length);
        }
        packet.recalculateChecksum();
        return packet;
    }

    public static Packet decodeUdpPacketWithDestInfo(Packet packet) throws UnknownHostException, WinDivertException {
        if (!packet.isUdp()){
            return packet;
        }
        //取出头数据和数据域
        byte[] header = Arrays.copyOf(packet.getRaw(),packet.getHeadersLength());
        byte[] data = packet.getPayload();
        //ipv4 or ipv6 headLen
        int otherLen = header.length - packet.getUdp().getHeaderLength();
        byte[] srcIp = null;
        //获取封装的ip
        if (data.length>0){
            switch (data[0]){
                case 4:
                    srcIp = new byte[4];
                    System.arraycopy(data, 1, srcIp, 0, srcIp.length);
                    break;
                case 6:
                    srcIp = new byte[16];
                    System.arraycopy(data, 1, srcIp, 0, srcIp.length);
                    break;
                default:
                    return packet;
            }
        }
        //获取封装的端口
        byte[] srcPort = new byte[2];
        int udpEncodeLen = 1+ srcIp.length+srcPort.length;
        byte[] nowData = new byte[data.length-udpEncodeLen];
        //还原数据
        System.arraycopy(data, udpEncodeLen, nowData, 0, nowData.length);

        byte[] raw = new byte[header.length + nowData.length];
        System.arraycopy(header, 0, raw, 0, header.length);
        System.arraycopy(nowData, 0, raw, header.length, nowData.length);
        //取得实际包长度，长度为raw取掉ip协议后长度
        short lenth = (short)(raw.length - otherLen);
        byte[] lenBytes = ByteUtils.intTo2ByteArray(lenth);
        if (packet.isUdp()) {
            //重设udp长度
            raw[otherLen + 4] = lenBytes[0];
            raw[otherLen + 5] = lenBytes[1];
        }
        //重构数据包
        packet = new Packet(raw, packet.getWinDivertAddress());
        //重设ip协议中的包长度
        if (packet.isIpv6()){
            packet.getIpv6().setSrcAddr((Inet6Address) Inet6Address.getByAddress(srcIp));
            packet.getIpv6().setPayloadLength((short)nowData.length);
        }else if(packet.isIpv4()){
            packet.getIpv4().setSrcAddr((Inet4Address) Inet4Address.getByAddress(srcIp));
            packet.getIpv4().setTotalLength(raw.length);
        }
        packet.getUdp().setLength(lenth);
        packet.getUdp().setSrcPort(ByteUtils.byte2ToInteger(srcPort));
        packet.recalculateChecksum();
        return packet;
    }

}
