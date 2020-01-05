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

package com.github.jdiverttunnel.weynidrop.tunnel;

import com.github.ffalcinelli.jdivert.Packet;
import com.github.ffalcinelli.jdivert.WinDivert;
import com.github.jdiverttunnel.weynidrop.tunnel.filter.DefaultFilter;
import com.github.jdiverttunnel.weynidrop.tunnel.handler.PacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 〈WinDivert 功能实现〉
 *  对于一个ip建议使用单线程模式，多线程可能会造成tcp数据包来往次序不一致的问题
 */
public class WinDivertThread implements Runnable{
    private static final Logger LOGGER = LoggerFactory.getLogger(WinDivertThread.class);
    private List<PacketHandler> handlers;
    private WinDivert winDivert;
    private String filter;


    public WinDivertThread(){
        this(DefaultFilter.DEFAULT.getFilter());
    }

    public WinDivertThread(String filter){
        this.filter = filter;
        this.handlers = new ArrayList<>();
    }

    public WinDivertThread(String filter,PacketHandler... handlers){
        this(filter);
        this.handlers.addAll(Arrays.asList(handlers));
    }

    public WinDivertThread appendHandler(PacketHandler... handlers){
        this.handlers.addAll(Arrays.asList(handlers));
        return this;
    }

    /**
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }

    public void close() {
        winDivert.close();
        Thread.currentThread().interrupt();
    }


    @Override
    public void run() {
        LOGGER.info("WinDivert start with filter:{}",filter);
        winDivert = new WinDivert(filter);
        try {
            winDivert.open();
            while (!Thread.currentThread().isInterrupted()) {
                // read a single packet
                Packet packet = winDivert.recv();

                //packet handlers
                this.handlers.forEach(handler->{
                    Boolean isMatch = handler.getFilter().match(packet);
                    if (isMatch!=null && isMatch){
                        handler.onReceivePacket(packet);
                    }
                });
                winDivert.send(packet);
            }
        } catch (Exception e) {
            LOGGER.error("WinDivert error:", e);
            //packet handlers
            this.handlers.forEach(handler -> handler.onCatchException(e));
        }
        // stop capturing packets
        close();
    }
}
