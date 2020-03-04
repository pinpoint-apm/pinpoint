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

package com.navercorp.pinpoint.collector.receiver.thrift.udp;

import com.navercorp.pinpoint.collector.util.ObjectPool;
import com.navercorp.pinpoint.collector.util.PacketUtils;
import com.navercorp.pinpoint.collector.util.PooledObject;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CpuUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author emeroad
 * @author netspider
 * @author jaehong.kim
 */
public class UDPReceiver {

    private final Logger logger;

    private final String name;

    private final InetSocketAddress bindAddress;

    // increasing ioThread size wasn't very effective
    private final int ioThreadSize;
    private ExecutorService ioExecutor;

    private final Executor worker;

    // can't really allocate memory as max udp packet sizes are unknown.
    // not allocating memory in advance as I am unsure of the max udp packet size.
    // packet cache is necessary as the JVM does not last long if they are dynamically created with the maximum size.
    private final ObjectPool<DatagramPacket> datagramPacketPool;

    //    private final DatagramSocket socket;
    private final DatagramSocket[] sockets;

    private final PacketHandlerFactory<DatagramPacket> packetHandlerFactory;

    private final AtomicBoolean state = new AtomicBoolean(true);

    public UDPReceiver(String name, PacketHandlerFactory<DatagramPacket> packetHandlerFactory,
                       @Qualifier("udpWorker") Executor worker, int receiverBufferSize, InetSocketAddress bindAddress, ObjectPool<DatagramPacket> datagramPacketPool) {
        this(name, packetHandlerFactory, worker, receiverBufferSize, bindAddress, null, datagramPacketPool);
    }

    public UDPReceiver(String name, PacketHandlerFactory<DatagramPacket> packetHandlerFactory,
                       @Qualifier("udpWorker") Executor worker, int receiverBufferSize, InetSocketAddress bindAddress, ReusePortSocketOptionHolder socketOptionHolder, ObjectPool<DatagramPacket> datagramPacketPool) {
        this.name = Objects.requireNonNull(name);
        this.logger = LoggerFactory.getLogger(name);

        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
        this.packetHandlerFactory = Objects.requireNonNull(packetHandlerFactory, "packetHandlerFactory");
        this.worker = Objects.requireNonNull(worker, "worker");

        Assert.isTrue(receiverBufferSize > 0, "receiverBufferSize must be greater than 0");

        int ioThreadSize = CpuUtils.cpuCount();
        if (socketOptionHolder != null) {
            int socketCount = socketOptionHolder.getSocketCount();
            if (socketCount == -1) {
                socketCount = ioThreadSize;
            }

            this.sockets = createSocket(receiverBufferSize, socketOptionHolder, socketCount);
            logger.info("Created multiple UDP socket. sockets:{}", Arrays.asList(sockets));
            if (sockets.length > ioThreadSize) {
                ioThreadSize = sockets.length;
            }
        } else {
            this.sockets = createSocket(receiverBufferSize);
        }

        this.ioThreadSize = ioThreadSize;
        this.datagramPacketPool = Objects.requireNonNull(datagramPacketPool, "datagramPacketPool");
    }

    private void receive(final DatagramSocket socket) {
        if (logger.isInfoEnabled()) {
            logger.info("start ioThread localAddress:{}, IoThread:{}", socket.getLocalAddress(), Thread.currentThread().getName());
        }

        // need shutdown logic
        while (state.get()) {
            final PooledObject<DatagramPacket> pooledPacket = read0(socket);
            if (pooledPacket == null) {
                continue;
            }
            Runnable task = wrapTask(socket, pooledPacket);
            worker.execute(task);
        }

        if (logger.isInfoEnabled()) {
            final SocketAddress localSocketAddress = socket.getLocalSocketAddress();
            logger.info("stop ioThread localAddress:{}, IoThread:{}", localSocketAddress, Thread.currentThread().getName());
        }
    }

