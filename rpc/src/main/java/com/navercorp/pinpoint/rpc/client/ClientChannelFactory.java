/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.BossPool;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.channel.socket.nio.WorkerPool;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.Timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClientChannelFactory {

    public ChannelFactory createChannelFactory(int bossCount, int workerCount, Timer timer) {
        ExecutorService boss = newCachedThreadPool("Pinpoint-Client-Boss");
        BossPool bossPool = new NioClientBossPool(boss, bossCount, timer, ThreadNameDeterminer.CURRENT);

        ExecutorService worker = newCachedThreadPool("Pinpoint-Client-Worker");
        WorkerPool workerPool = new NioWorkerPool(worker, workerCount, ThreadNameDeterminer.CURRENT);
        return new NioClientSocketChannelFactory(bossPool, workerPool);
    }

    private ExecutorService newCachedThreadPool(String threadName) {
        ThreadFactory threadFactory = new PinpointThreadFactory(threadName, true);
        return Executors.newCachedThreadPool(threadFactory);
    }
}
