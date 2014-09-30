package com.nhn.pinpoint.collector.receiver.tcp;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nhn.pinpoint.collector.cluster.zookeeper.ZookeeperClusterService;
import com.nhn.pinpoint.collector.receiver.DispatchHandler;
import com.nhn.pinpoint.collector.util.PacketUtils;
import com.nhn.pinpoint.common.util.ExecutorFactory;
import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.rpc.packet.ControlEnableWorkerConfirmPacket;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.StreamPacket;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.ServerStreamChannel;
import com.nhn.pinpoint.rpc.server.SocketChannel;
import com.nhn.pinpoint.thrift.io.DeserializerFactory;
import com.nhn.pinpoint.thrift.io.Header;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.nhn.pinpoint.thrift.io.L4Packet;
import com.nhn.pinpoint.thrift.io.SerializerFactory;
import com.nhn.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import com.nhn.pinpoint.thrift.io.ThreadLocalHeaderTBaseSerializerFactory;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class TCPReceiver {

	private final Logger logger = LoggerFactory.getLogger(TCPReceiver.class);

    private final ThreadFactory THREAD_FACTORY = new PinpointThreadFactory("Pinpoint-TCP-Worker");
	private final PinpointServerSocket pinpointServerSocket;
    private final DispatchHandler dispatchHandler;
    private final String bindAddress;
    private final int port;

    private int threadSize = 256;
    private int workerQueueSize = 1024 * 5;

    @Value("#{(pinpoint_collector_properties['collector.l4.ip']).split(',')}")
    private List<String> l4ipList;

    private final ThreadPoolExecutor worker = ExecutorFactory.newFixedThreadPool(threadSize, workerQueueSize, THREAD_FACTORY);

    private final SerializerFactory serializerFactory = new ThreadLocalHeaderTBaseSerializerFactory(new HeaderTBaseSerializerFactory(true, HeaderTBaseSerializerFactory.DEFAULT_UDP_STREAM_MAX_SIZE));

    private final DeserializerFactory deserializerFactory = new ThreadLocalHeaderTBaseDeserializerFactory(new HeaderTBaseDeserializerFactory());


    public TCPReceiver(DispatchHandler dispatchHandler, String bindAddress, int port) {
    	this(dispatchHandler, bindAddress, port, null);
    }

    public TCPReceiver(DispatchHandler dispatchHandler, String bindAddress, int port, ZookeeperClusterService service) {
        if (dispatchHandler == null) {
            throw new NullPointerException("dispatchHandler must not be null");
        }
        if (bindAddress == null) {
            throw new NullPointerException("bindAddress must not be null");
        }
        
        if (service == null) {
        	this.pinpointServerSocket = new PinpointServerSocket();
        } else {
        	this.pinpointServerSocket = new PinpointServerSocket(service.getChannelStateChangeEventListener());
        }
        
        this.dispatchHandler = dispatchHandler;
        this.bindAddress = bindAddress;
        this.port = port;
	}

    private void setL4TcpChannel(PinpointServerSocket pinpointServerSocket) {
        if (l4ipList == null) {
            return;
        }
        try {
        	List<InetAddress> inetAddressList = new ArrayList<InetAddress>();
        	for (int i = 0; i < l4ipList.size(); i++) {
        		String l4Ip = l4ipList.get(i);
        		if (StringUtils.isBlank(l4Ip)) {
        			continue;
        		}
        		
        		InetAddress address = InetAddress.getByName(l4Ip);
        		if (address != null) {
        			inetAddressList.add(address);
        		}
            }
            
        	InetAddress[] inetAddressArray = new InetAddress[inetAddressList.size()];
        	pinpointServerSocket.setIgnoreAddressList(inetAddressList.toArray(inetAddressArray));
        } catch (UnknownHostException e) {
            logger.warn("l4ipList error {}", l4ipList, e);
        }
    }

    @PostConstruct
	public void start() {
        setL4TcpChannel(pinpointServerSocket);
        // message handler를 붙일 경우 주의점
        // iothread에서 올라오는 이벤트 이기 때문에. queue에 넣던가. 별도 thread처리등을 해야 한다.
        this.pinpointServerSocket.setMessageListener(new ServerMessageListener() {
            @Override
            public void handleSend(SendPacket sendPacket, SocketChannel channel) {
                receive(sendPacket, channel);
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
                requestResponse(requestPacket, channel);
            }

            @Override
            public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
                logger.warn("unsupported streamPacket received {}", streamPacket);
            }
            
            @Override
            public int handleEnableWorker(Map properties) {
    			if (properties == null) {
    				return ControlEnableWorkerConfirmPacket.ILLEGAL_PROTOCOL;
    			}
    			
    			boolean hasAllType = AgentPropertiesType.hasAllType(properties);
    			if (!hasAllType) {
    				return ControlEnableWorkerConfirmPacket.INVALID_PROPERTIES;
    			}

    			return ControlEnableWorkerConfirmPacket.SUCCESS;
            }
        });
        this.pinpointServerSocket.bind(bindAddress, port);


	}

    private void receive(SendPacket sendPacket, SocketChannel channel) {
        try {
            worker.execute(new Dispatch(sendPacket.getPayload(), channel.getRemoteAddress()));
        } catch (RejectedExecutionException e) {
            // 이건 stack trace찍어 봤자임. 원인이 명확함. 어떤 메시지 에러인지 좀더 알기 쉽게 찍을 필요성이 있음.
            logger.warn("RejectedExecutionException Caused:{}", e.getMessage());
        }
    }

    private void requestResponse(RequestPacket requestPacket, SocketChannel channel) {
        try {
            worker.execute(new RequestResponseDispatch(requestPacket, channel));
        } catch (RejectedExecutionException e) {
            // 이건 stack trace찍어 봤자임. 원인이 명확함. 어떤 메시지 에러인지 좀더 알기 쉽게 찍을 필요성이 있음.
            logger.warn("RejectedExecutionException Caused:{}", e.getMessage());
        }
    }

    private class Dispatch implements Runnable {
        private final byte[] bytes;
        private final SocketAddress remoteAddress;


        private Dispatch(byte[] bytes, SocketAddress remoteAddress) {
            if (bytes == null) {
                throw new NullPointerException("bytes");
            }
            this.bytes = bytes;
            this.remoteAddress = remoteAddress;
        }

        @Override
        public void run() {
            try {
                final HeaderTBaseDeserializer deserializer = deserializerFactory.createDeserializer();
                TBase<?, ?> tBase = deserializer.deserialize(bytes);
                dispatchHandler.dispatchSendMessage(tBase, bytes, Header.HEADER_SIZE, bytes.length);
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", remoteAddress, e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(bytes));
                }
            } catch (Exception e) {
                // 잘못된 header가 도착할 경우 발생하는 케이스가 있음.
                if (logger.isWarnEnabled()) {
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{}", remoteAddress, e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(bytes));
                }
            }
        }
    }

    private class RequestResponseDispatch implements Runnable {
        private final RequestPacket requestPacket;
        private final SocketChannel socketChannel;


        private RequestResponseDispatch(RequestPacket requestPacket, SocketChannel socketChannel) {
            if (requestPacket == null) {
                throw new NullPointerException("requestPacket");
            }
            this.requestPacket = requestPacket;
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {

            byte[] bytes = requestPacket.getPayload();
            SocketAddress remoteAddress = socketChannel.getRemoteAddress();
            try {
                final HeaderTBaseDeserializer deserializer = deserializerFactory.createDeserializer();
                TBase<?, ?> tBase = deserializer.deserialize(bytes);
                if (tBase instanceof L4Packet) {
                    // 동적으로 패스가 가능하도록 보완해야 될듯 하다.
                    if (logger.isDebugEnabled()) {
                        L4Packet packet = (L4Packet) tBase;
                        logger.debug("tcp l4 packet {}", packet.getHeader());
                    }
                    return;
                }
                TBase result = dispatchHandler.dispatchRequestMessage(tBase, bytes, Header.HEADER_SIZE, bytes.length);
                if (result != null) {
                    HeaderTBaseSerializer serializer = serializerFactory.createSerializer();
                    byte[] resultBytes = serializer.serialize(result);
                    socketChannel.sendResponseMessage(requestPacket, resultBytes);
                }
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", remoteAddress, e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(bytes));
                }
            } catch (Exception e) {
                // 잘못된 header가 도착할 경우 발생하는 케이스가 있음.
                if (logger.isWarnEnabled()) {
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{}", remoteAddress, e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(bytes));
                }
            }
        }
    }

    @PreDestroy
    public void stop() {
        logger.info("Pinpoint-TCP-Server stop");
        pinpointServerSocket.close();
        worker.shutdown();
        try {
            worker.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
