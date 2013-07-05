package com.nhn.pinpoint.collector.receiver.udp;

import com.nhn.pinpoint.collector.config.TomcatProfilerReceiverConfig;
import com.nhn.pinpoint.collector.util.DatagramPacketFactory;
import com.nhn.pinpoint.collector.util.FixedPool;
import com.nhn.pinpoint.collector.util.PacketUtils;
import org.jboss.netty.handler.timeout.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiplexedUDPReceiver implements DataReceiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private final ThreadPoolExecutor worker = (ThreadPoolExecutor) Executors.newFixedThreadPool(512);

    private final FixedPool<DatagramPacket> datagramPacketPool = new FixedPool<DatagramPacket>(new DatagramPacketFactory(),1024);

    private DatagramSocket socket = null;

    private MultiplexedPacketHandler multiplexedPacketHandler;

    private long rejectedExecutionCount = 0;

    private AtomicBoolean state = new AtomicBoolean(true);

    private final CountDownLatch startLatch = new CountDownLatch(1);

    public MultiplexedUDPReceiver(MultiplexedPacketHandler multiplexedPacketHandler) {
        this(multiplexedPacketHandler, TomcatProfilerReceiverConfig.SERVER_UDP_LISTEN_PORT);
    }

    public MultiplexedUDPReceiver(MultiplexedPacketHandler multiplexedPacketHandler, int port) {
        if (multiplexedPacketHandler == null) {
            throw new NullPointerException("multiplexedPacketHandler");
        }
        this.socket = createSocket(port);
        this.multiplexedPacketHandler = multiplexedPacketHandler;
    }

    private Thread ioThread = new Thread(MultiplexedUDPReceiver.class.getSimpleName()) {
        @Override
        public void run() {
            receive();
        }
    };

    public void receive() {
        if (logger.isInfoEnabled()) {
            logger.info("Waiting agent data on {}", this.socket.getLocalSocketAddress());
        }

        startLatch.countDown();

        // 종료 처리필요.
        while (state.get()) {
            DatagramPacket packet = read0();
            if (packet == null) {
                continue;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("pool getActiveCount:{}", worker.getActiveCount());
            }

            try {
                worker.execute(new DispatchPacket(packet));
            } catch (RejectedExecutionException ree) {
                rejectedExecutionCount++;
                if (rejectedExecutionCount > 1000) {
                    logger.warn("RejectedExecutionCount=1000");
                    rejectedExecutionCount = 0;
                }
            }
        }
    }

    private DatagramPacket read0() {
        boolean success = false;
        DatagramPacket packet = datagramPacketPool.getObject();
        if (packet == null) {
            logger.error("datagramPacketPool is empty");
            return null;
        }
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
                     // dump packet은 데이터가 많을것이니 trace로
                    logger.trace("dump packet:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            }
        } catch (IOException e) {
            if (state.get() == false) {
                // shutdown
            } else {
                logger.error(e.getMessage(), e);
            }
            return null;
        } finally {
            if (!success) {
                datagramPacketPool.returnObject(packet);
            }
        }
        return packet;
    }

    private DatagramSocket createSocket(int port) {
        try {
            DatagramSocket so = new DatagramSocket(port);
            so.setSoTimeout(1000 * 10);
            return so;
        } catch (SocketException ex) {
            throw new RuntimeException("Socket create Fail. port:" + port + " Caused:" + ex.getMessage(), ex);
        }
    }

    @Override
    public Future<Boolean> start() {
        if (socket != null) {
            this.ioThread.start();
            logger.info("UDP Packet reader started.");

            return new Future<Boolean>() {
                @Override
                public boolean isDone() {
                    return state.get();
                }

                @Override
                public boolean isCancelled() {
                    return !state.get();
                }

                @Override
                public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    if (!startLatch.await(timeout, unit)) {
                        throw new TimeoutException();
                    } else {
                        return state.get();
                    }
                }

                @Override
                public Boolean get() throws InterruptedException, ExecutionException {
                    return get(3000L, TimeUnit.MILLISECONDS);
                }

                @Override
                public boolean cancel(boolean ign) {
                    if (ign) {
                        shutdown();
                    }
                    return !state.get();
                }
            };
        } else {
            throw new RuntimeException("socket create fail");
        }
    }

    @Override
    public void shutdown() {
        logger.info("Shutting down UDP Packet reader.");
        state.set(false);
        // 그냥 닫으면 되는건지?
        socket.close();
        worker.shutdown();
        try {
            worker.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private class DispatchPacket implements Runnable {
        private final DatagramPacket packet;

        private DispatchPacket(DatagramPacket packet) {
            if (packet == null) {
                throw new NullPointerException("packet");
            }
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                multiplexedPacketHandler.handlePacket(packet);
                // packet에 대한 캐쉬를 해야 될듯.
                // packet.return(); 등등
            } finally {
                datagramPacketPool.returnObject(packet);
            }
        }
    }
}
