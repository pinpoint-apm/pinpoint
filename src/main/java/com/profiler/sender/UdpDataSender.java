package com.profiler.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.profiler.common.dto.Header;
import com.profiler.io.DefaultTBaseLocator;
import com.profiler.io.HeaderTBaseSerializer;
import com.profiler.io.TBaseLocator;
import com.profiler.context.Thriftable;
import com.profiler.util.Assert;

/**
 * @author netspider
 */
public class UdpDataSender implements DataSender, Runnable {

	private final Logger logger = Logger.getLogger(UdpDataSender.class.getName());

	private final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>(1024);

	private int maxDrainSize = 10;
	// 주의 single thread용임
	private List<Object> drain = new ArrayList<Object>(maxDrainSize);

	private DatagramSocket udpSocket = null;
	private Thread ioThread;

	private TBaseLocator locator = new DefaultTBaseLocator();
	// 주의 single thread용임
	private HeaderTBaseSerializer serializer = new HeaderTBaseSerializer();

	private AtomicBoolean allowInput = new AtomicBoolean();

	public UdpDataSender(String host, int port) {
		Assert.notNull(host, "host must not be null");

		// Socket 생성에 에러가 발생하면 Agent start가 안되게 변경.
		this.udpSocket = createSocket(host, port);

		this.allowInput.set(true);

		this.ioThread = createIoThread();

		logger.info("UdpDataSender initialized. host=" + host + ", port=" + port);
	}

	private Thread createIoThread() {
		Thread thread = new Thread(this);
		thread.setName("HIPPO-UdpDataSender-IoThread");
		thread.setDaemon(true);
		thread.start();
		return thread;
	}

	private DatagramSocket createSocket(String host, int port) {
		try {
			DatagramSocket datagramSocket = new DatagramSocket();
			datagramSocket.setSoTimeout(1000 * 5);

			InetSocketAddress serverAddress = new InetSocketAddress(host, port);
			datagramSocket.connect(serverAddress);
			return datagramSocket;
		} catch (SocketException e) {
			throw new IllegalStateException("DataramSocket create fail. Cause" + e.getMessage(), e);
		}
	}

	public boolean send(TBase<?, ?> data) {
		return putQueue(data);
	}

	public boolean send(Thriftable thriftable) {
		return putQueue(thriftable);
	}

	private boolean putQueue(Object data) {
		if (data == null) {
			logger.warning("putQueue(). data is null");
			return false;
		}
		if (!allowInput.get()) {
			return false;
		}
		boolean offer = queue.offer(data);
		if (!offer) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Drop data. queue is full. size:" + queue.size());
			}
		}
		return offer;
	}

	@Override
	public void stop() {
		allowInput.set(false);
		
		if (!isEmpty()) {
			logger.info("Wait 5 seconds. Flushing queued data." + queue.size());
		}
		
		try {
            ioThread.join(5000);
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt();
			logger.info("UdpDataSender stopped incompletely.");
		}
		
		logger.info("UdpDataSender stopped.");
	}

	public void run() {
		logger.info(Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ") started.");
		doSend();
	}

	private void doSend() {
		drain: while (true) {
			try {
				if (!allowInput.get() && isEmpty()) {
					break;
				}

				List<Object> dtoList = takeN();
				if (dtoList != null) {
					sendPacketN(dtoList);
					continue;
				}

				while (true) {
					if (!allowInput.get() && isEmpty()) {
						break;
					}

					Object dto = takeOne();
					if (dto != null) {
						sendPacket(dto);
						continue drain;
					}
				}
			} catch (Throwable th) {
				logger.log(Level.WARNING, "Unexpected Error. Cause:" + th.getMessage(), th);
			}
		}
	}

	private void sendPacketN(List<Object> dtoList) {
		for (Object dto : dtoList) {
			try {
				sendPacket(dto);
			} catch (Throwable th) {
				logger.log(Level.WARNING, "Unexpected Error. Cause:" + th.getMessage(), th);
			}
		}
	}

	private void sendPacket(Object dto) {
		TBase<?, ?> tBase;
		if (dto instanceof TBase) {
			tBase = (TBase<?, ?>) dto;
		} else if (dto instanceof Thriftable) {
			tBase = ((Thriftable) dto).toThrift();
		} else {
			logger.warning("sendPacket fail. invalid type:" + dto.getClass());
			return;
		}
        // TODO single thread이므로 데이터 array를 nocopy해서 보낼수 있음.
		byte[] sendData = serialize(tBase);
		if (sendData == null) {
			logger.warning("sendData is null");
			return;
		}
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length);
		try {
			udpSocket.send(packet);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Data sent. " + dto);
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "packet send error " + dto, e);
		}
	}

	private Object takeOne() {
		try {
			return queue.poll(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return null;
		}
	}

	private List<Object> takeN() {
		drain.clear();
		int size = queue.drainTo(drain, 10);
		if (size <= 0) {
			return null;
		}
		return drain;
	}

	private boolean isEmpty() {
		return queue.size() == 0;
	}

	private byte[] serialize(TBase<?, ?> dto) {
		try {
			Header header = headerLookup(dto);
			return serializer.serialize(header, dto);
		} catch (TException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Serialize fail:" + dto + " Caused:" + e.getMessage(), e);
			}
			return null;
		}
	}

	private Header headerLookup(TBase<?, ?> dto) throws TException {
		// header 객체 생성을 안하고 정적 lookup이 되도록 변경.
		return locator.headerLookup(dto);
	}
}
