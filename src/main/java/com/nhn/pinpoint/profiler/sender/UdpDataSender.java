package com.nhn.pinpoint.profiler.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collection;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author netspider
 */
public class UdpDataSender implements DataSender {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isTrace = logger.isTraceEnabled();

    // 주의 single thread용임
    private DatagramPacket reusePacket = new DatagramPacket(new byte[1], 1);

	private final DatagramSocket udpSocket;

	// 주의 single thread용임
	private HeaderTBaseSerializer serializer = new HeaderTBaseSerializer();

    private AsyncQueueingExecutor<TBase<?, ?>> executor;

	public UdpDataSender(String host, int port, String threadName) {
        if (host == null ) {
            throw new NullPointerException("host must not be null");
        }
        if (threadName == null) {
            throw new NullPointerException("threadName must not be null");
        }


        // Socket 생성에 에러가 발생하면 Agent start가 안되게 변경.
        logger.info("UdpDataSender initialized. host={}, port={}", host, port);
		this.udpSocket = createSocket(host, port);

        this.executor = getExecutor(threadName);
	}

    private AsyncQueueingExecutor<TBase<?, ?>> getExecutor(String senderName) {
        final AsyncQueueingExecutor<TBase<?, ?>> executor = new AsyncQueueingExecutor<TBase<?, ?>>(1024 * 5, senderName);
        executor.setListener(new AsyncQueueingExecutorListener<TBase<?, ?>>() {
            @Override
            public void execute(Collection<TBase<?, ?>> dtoList) {
                sendPacketN(dtoList);
            }

            @Override
            public void execute(TBase<?, ?> dto) {
                sendPacket(dto);
            }
        });
        return executor;
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

    @Override
	public boolean send(TBase<?, ?> data) {
		return executor.execute(data);
	}


    @Override
    public void stop() {
        executor.stop();
    }




    protected void sendPacketN(Collection<TBase<?, ?>> dtoList) {
        Object[] dataList = dtoList.toArray();
//        for (Object data : dataList) {
//        이렇게 바꾸지 말것. copy해서 return 하는게 아니라 항상 max치가 나옴.
        final int size = dtoList.size();
        for (int i = 0; i < size; i++) {
			try {
				sendPacket((TBase<?, ?>)dataList[i]);
			} catch (Throwable th) {
				logger.warn("Unexpected Error. Cause:" + th.getMessage(), th);
			}
		}
	}


	protected void sendPacket(TBase<?, ?> dto) {
        if (dto instanceof TBase) {
            final TBase<?, ?> tBase = (TBase<?, ?>) dto;
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
		} else {
			logger.warn("sendPacket fail. invalid type:{}", dto != null ? dto.getClass() : null);
			return;
		}

	}



	private byte[] serialize(TBase<?, ?> dto) {
		try {
			return serializer.serialize(dto);
		} catch (TException e) {
			if (logger.isWarnEnabled()) {
                logger.warn("Serialize fail:{} Caused:{}", dto, e.getMessage(), e);
			}
			return null;
		}
	}

}
