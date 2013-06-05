package com.nhn.pinpoint.collector.receiver.udp;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.nhn.pinpoint.collector.util.BufferPool;
import com.nhn.pinpoint.collector.config.TomcatProfilerReceiverConfig;
import com.nhn.pinpoint.collector.util.PacketUtils;

import org.jboss.netty.handler.timeout.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;

public class MultiplexedUDPReceiver implements DataReceiver {

	private static final int AcceptedSize = 65507;

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private final ThreadPoolExecutor worker = (ThreadPoolExecutor) Executors.newFixedThreadPool(512);
	private final BufferPool pool = new BufferPool(1024);

	private DatagramSocket socket = null;

	private GenericApplicationContext context;
	private MultiplexedPacketHandler multiplexedPacketHandler;

	long rejectedExecutionCount = 0;

	private AtomicBoolean state = new AtomicBoolean(true);

	private final CountDownLatch startLatch = new CountDownLatch(1);

	public MultiplexedUDPReceiver(GenericApplicationContext context) {
        this(context, TomcatProfilerReceiverConfig.SERVER_UDP_LISTEN_PORT);
    }

    public MultiplexedUDPReceiver(GenericApplicationContext context, int port) {
        this.context = context;
        this.socket = createSocket(port);
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
			logger.info("Waiting agent data on {}", this.socket.getLocalSocketAddress());
		}

		startLatch.countDown();

		// 종료 처리필요.
		while (state.get()) {
			boolean success = false;
			DatagramPacket packet = null;
			try {
				// TODO 최대 사이즈로 수정필요. 최대사이즈로 할경우 캐쉬필요.
				byte[] buffer = pool.getBuffer();
				packet = new DatagramPacket(buffer, AcceptedSize);
				try {
					socket.receive(packet);
				} catch (SocketTimeoutException e) {
					continue;
				}

				if (logger.isDebugEnabled()) {
					logger.debug("DatagramPacket SocketAddress:" + packet.getSocketAddress() + " read size:" + packet.getLength());
                    if (logger.isTraceEnabled()) {
                        // dump packet은 데이터가 많을것이니 trace로
                        logger.trace("dump packet:" + PacketUtils.dumpDatagramPacket(packet));
                    }
				}
				success = true;
			} catch (IOException e) {
				if (state.get() == false) {
					// shutdown
				} else {
					logger.error(e.getMessage(), e);
				}
				continue;
			} finally {
				if (!success) {
					pool.returnPacket(packet.getData());
				}
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
			this.packet = packet;
		}

		@Override
		public void run() {
			try {
				multiplexedPacketHandler.handlePacket(packet);
				// packet에 대한 캐쉬를 해야 될듯.
				// packet.return(); 등등
			} finally {
				pool.returnPacket(packet.getData());
			}
		}
	}
}
