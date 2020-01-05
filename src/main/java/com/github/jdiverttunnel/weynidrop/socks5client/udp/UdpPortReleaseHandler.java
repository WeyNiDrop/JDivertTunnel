package com.github.jdiverttunnel.weynidrop.socks5client.udp;

import com.github.jdiverttunnel.weynidrop.common.socks5.listener.Socks5CloseListener;
import com.github.jdiverttunnel.weynidrop.common.utils.SocketUtils;
import com.github.jdiverttunnel.weynidrop.common.thread.ThreadManager;
import com.github.jdiverttunnel.weynidrop.common.utils.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 〈〉
 * 〈功能详细描述〉
 *
 * @author 88469401
 * @date 2019/12/24
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class UdpPortReleaseHandler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UdpPortReleaseHandler.class);

    private final InetSocketAddress localSrc;
    private Socks5CloseListener closeListener;

    /**
     * @param closeListener the closeListener to set
     */
    public void setCloseListener(Socks5CloseListener closeListener) {
        this.closeListener = closeListener;
    }

    public UdpPortReleaseHandler(InetSocketAddress localSrc, Socks5CloseListener closeListener) {
        this.localSrc = localSrc;
        this.closeListener = closeListener;
    }

    public UdpPortReleaseHandler(InetSocketAddress localSrc) {
        this.localSrc = localSrc;
    }

    public void close() {
        Thread.currentThread().interrupt();
        ThreadManager.getInstance().remove(this);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (SocketUtils.udpPortIsRelease(localSrc)) {
                LOGGER.debug("udp端口关闭，转发终止");
                if (closeListener != null) {
                    closeListener.onSocks5Close();
                }
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        close();
    }
}
