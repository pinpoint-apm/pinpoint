/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.udp;

import com.navercorp.pinpoint.collector.receiver.DataReceiver;
import com.navercorp.pinpoint.collector.util.DatagramPacketFactory;
import com.navercorp.pinpoint.collector.util.DefaultObjectPool;
import com.navercorp.pinpoint.collector.util.ObjectPool;
import com.navercorp.pinpoint.collector.util.PacketUtils;
import com.navercorp.pinpoint.collector.util.PooledObject;
import com.navercorp.pinpoint.common.util.ExecutorFactory;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.CpuUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestUDPReceiver implements DataReceiver {

    private final Logger logger;

    private final String bindAddress;
    private final int port;

    private final String receiverName;

    // increasing ioThread size wasn't very effective
    private final int ioThreadSize = CpuUtils.cpuCount();
    private ThreadPoolExecutor io;

    // modify thread pool size appropriately when modifying queue capacity
    private ThreadPoolExecutor worker;
    private int workerThreadSize = 128;
    private int workerThreadQueueSize = 1024;

    // can't really allocate memory as max udp packet sizes are unknown.
    // not allocating memory in advance as I am unsure of the max udp packet
    // size.
    // packet cache is necessary as the JVM does not last long if they are
    // dynamically created with the maximum size.
    private ObjectPool<DatagramPacket> datagramPacketPool;

    private final DatagramSocket socket;

    private final PacketHandlerFactory<DatagramPacket> packetHandlerFactory;

    private final AtomicInteger rejectedExecutionCount = new AtomicInteger(0);

    private final AtomicBoolean state = new AtomicBoolean(true);

    public TestUDPReceiver(String receiverName, PacketHandlerFactory<DatagramPacket> packetHandlerFactory, String bindAddress, int port,
            int receiverBufferSize, int workerThreadSize, int workerThreadQueueSize) {
        if (receiverName != null) {
            this.logger = LoggerFactory.getLogger(receiverName);
        } else {
            this.logger = LoggerFactory.getLogger(this.getClass());
        }
        if (packetHandlerFactory == null) {
            throw new NullPointerException("packetHandlerFactory must not be null");
        }
        if (bindAddress == null) {
            throw new NullPointerException("bindAddress must not be null");
        }

        this.receiverName = receiverName;
        this.bindAddress = bindAddress;
        this.port = port;
        this.socket = createSocket(receiverBufferSize);

        this.workerThreadSize = workerThreadSize;
        this.workerThreadQueueSize = workerThreadQueueSize;
        this.packetHandlerFactory = packetHandlerFactory;
    }

    public void afterPropertiesSet() {
        Assert.notNull(packetHandlerFactory, "packetHandlerFactory must not be null");

        final int packetPoolSize = getPacketPoolSize(workerThreadSize, workerThreadQueueSize);
        this.datagramPacketPool = new DefaultObjectPool<>(new DatagramPacketFactory(), packetPoolSize);
        this.worker = ExecutorFactory.newFixedThreadPool(workerThreadSize, workerThreadQueueSize, receiverName + "-Worker", true);

        this.io = (ThreadPoolExecutor) Executors.newCachedThreadPool(new PinpointThreadFactory(receiverName + "-Io", true));
    }

    private void receive(final DatagramSocket socket) {
        if (logger.isInfoEnabled()) {
            logger.info("start ioThread localAddress:{}, IoThread:{}", this.socket.getLocalAddress(), Thread.currentThread().getName());
        }
        final SocketAddress localSocketAddress = socket.getLocalSocketAddress();
        final boolean debugEnabled = logger.isDebugEnabled();

        // need shutdown logic
        while (state.get()) {
            PooledObject<DatagramPacket> pooledPacket = read0(socket);
            if (pooledPacket == null) {
                continue;
            }
            final DatagramPacket packet = pooledPacket.getObject();
            if (packet.getLength() == 0) {
                if (debugEnabled) {
                    logger.debug("length is 0 ip:{}, port:{}", packet.getAddress(), packet.getPort());
                }
                return;
            }
            if (debugEnabled) {
                logger.debug("pool getActiveCount:{}", worker.getActiveCount());
            }
            try {
                Runnable dispatchTask = wrapDispatchTask(pooledPacket);
                worker.execute(dispatchTask);
            } catch (RejectedExecutionException ree) {
                logger.warn(ree.getMessage(), ree);
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("stop ioThread localAddress:{}, IoThread:{}", localSocketAddress, Thread.currentThread().getName());
        }
    }

    private Runnable wrapDispatchTask(final PooledObject<DatagramPacket> pooledPacket) {
        final Runnable lazyExecution = new Runnable() {
            @Override
            public void run() {
                PacketHandler<DatagramPacket> dispatchPacket = packetHandlerFactory.createPacketHandler();
                PooledPacketWrap pooledPacketWrap = new PooledPacketWrap(socket, dispatchPacket, pooledPacket);
                Runnable execution = pooledPacketWrap;
                execution.run();
            }
        };
        return lazyExecution;
    }

    private PooledObject<DatagramPacket> read0(final DatagramSocket socket) {
        boolean success = false;
        PooledObject<DatagramPacket> pooledObject = datagramPacketPool.getObject();
        if (pooledObject == null) {
            logger.error("datagramPacketPool is empty");
            return null;
        }
        DatagramPacket packet = pooledObject.getObject();
        try {
            try {
                socket.receive(packet);
                success = true;
            } catch (SocketTimeoutException e) {
                return null;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("DatagramPacket SocketAddress:{} read size:{}", packet.getSocketAddress(), packet.getLength());
                if (logger.isTraceEnabled()) {
                    // use trace as packet dump may be large
                    logger.trace("dump packet:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            }
        } catch (IOException e) {
            if (!state.get()) {
                // shutdown
            } else {
                logger.error("IoError, Caused:", e.getMessage(), e);
            }
            return null;
        } finally {
            if (!success) {
                pooledObject.returnObject();
            }
        }
        return pooledObject;
    }

    private DatagramSocket createSocket(int receiveBufferSize) {
        try {
            DatagramSocket socket = new DatagramSocket(null);
            socket.setReceiveBufferSize(receiveBufferSize);
            if (logger.isWarnEnabled()) {
                final int checkReceiveBufferSize = socket.getReceiveBufferSize();
                if (receiveBufferSize != checkReceiveBufferSize) {
                    logger.warn("DatagramSocket.setReceiveBufferSize() error. {}!={}", receiveBufferSize, checkReceiveBufferSize);
                }
            }
            socket.setSoTimeout(1000 * 5);
            return socket;
        } catch (SocketException ex) {
            throw new RuntimeException("Socket create Fail. Caused:" + ex.getMessage(), ex);
        }
    }

    private void bindSocket(DatagramSocket socket, String bindAddress, int port) {
        if (socket == null) {
            throw new NullPointerException("socket must not be null");
        }
        try {
            logger.info("DatagramSocket.bind() {}/{}", bindAddress, port);
            socket.bind(new InetSocketAddress(bindAddress, port));
        } catch (SocketException ex) {
            throw new IllegalStateException("Socket bind Fail. port:" + port + " Caused:" + ex.getMessage(), ex);
        }
    }

    private int getPacketPoolSize(int workerThreadSize, int workerThreadQueueSize) {
        return workerThreadSize + workerThreadQueueSize + ioThreadSize;
    }

    @PostConstruct
    @Override
    public void start() {
        logger.info("{} start.", receiverName);
        afterPropertiesSet();
        final DatagramSocket socket = this.socket;
        if (socket == null) {
            throw new IllegalStateException("socket is null.");
        }
        bindSocket(socket, bindAddress, port);

        logger.info("UDP Packet reader:{} started.", ioThreadSize);
        for (int i = 0; i < ioThreadSize; i++) {
            io.execute(new Runnable() {
                @Override
                public void run() {
                    receive(socket);
                }
            });
        }

    }

    @PreDestroy
    @Override
    public void shutdown() {
        logger.info("{} shutdown.", this.receiverName);
        state.set(false);
        // is it okay to just close here?
        if (socket != null) {
            socket.close();
        }
        shutdownExecutor(io, "IoExecutor");
        shutdownExecutor(worker, "WorkerExecutor");
    }

    private void shutdownExecutor(ExecutorService executor, String executorName) {
        logger.info("{] shutdown.", executorName);
        executor.shutdown();
        try {
            executor.awaitTermination(1000 * 10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.info("{}.shutdown() Interrupted", executorName, e);
            Thread.currentThread().interrupt();
        }
    }
}