    private Runnable wrapTask(final DatagramSocket socket, final PooledObject<DatagramPacket> pooledPacket) {
        return new
                Task(socket, packetHandlerFactory, pooledPacket);
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
                logger.error("IoError, Caused by:{}", e.getMessage(), e);
            }
            return null;
        } finally {
            if (!success) {
                pooledObject.returnObject();
            }
        }
        if (!validatePacket(packet)) {
            pooledObject.returnObject();
            return null;
        }
        return pooledObject;
    }

    @VisibleForTesting
    boolean validatePacket(DatagramPacket packet) {
        // L4 health check packet
        if (packet.getLength() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("length is 0 ip:{}, port:{}", packet.getAddress(), packet.getPort());
            }
            return false;
        }

        return true;
    }

    private DatagramSocket[] createSocket(int receiveBufferSize) {
        DatagramSocket socket = createSocket0(receiveBufferSize);
        return new DatagramSocket[]{socket};
    }

    private DatagramSocket[] createSocket(int receiveBufferSize, ReusePortSocketOptionHolder socketOptionHolder, int socketCount) {
        if (!socketOptionHolder.isEnable()) {
            return createSocket(receiveBufferSize);
        }

        DatagramSocket[] datagramSockets = new DatagramSocket[socketCount];
        for (int i = 0; i < socketCount; i++) {
            DatagramSocket socket = createSocket0(receiveBufferSize);

            boolean isSet = setSocketOption(socket, socketOptionHolder);
            if (!isSet) {
                logger.warn("Failed to create multipleSocket.");
                return new DatagramSocket[]{socket};
            }

            datagramSockets[i] = socket;
        }
        return datagramSockets;
    }

    private DatagramSocket createSocket0(int receiveBufferSize) {
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

    // caution : java9+
    private boolean setSocketOption(DatagramSocket socket, ReusePortSocketOptionHolder socketOptionHolder) {
        if (socketOptionHolder == null) {
            return false;
        }

        try {
            Method setOptionMethod = DatagramSocket.class.getDeclaredMethod("setOption", SocketOption.class, Object.class);
            setOptionMethod.invoke(socket, socketOptionHolder.getSocketOption(), socketOptionHolder.isEnable());
            return true;
        } catch (Exception e) {
            logger.warn("Failed to set SocketOption. caused : can not set 'java.net.DatagramSocket#setOption' method", e);
        }
        return false;
    }

    private void bindSocket(DatagramSocket socket, InetSocketAddress bindAddress) {
        try {
            logger.info("DatagramSocket.bind() {}/{}", bindAddress.getHostString(), bindAddress.getPort());
            socket.bind(bindAddress);
        } catch (SocketException ex) {
            throw new IllegalStateException("Socket bind Fail. port:" + bindAddress.getPort() + " Caused:" + ex.getMessage(), ex);
        }
    }

    private ExecutorService newThreadPoolExecutor() {
        final ThreadFactory threadFactory = new PinpointThreadFactory(name + "-Io", true);
        return Executors.newCachedThreadPool(threadFactory);
    }

    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("{} start() started", name);
        }

        this.ioExecutor = newThreadPoolExecutor();
        final DatagramSocket[] sockets = this.sockets;
        if (sockets == null) {
            throw new IllegalStateException("socket is null.");
        }
        for (DatagramSocket socket : sockets) {
            bindSocket(socket, bindAddress);
        }

        logger.info("UDP Packet reader:{} started.", ioThreadSize);
        for (int i = 0; i < ioThreadSize; i++) {
            final int index = i;
            ioExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    receive(sockets[index % sockets.length]);
                }
            });
        }

        if (logger.isInfoEnabled()) {
            logger.info("{} start() completed", name);
        }
    }

    public void shutdown() {
        if (logger.isInfoEnabled()) {
            logger.info("{} shutdown() started", this.name);
        }

        state.set(false);
        // is it okay to just close here?
        if (sockets != null) {
            for (DatagramSocket socket : sockets) {
                socket.close();
            }
        }
        if (ioExecutor != null) {
            shutdownExecutor(ioExecutor, name);
        }

        if (logger.isInfoEnabled()) {
            logger.info("{} shutdown() completed", this.name);
        }
    }

    private void shutdownExecutor(ExecutorService executor, String executorName) {
        logger.info("{} shutdown.", executorName);
        executor.shutdown();
        try {
            executor.awaitTermination(1000 * 10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.info("{}.shutdown() Interrupted", executorName, e);
            Thread.currentThread().interrupt();
        }
    }

}
