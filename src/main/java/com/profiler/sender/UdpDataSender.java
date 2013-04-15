package com.profiler.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.profiler.common.dto.Header;
import com.profiler.io.DefaultTBaseLocator;
import com.profiler.io.HeaderTBaseSerializer;
import com.profiler.io.TBaseLocator;
import com.profiler.context.Thriftable;

/**
 * @author netspider
 */
public class UdpDataSender implements DataSender, Runnable {

	private final Logger logger = LoggerFactory.getLogger(UdpDataSender.class.getName());

	private final LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>(1024);

	private final int maxDrainSize = 10;
	// 주의 single thread용임. ArrayList보다 더 단순한 오퍼레이션을 수행하는 Collection.
	private Collection<Object> drain = new UnsafeArrayCollection<Object>(maxDrainSize);
    // 주의 single thread용임
    private DatagramPacket reusePacket = new DatagramPacket(new byte[1], 1);

	private DatagramSocket udpSocket = null;
	private Thread ioThread;

	private TBaseLocator locator = new DefaultTBaseLocator();
	// 주의 single thread용임
	private HeaderTBaseSerializer serializer = new HeaderTBaseSerializer();


	private AtomicBoolean allowInput = new AtomicBoolean();

	public UdpDataSender(String host, int port) {
		if (host == null ) {
            throw new NullPointerException("host must not be null");
        }

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
			logger.warn("putQueue(). data is null");
			return false;
		}
		if (!allowInput.get()) {
			return false;
		}
		boolean offer = queue.offer(data);
		if (!offer) {
			if (logger.isWarnEnabled()) {
				logger.warn("Drop data. queue is full. size:{}", queue.size());
			}
		}
		return offer;
	}

	@Override
	public void stop() {
		allowInput.set(false);
		
		if (!isEmpty()) {
			logger.info("Wait 5 seconds. Flushing queued data.");
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
        Thread thread = Thread.currentThread();
        logger.info("{}(\"{}\") started.", thread.getName(), thread.getId());
		doSend();
	}
	private void doSend() {
        drain: while (true) {
			try {
				if (isShutdown()) {
					break;
				}


				Collection<Object> dtoList = takeN();
				if (dtoList != null) {
					sendPacketN(dtoList);
					continue;
				}

				while (true) {
					if (isShutdown()) {
						break;
					}

					Object dto = takeOne();
					if (dto != null) {
						sendPacket(dto);
						continue drain;
					}
				}
			} catch (Throwable th) {
				logger.warn("UdpSenderLoop->Unexpected Error. Cause:{}", th.getMessage(), th);
			}
		}
        flushQueue();
	}

    private void flushQueue() {
        logger.debug("UdpSenderLoop is stop. flushData");
        while(true) {
            Collection<Object> flushData = takeN();
            if(flushData == null) {
                break;
            }
            logger.debug("flushData {}", flushData.size());
            sendPacket(flushData);
        }
    }

    private boolean isShutdown() {
        return !allowInput.get();
    }

    private void sendPacketN(Collection<Object> dtoList) {
        Object[] dataList = dtoList.toArray();
        for (int i = 0; i< dtoList.size(); i++) {
			try {
				sendPacket(dataList[i]);
			} catch (Throwable th) {
				logger.warn("Unexpected Error. Cause:" + th.getMessage(), th);
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
			logger.warn("sendPacket fail. invalid type:{}", dto.getClass());
			return;
		}
        // single thread이므로 데이터 array를 nocopy해서 보냄.
		byte[] interBufferData = serialize(tBase);
        int interBufferSize = serializer.getInterBufferSize();
        if (interBufferData == null) {
			logger.warn("interBufferData is null");
			return;
		}
        // single thread이므로 그냥 재활용한다.
		reusePacket.setData(interBufferData, 0, interBufferSize);
		try {
			udpSocket.send(reusePacket);
			if (logger.isInfoEnabled()) {
				logger.info("Data sent. {}", dto);
			}
		} catch (IOException e) {
			logger.warn("packet send error {}", dto, e);
		}
	}

	private Object takeOne() {
		try {
			return queue.poll(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
            // Thread.currentThread().interrupt();
            // 인터럽트 한번은 그냥 무시한다.


            return null;
		}
	}

	private Collection<Object> takeN() {
		drain.clear();
		int size = queue.drainTo(drain, maxDrainSize);
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
			if (logger.isWarnEnabled()) {
                logger.warn("Serialize fail:{} Caused:{}", new Object[] { dto, e.getMessage(), e});
			}
			return null;
		}
	}

    private int beforeSerializeLength() {
        return serializer.getInterBufferSize();
    }

	private Header headerLookup(TBase<?, ?> dto) throws TException {
		// header 객체 생성을 안하고 정적 lookup이 되도록 변경.
		return locator.headerLookup(dto);
	}
}
