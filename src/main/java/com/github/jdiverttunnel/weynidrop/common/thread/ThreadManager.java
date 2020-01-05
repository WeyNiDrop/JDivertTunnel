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

package com.github.jdiverttunnel.weynidrop.common.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ThreadManager {
    private static final ThreadManager INSTANCE = new ThreadManager();
    private ExecutorService executorService;

    private ThreadManager() {
        executorService = new ThreadPoolExecutor(128, 128,
                0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(12),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public boolean remove(Runnable runnable){
        return ((ThreadPoolExecutor) executorService).remove(runnable);
    }

    public void execute(Runnable runnable){
        executorService.execute(runnable);
    }

    public boolean isShutdown(){
        return executorService.isShutdown();
    }

    /**
     * @return the instance
     */
    public static ThreadManager getInstance() {
        return INSTANCE;
    }


    public void shutDown(){
        executorService.shutdown();
    }
}
