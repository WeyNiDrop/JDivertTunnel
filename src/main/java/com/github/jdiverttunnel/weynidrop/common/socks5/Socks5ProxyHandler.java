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

package com.github.jdiverttunnel.weynidrop.common.socks5;

import com.github.jdiverttunnel.weynidrop.common.socks5.listener.Socks5ProxySuccessListener;
import com.github.jdiverttunnel.weynidrop.common.socks5.listener.Socks5ProxySuccessListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.*;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.util.NetUtil;
import io.netty.util.internal.StringUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;


/**
 * 按照netty自带的socks5handler编写的 handler 添加udp支持
 */
@Deprecated
public final class Socks5ProxyHandler extends ProxyHandler {
    private static final String PROTOCOL = "socks5";
    private static final String AUTH_PASSWORD = "password";
    static final int LOCAL_PORT_TCP_CONNECT = -1;
    static final String DST_HOST_0X00 = new String(new byte[]{0x00});
    /**
     * A string that signifies 'no authentication' or 'anonymous'.
     */
    static final String MY_AUTH_NONE = "none";

    private static final Socks5InitialRequest INIT_REQUEST_NO_AUTH =
            new DefaultSocks5InitialRequest(Collections.singletonList(Socks5AuthMethod.NO_AUTH));

    private static final Socks5InitialRequest INIT_REQUEST_PASSWORD =
            new DefaultSocks5InitialRequest(Arrays.asList(Socks5AuthMethod.NO_AUTH, Socks5AuthMethod.PASSWORD));

    private Socks5ProxySuccessListener socks5ProxySuccessListener;

    private final String username;
    private final String password;
    /**
     * udp client bind port
     */
    private final int localUdpPort;

    private String decoderName;
    private String encoderName;

    public Socks5ProxyHandler(SocketAddress proxyAddress, int localUdpPort) {
        this(proxyAddress,localUdpPort, null, null);
    }

    public Socks5ProxyHandler(SocketAddress proxyAddress) {
        this(proxyAddress, LOCAL_PORT_TCP_CONNECT, null, null);
    }

    public Socks5ProxyHandler(SocketAddress proxyAddress, String username, String password) {
        this(proxyAddress, LOCAL_PORT_TCP_CONNECT, username, password);
    }

    public Socks5ProxyHandler(SocketAddress proxyAddress, int localUdpPort, String username, String password) {
        super(proxyAddress);
        if (username != null && username.length() == 0) {
            username = null;
        }
        if (password != null && password.length() == 0) {
            password = null;
        }
        this.localUdpPort =localUdpPort;
        this.username = username;
        this.password = password;
    }
    @Override
    public String protocol() {
        return PROTOCOL;
    }

    @Override
    public String authScheme() {
        return socksAuthMethod() == Socks5AuthMethod.PASSWORD? AUTH_PASSWORD : MY_AUTH_NONE;
    }

    public int localPort(){
        return localUdpPort;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    /**
     * @param socks5ProxySuccessListener the socks5ProxySuccessListener to set
     */
    public void setSocks5ProxySuccessListener(Socks5ProxySuccessListener socks5ProxySuccessListener) {
        this.socks5ProxySuccessListener = socks5ProxySuccessListener;
    }

    @Override
    protected void addCodec(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline p = ctx.pipeline();
        String name = ctx.name();

        Socks5InitialResponseDecoder decoder = new Socks5InitialResponseDecoder();
        p.addBefore(name, null, decoder);

        decoderName = p.context(decoder).name();
        encoderName = decoderName + ".encoder";

        p.addBefore(name, encoderName, Socks5ClientEncoder.DEFAULT);
    }

    @Override
    protected void removeEncoder(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().remove(encoderName);
    }

    @Override
    protected void removeDecoder(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline p = ctx.pipeline();
        if (p.context(decoderName) != null) {
            p.remove(decoderName);
        }
    }

    @Override
    protected Object newInitialMessage(ChannelHandlerContext ctx) throws Exception {
        return socksAuthMethod() == Socks5AuthMethod.PASSWORD? INIT_REQUEST_PASSWORD : INIT_REQUEST_NO_AUTH;
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
        if (response instanceof Socks5InitialResponse) {
            Socks5InitialResponse res = (Socks5InitialResponse) response;
            Socks5AuthMethod authMethod = socksAuthMethod();

            if (res.authMethod() != Socks5AuthMethod.NO_AUTH && res.authMethod() != authMethod) {
                // Server did not allow unauthenticated access nor accept the requested authentication scheme.
                throw new ProxyConnectException(exceptionMessage("unexpected authMethod: " + res.authMethod()));
            }

            if (authMethod == Socks5AuthMethod.NO_AUTH) {
                sendCommand(ctx);
            } else if (authMethod == Socks5AuthMethod.PASSWORD) {
                // In case of password authentication, send an authentication request.
                ctx.pipeline().replace(decoderName, decoderName, new Socks5PasswordAuthResponseDecoder());
                sendToProxyServer(new DefaultSocks5PasswordAuthRequest(
                        username != null? username : "", password != null? password : ""));
            } else {
                // Should never reach here.
                throw new Error();
            }

            return false;
        }

        if (response instanceof Socks5PasswordAuthResponse) {
            // Received an authentication response from the server.
            Socks5PasswordAuthResponse res = (Socks5PasswordAuthResponse) response;
            if (res.status() != Socks5PasswordAuthStatus.SUCCESS) {
                throw new ProxyConnectException(exceptionMessage("authStatus: " + res.status()));
            }

            sendCommand(ctx);
            return false;
        }

        // This should be the last message from the server.
        Socks5CommandResponse res = (Socks5CommandResponse) response;
        if (res.status() != Socks5CommandStatus.SUCCESS) {
            throw new ProxyConnectException(exceptionMessage("status: " + res.status()));
        }else if(socks5ProxySuccessListener!=null){
            //监听器回调成功的返回信息
            socks5ProxySuccessListener.onSuccess(res);
        }

        return true;
    }

    private Socks5AuthMethod socksAuthMethod() {
        Socks5AuthMethod authMethod;
        if (username == null && password == null) {
            authMethod = Socks5AuthMethod.NO_AUTH;
        } else {
            authMethod = Socks5AuthMethod.PASSWORD;
        }
        return authMethod;
    }

    private void sendCommand(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress raddr = destinationAddress();
        Socks5AddressType addrType;
        String rhost;
        if (raddr.isUnresolved()) {
            addrType = Socks5AddressType.DOMAIN;
            rhost = raddr.getHostString();
        } else {
            rhost = raddr.getAddress().getHostAddress();
            if (NetUtil.isValidIpV4Address(rhost)) {
                addrType = Socks5AddressType.IPv4;
            } else if (NetUtil.isValidIpV6Address(rhost)) {
                addrType = Socks5AddressType.IPv6;
            } else {
                throw new ProxyConnectException(
                        exceptionMessage("unknown address type: " + StringUtil.simpleClassName(rhost)));
            }
        }
        ctx.pipeline().replace(decoderName, decoderName, new Socks5CommandResponseDecoder());
        if (localUdpPort == LOCAL_PORT_TCP_CONNECT){
            //tcp connect
            sendToProxyServer(new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT, addrType, rhost, raddr.getPort()));
        }else {
            //udp proxy
            sendToProxyServer(new DefaultSocks5CommandRequest(Socks5CommandType.UDP_ASSOCIATE, addrType, DST_HOST_0X00, localUdpPort));
        }
    }

}
