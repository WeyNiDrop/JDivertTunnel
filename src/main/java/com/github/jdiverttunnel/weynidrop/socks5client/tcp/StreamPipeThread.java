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

package com.github.jdiverttunnel.weynidrop.socks5client.tcp;

import com.github.jdiverttunnel.weynidrop.common.thread.ThreadManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamPipeThread implements Runnable{
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private boolean isRunning;

    /**
     * @return the isRunning
     */
    public boolean isRunning() {
        return isRunning;
    }

    public StreamPipeThread(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        isRunning = false;
    }

    public void start(){
        ThreadManager.getInstance().execute(this);
        isRunning = true;
    }

    public void close(){
        ThreadManager.getInstance().remove(this);
        isRunning = false;
        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        int len = 0;
        //定义一个2k字节的缓存
        byte[] buffer=new byte[1024*2];
        //将输入流的数据写到输出流
        try{
            while (isRunning&&!Thread.currentThread().isInterrupted()&&((len = inputStream.read(buffer,0,buffer.length))!=-1)){
                outputStream.write(buffer,0,len);
                outputStream.flush();
            }
        }catch (IOException e){
        }
        close();
    }
}

