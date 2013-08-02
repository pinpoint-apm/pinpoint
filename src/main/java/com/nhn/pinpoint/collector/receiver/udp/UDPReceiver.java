package com.nhn.pinpoint.collector.receiver.udp;

import com.nhn.pinpoint.collector.receiver.DispatchHandler;
import com.nhn.pinpoint.collector.util.DatagramPacketFactory;
import com.nhn.pinpoint.collector.util.FixedPool;
import com.nhn.pinpoint.collector.util.PacketUtils;
import com.nhn.pinpoint.common.dto2.Header;
import com.nhn.pinpoint.common.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.common.util.ExecutorFactory;
import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
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
import java.util.concurrent.atomic.AtomicInteger;

public class UDPReceiver implements DataReceiver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

//    private static final ThreadFactory THREAD_FACTORY = new PinpointThreadFactory("Pinpoint-UDP-Io");

    private int threadSize = 512;
    private int workerQueueSize = 1024 * 5;
    // queue에 적체 해야 되는 max 사이즈 변경을 위해  thread pool을 조정해야함.
    private final ThreadPoolExecutor worker = ExecutorFactory.newFixedThreadPool(threadSize, workerQueueSize, "Pinpoint-UDP-Worker", true);

    // udp 패킷의 경우 맥스 사이즈가 얼마일지 알수 없어서 메모리를 할당해서 쓰기가 그럼. 내가 모르는걸수도 있음. 이럴경우 더 좋은방법으로 수정.
    // 최대치로 동적할당해서 사용하면 jvm이 얼마 버티지 못하므로 packet을 캐쉬할 필요성이 있음.
    private final FixedPool<DatagramPacket> datagramPacketPool = new FixedPool<DatagramPacket>(new DatagramPacketFactory(), threadSize + workerQueueSize);

    private DatagramSocket socket = null;

    private DispatchHandler dispatchHandler;

    private AtomicInteger rejectedExecutionCount = new AtomicInteger(0);

    private AtomicBoolean state = new AtomicBoolean(true);

    private final CountDownLatch startLatch = new CountDownLatch(1);

    public UDPReceiver(DispatchHandler dispatchHandler, int port) {
        if (dispatchHandler == null) {
            throw new NullPointerException("dispatchHandler");
        }
        this.socket = createSocket(port);
        this.dispatchHandler = dispatchHandler;
    }

    private Thread ioThread = new PinpointThreadFactory("Pinpoint-UDP-Io").newThread(new Runnable() {
        @Override
        public void run() {
            receive();
        }
    });


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
                final int error = rejectedExecutionCount.getAndIncrement();
                final int mod = 10;
                if ((error % mod) == 0) {
                    logger.warn("RejectedExecutionCount={}", error);
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
            // thread local로 캐쉬할까? 근데 worker라서 별로 영향이 없을거 같음.
            HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
            try {
                TBase<?, ?> tBase = deserializer.deserialize(packet.getData());
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
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{}", new Object[]{packet.getSocketAddress(), e.getMessage(), e});
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            } finally {
                datagramPacketPool.returnObject(packet);
            }
        }


    }
}
