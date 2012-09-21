package com.profiler.server.receiver.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;

import com.profiler.server.config.TomcatProfilerReceiverConfig;
import org.springframework.context.support.GenericApplicationContext;

public class MulplexedUDPReceiver implements DataReceiver {

	private static final int AcceptedSize = 65507;

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final ExecutorService worker = Executors.newFixedThreadPool(1024);

	private DatagramSocket udpSocket = null;
    private GenericApplicationContext context;
	long rejectedExecutionCount = 0;

    private volatile boolean state = true;

    public MulplexedUDPReceiver(GenericApplicationContext context) {
        this.context = context;
    }

    private Thread packetReader = new Thread(MulplexedUDPReceiver.class.getSimpleName()) {
		@Override
		public void run() {
			receive();
		}
	};

	public void receive() {
		try {
			this.udpSocket = new DatagramSocket(TomcatProfilerReceiverConfig.DEFUALT_PORT);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		if (udpSocket != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Waiting for " + MulplexedUDPReceiver.class.getSimpleName());
			}
            // 종료 처리필요.
			while (state) {
                DatagramPacket packet = null;
                try {
                    // TODO 최대 사이즈로 수정필요. 최대사이즈로 할경우 캐쉬필요.
                    byte[] buffer = new byte[AcceptedSize];
                    if (logger.isInfoEnabled()) {
                        logger.info("ReceiveBufferSize=" + udpSocket.getReceiveBufferSize());
                    }

                    packet = new DatagramPacket(buffer, AcceptedSize);
                    udpSocket.receive(packet);

                    if (logger.isDebugEnabled()) {
                        logger.debug("DatagramPacket read size:" + packet.getLength());
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    continue;
                }

                try {
					worker.execute(new MulplexedPacketHandler(packet, context));
				} catch (RejectedExecutionException ree) {
					rejectedExecutionCount++;
					if (rejectedExecutionCount > 1000) {
						logger.warn("RejectedExecutionCount=1000");
						rejectedExecutionCount = 0;
					}
				}
			}
		} else {
			logger.fatal("There is problem with making UDP Socket connection.");
		}
	}

	@Override
	public void start() {
		this.packetReader.start();
		logger.info("UDP Packet reader started.");
	}

	@Override
	public void shutdown() {
		logger.info("Shutting down UDP Packet reader.");
        state = false;
		// TODO 가능한 gracefull shutdown 구현필요.
		// this.udpSocket.close();
		// this.worker.shutdown();
		// try {
		// this.worker.awaitTermination(5, TimeUnit.SECONDS);
		// } catch (InterruptedException e) {
		// Thread.currentThread().interrupt();
		// }
	}
}
