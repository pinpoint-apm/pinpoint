package com.nhn.pinpoint.profiler.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.thrift.io.NetworkAvailabilityCheckPacket;

/**
 * @author netspider
 * @author emeroad
 * @author koo.taejin
 */
public class UdpDataSender extends AbstractDataSender implements DataSender {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isTrace = logger.isTraceEnabled();

    // 주의 single thread용임
    private DatagramPacket reusePacket = new DatagramPacket(new byte[1], 1);

	private final DatagramSocket udpSocket;

	// 주의 single thread용임
	private final HeaderTBaseSerializer serializer = new HeaderTBaseSerializerFactory(false, HeaderTBaseSerializerFactory.DEFAULT_UDP_STREAM_MAX_SIZE).createSerializer();

    private AsyncQueueingExecutor<Object> executor;

	public UdpDataSender(String host, int port, String threadName, int queueSize) {
        if (host == null ) {
            throw new NullPointerException("host must not be null");
        }
        if (threadName == null) {
            throw new NullPointerException("threadName must not be null");
        }
        if (queueSize <= 0) {
            throw new IllegalArgumentException("queueSize");
        }

        // Socket 생성에 에러가 발생하면 Agent start가 안되게 변경.
        logger.info("UdpDataSender initialized. host={}, port={}", host, port);
		this.udpSocket = createSocket(host, port);

		this.executor = createAsyncQueueingExecutor(queueSize, threadName);
	}
	
    @Override
	public boolean send(TBase<?, ?> data) {
		return executor.execute(data);
	}

    @Override
    public void stop() {
        executor.stop();
    }
    
    public boolean isNetworkAvalable() {
    	NetworkAvailabilityCheckPacket dto = new NetworkAvailabilityCheckPacket();
        try {
            byte[] interBufferData = serialize(serializer, dto);
        	int interBufferSize = serializer.getInterBufferSize();
            reusePacket.setData(interBufferData, 0, interBufferSize);
            udpSocket.send(reusePacket);
            
            if (isTrace) {
                logger.trace("Data sent. {}", dto);
            }
            
			byte[] receiveData = new byte[NetworkAvailabilityCheckPacket.DATA_OK.length];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			udpSocket.receive(receivePacket);
			
			if (isTrace) {
				logger.trace("Data received. {}", Arrays.toString(receivePacket.getData()));
			}
			
			return Arrays.equals(NetworkAvailabilityCheckPacket.DATA_OK , receiveData);
        } catch (IOException e) {
            logger.warn("packet send error {}", dto, e);
            return false;
        }
    }

    private DatagramSocket createSocket(String host, int port) {
		try {
            DatagramSocket datagramSocket = new DatagramSocket();

			datagramSocket.setSoTimeout(1000 * 5);
            datagramSocket.setSendBufferSize(1024 * 64 * 16);

			InetSocketAddress serverAddress = new InetSocketAddress(host, port);
			datagramSocket.connect(serverAddress);
			return datagramSocket;
		} catch (SocketException e) {
			throw new IllegalStateException("DataramSocket create fail. Cause" + e.getMessage(), e);
		}
	}

	protected void sendPacket(Object message) {
		if (message instanceof TBase) {
			TBase dto = (TBase) message;
            // single thread이므로 데이터 array를 nocopy해서 보냄.
            byte[] interBufferData = serialize(this.serializer, dto);
            if (interBufferData == null) {
                logger.warn("interBufferData is null");
                return;
            }

            final int interBufferSize = this.serializer.getInterBufferSize();
            // single thread이므로 그냥 재활용한다.
            reusePacket.setData(interBufferData, 0, interBufferSize);
            try {
                udpSocket.send(reusePacket);
                if (logger.isDebugEnabled()) {
                    logger.debug("Data sent. {}, {}", interBufferSize, dto);
                }
            } catch (IOException e) {
                logger.warn("packet send error {}, {}", interBufferSize, dto, e);
            }
		} else {
			logger.warn("sendPacket fail. invalid type:{}", message != null ? message.getClass() : null);
			return;
		}
	}
}
