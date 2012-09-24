package com.profiler.server.receiver.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.profiler.server.config.TomcatProfilerReceiverConfig;
import org.springframework.context.support.GenericApplicationContext;

public class MultiplexedUDPReceiver implements DataReceiver {

	private static final int AcceptedSize = 65507;

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final ExecutorService worker = Executors.newFixedThreadPool(1024);

	private DatagramSocket socket = null;

    private GenericApplicationContext context;
    MultiplexedPacketHandler multiplexedPacketHandler;

	long rejectedExecutionCount = 0;

    private AtomicBoolean state = new AtomicBoolean(true);

    public MultiplexedUDPReceiver(GenericApplicationContext context) {
        this.context = context;
        this.socket = createSocket();
        this.multiplexedPacketHandler = this.context.getBean("MultiplexedPacketHandler", MultiplexedPacketHandler.class);
    }

    private Thread ioThread = new Thread(MultiplexedUDPReceiver.class.getSimpleName()) {
		@Override
		public void run() {
			receive();
		}
	};

	public void receive() {
        if (logger.isInfoEnabled()) {
            logger.info("Waiting for " + MultiplexedUDPReceiver.class.getSimpleName());
        }
        // 종료 처리필요.
        while (state.get()) {
            DatagramPacket packet = null;
            try {
                // TODO 최대 사이즈로 수정필요. 최대사이즈로 할경우 캐쉬필요.
                byte[] buffer = new byte[AcceptedSize];

                packet = new DatagramPacket(buffer, AcceptedSize);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    continue;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("DatagramPacket read size:" + packet.getLength());
                }
            } catch (IOException e) {
                if (state.get()== false) {
                    // shutdown
                } else {
                    logger.error(e.getMessage(), e);
                }
                continue;
            }

            try {
                logger.debug("Dispatch packet");
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

    private DatagramSocket createSocket() {
        try {
            DatagramSocket so = new DatagramSocket(TomcatProfilerReceiverConfig.DEFUALT_PORT);
            so.setSoTimeout(1000 * 5);
            return so;
        } catch (SocketException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
	public void start() {
        if (socket != null) {
            this.ioThread.start();
            logger.info("UDP Packet reader started.");
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
            this.packet = packet;
        }

        @Override
        public void run() {
            multiplexedPacketHandler.handlePacket(packet);
            // packet에 대한 캐쉬를 해야 될듯.
            // packet.return(); 등등
        }
    }
}
