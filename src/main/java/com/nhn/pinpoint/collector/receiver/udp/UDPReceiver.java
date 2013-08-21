package com.nhn.pinpoint.collector.receiver.udp;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.nhn.pinpoint.collector.StatServer;
import com.nhn.pinpoint.collector.config.CollectorConfiguration;
import com.nhn.pinpoint.collector.receiver.DataReceiver;
import com.nhn.pinpoint.collector.receiver.DispatchHandler;
import com.nhn.pinpoint.collector.util.DatagramPacketFactory;
import com.nhn.pinpoint.collector.util.FixedPool;
import com.nhn.pinpoint.collector.util.PacketUtils;
import com.nhn.pinpoint.common.dto2.Header;
import com.nhn.pinpoint.common.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.common.util.ExecutorFactory;
import com.nhn.pinpoint.rpc.util.CpuUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UDPReceiver implements DataReceiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private final Timer timer = StatServer.registry.timer(UDPReceiver.class.getSimpleName() + "-timer");
    private final Counter rejectedCounter = StatServer.registry.counter(UDPReceiver.class.getSimpleName() + "-rejected");

    // ioThread 사이즈를 늘리거는 생각보다 효용이 별로임.
    private int ioThreadSize = CpuUtils.cpuCount();
    private final ThreadPoolExecutor io = ExecutorFactory.newFixedThreadPool(ioThreadSize, Integer.MAX_VALUE, "Pinpoint-UDP-Io", true);

    // queue에 적체 해야 되는 max 사이즈 변경을 위해  thread pool을 조정해야함.
    private final ThreadPoolExecutor worker;

    // udp 패킷의 경우 맥스 사이즈가 얼마일지 알수 없어서 메모리를 할당해서 쓰기가 그럼. 내가 모르는걸수도 있음. 이럴경우 더 좋은방법으로 수정.
    // 최대치로 동적할당해서 사용하면 jvm이 얼마 버티지 못하므로 packet을 캐쉬할 필요성이 있음.
    private final FixedPool<DatagramPacket> datagramPacketPool;


    private volatile DatagramSocket socket = null;

    private DispatchHandler dispatchHandler;

    private AtomicInteger rejectedExecutionCount = new AtomicInteger(0);

    private AtomicBoolean state = new AtomicBoolean(true);


    public UDPReceiver(DispatchHandler dispatchHandler, CollectorConfiguration configuration) {
        if (dispatchHandler == null) {
            throw new NullPointerException("dispatchHandler must not be null");
        }
        this.socket = createSocket(configuration.getCollectorUdpListenPort(), configuration.getUdpSocketReceiveBufferSize());
        this.dispatchHandler = dispatchHandler;

        this.datagramPacketPool = new FixedPool<DatagramPacket>(new DatagramPacketFactory(), getPacketPoolSize(configuration));
        this.worker = ExecutorFactory.newFixedThreadPool(configuration.getUdpWorkerThread(), configuration.getUdpWorkerQueueSize(), "Pinpoint-UDP-Worker", true);
    }


    private void receive() {
        if (logger.isInfoEnabled()) {
            logger.info("start ioThread localAddress:{}, IoThread:{}", socket.getLocalAddress(), Thread.currentThread().getName());
        }
        final SocketAddress localSocketAddress = socket.getLocalSocketAddress();
        final boolean debugEnabled = logger.isDebugEnabled();

        // 종료 처리필요.
        while (state.get()) {
            DatagramPacket packet = read0();
            if (packet == null) {
                continue;
            }
            if (debugEnabled) {
                logger.debug("pool getActiveCount:{}", worker.getActiveCount());
            }
            try {
                worker.execute(new DispatchPacket(packet));
            } catch (RejectedExecutionException ree) {
            	rejectedCounter.inc();
                final int error = rejectedExecutionCount.incrementAndGet();
                final int mod = 100;
                if ((error % mod) == 0) {
                    logger.warn("RejectedExecutionCount={}", error);
                }
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("stop ioThread localAddress:{}, IoThread:{}", localSocketAddress, Thread.currentThread().getName());
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
            if (!state.get()) {
                // shutdown
            } else {
                logger.error("IoError, Caused:", e.getMessage(), e);
            }
            return null;
        } finally {
            if (!success) {
                datagramPacketPool.returnObject(packet);
            }
        }
        return packet;
    }

    private DatagramSocket createSocket(int port, int receiveBufferSize) {
        try {
            DatagramSocket so = new DatagramSocket(port);
            so.setReceiveBufferSize(receiveBufferSize);
            so.setSoTimeout(1000 * 5);
            return so;
        } catch (SocketException ex) {
            throw new RuntimeException("Socket create Fail. port:" + port + " Caused:" + ex.getMessage(), ex);
        }
    }

    private int getPacketPoolSize(CollectorConfiguration configuration) {
        return configuration.getUdpWorkerThread() + configuration.getUdpWorkerQueueSize() + ioThreadSize;
    }

    @Override
    public void start() {
        if (socket == null) {
            throw new RuntimeException("socket create fail");
        }

        logger.info("UDP Packet reader:{} started.", ioThreadSize);
        for (int i = 0; i < ioThreadSize; i++) {
            io.execute(new Runnable() {
                @Override
                public void run() {
                    receive();
                }
            });
        }

    }

    @Override
    public void shutdown() {
        logger.info("Shutting down UDP Packet reader.");
        state.set(false);
        // 그냥 닫으면 되는건지?
        socket.close();
        shutdownExecutor(io, "IoExecutor");
        shutdownExecutor(worker, "WorkerExecutor");
    }

    private void shutdownExecutor(ExecutorService executor, String executorName) {
        executor.shutdown();
        try {
            executor.awaitTermination(1000*10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.info("{}.shutdown() Interrupted", executorName, e);
            Thread.currentThread().interrupt();
        }
    }

    private class DispatchPacket implements Runnable {
        private final DatagramPacket packet;
        private final ThreadLocal<HeaderTBaseDeserializer> deserializer = new NamedThreadLocal<HeaderTBaseDeserializer>("HeaderTBaseDeserializer") {
            @Override
            protected HeaderTBaseDeserializer initialValue() {
                return new HeaderTBaseDeserializer();
            }
        };

        private DispatchPacket(DatagramPacket packet) {
            if (packet == null) {
                throw new NullPointerException("packet must not be null");
            }
            this.packet = packet;
        }

        @Override
        public void run() {
        	Timer.Context time = timer.time();
        	
            HeaderTBaseDeserializer deserializer = this.deserializer.get();
            TBase<?, ?> tBase = null;
            try {
                tBase = deserializer.deserialize(packet.getData());
                // dispatch는 비지니스 로직 실행을 의미.
                dispatchHandler.dispatch(tBase, packet.getData(), Header.HEADER_SIZE, packet.getLength());
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", new Object[]{packet.getSocketAddress(), e.getMessage(), e});
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            } catch (Exception e) {
                // 잘못된 header가 도착할 경우 발생하는 케이스가 있음.
                if (logger.isWarnEnabled()) {
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{} tbase:{}", new Object[]{packet.getSocketAddress(), e.getMessage(), e, tBase});
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            } finally {
                datagramPacketPool.returnObject(packet);
                // exception 난 경우는 어떻게 해야 하지?
                time.stop();
            }
        }


    }
}
