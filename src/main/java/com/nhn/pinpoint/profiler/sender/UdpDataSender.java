package com.nhn.pinpoint.profiler.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collection;

import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.nhn.pinpoint.profiler.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.profiler.context.Thriftable;

/**
 * @author netspider
 */
public class UdpDataSender extends AbstractQueueingDataSender implements DataSender {

	private final Logger logger = LoggerFactory.getLogger(UdpDataSender.class.getName());
    private final boolean isTrace = logger.isTraceEnabled();

    // 주의 single thread용임
    private DatagramPacket reusePacket = new DatagramPacket(new byte[1], 1);

	private final DatagramSocket udpSocket;

	// 주의 single thread용임
	private HeaderTBaseSerializer serializer = new HeaderTBaseSerializer();


	public UdpDataSender(String host, int port) {
        super(1024, "UdpDataSender");

        if (host == null ) {
            throw new NullPointerException("host must not be null");
        }
		// Socket 생성에 에러가 발생하면 Agent start가 안되게 변경.
        logger.info("UdpDataSender initialized. host={}, port={}", host, port);
		this.udpSocket = createSocket(host, port);
	}


	private DatagramSocket createSocket(String host, int port) {
		try {
            DatagramSocket datagramSocket = new DatagramSocket();

			datagramSocket.setSoTimeout(1000 * 5);
            datagramSocket.setSendBufferSize(1024 * 64);

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


    protected void sendPacketN(Collection<Object> dtoList) {
        Object[] dataList = dtoList.toArray();
//        for (Object data : dataList) {
//        이렇게 바꾸지 말것. copy해서 return 하는게 아니라 항상 max치가 나옴.
        final int size = dtoList.size();
        for (int i = 0; i < size; i++) {
			try {
				sendPacket(dataList[i]);
			} catch (Throwable th) {
				logger.warn("Unexpected Error. Cause:" + th.getMessage(), th);
			}
		}
	}


	protected void sendPacket(Object dto) {
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
			if (isTrace) {
				logger.trace("Data sent. {}", dto);
			}
		} catch (IOException e) {
			logger.warn("packet send error {}", dto, e);
		}
	}



	private byte[] serialize(TBase<?, ?> dto) {
		try {
			return serializer.serialize(dto);
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

}
