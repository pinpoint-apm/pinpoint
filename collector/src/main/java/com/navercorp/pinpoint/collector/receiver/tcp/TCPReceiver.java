/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.tcp;

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

import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperClusterService;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.util.PacketUtils;
import com.navercorp.pinpoint.common.util.ExecutorFactory;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.Header;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.L4Packet;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class TCPReceiver {

    private final Logger logger = LoggerFactory.getLogger(TCPReceiver.class);

    private final ThreadFactory THREAD_FACTORY = new PinpointThreadFactory("Pinpoint-TCP-Worker");
    private final PinpointServerAcceptor serverAcceptor;
    private final DispatchHandler dispatchHandler;
    private final String bindAddress;
    private final int port;

    private int threadSize = 256;
    private int workerQueueSize = 1024 * 5;

    @Value("#{(pinpoint_collector_properties['collector.l4.ip']).split(',')}")
    private List<String> l4ipList;

    private final ThreadPoolExecutor worker = ExecutorFactory.newFixedThreadPool(threadSize, workerQueueSize, THREAD_FACTORY);

    private final SerializerFactory<HeaderTBaseSerializer> serializerFactory = new ThreadLocalHeaderTBaseSerializerFactory<HeaderTBaseSerializer>(new HeaderTBaseSerializerFactory(true, HeaderTBaseSerializerFactory.DEFAULT_UDP_STREAM_MAX_SIZE));

    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new ThreadLocalHeaderTBaseDeserializerFactory<HeaderTBaseDeserializer>(new HeaderTBaseDeserializerFactory());


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
        
        if (service == null || !service.isEnable()) {
            this.serverAcceptor = new PinpointServerAcceptor();
        } else {
            this.serverAcceptor = new PinpointServerAcceptor();
            this.serverAcceptor.setStateChangeEventHandler(service.getChannelStateChangeEventHandler());
        }
        
        this.dispatchHandler = dispatchHandler;
        this.bindAddress = bindAddress;
        this.port = port;
    }

    private void setL4TcpChannel(PinpointServerAcceptor serverFactory) {
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
            serverFactory.setIgnoreAddressList(inetAddressList.toArray(inetAddressArray));
        } catch (UnknownHostException e) {
            logger.warn("l4ipList error {}", l4ipList, e);
        }
    }

    @PostConstruct
    public void start() {
        setL4TcpChannel(serverAcceptor);
        // take care when attaching message handlers as events are generated from the IO thread.
        // pass them to a separate queue and handle them in a different thread.
        this.serverAcceptor.setMessageListener(new ServerMessageListener() {
            @Override
            public void handleSend(SendPacket sendPacket, PinpointServer pinpointServer) {
                receive(sendPacket, pinpointServer);
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, PinpointServer pinpointServer) {
                requestResponse(requestPacket, pinpointServer);
            }

            @Override
            public HandshakeResponseCode handleHandshake(Map properties) {
                if (properties == null) {
                    return HandshakeResponseType.ProtocolError.PROTOCOL_ERROR;
                }

                boolean hasAllType = AgentHandshakePropertyType.hasAllType(properties);
                if (!hasAllType) {
                    return HandshakeResponseType.PropertyError.PROPERTY_ERROR;
                }

                boolean supportServer = MapUtils.getBoolean(properties, AgentHandshakePropertyType.SUPPORT_SERVER.getName(), true);
                if (supportServer) {
                    return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
                } else {
                    return HandshakeResponseType.Success.SIMPLEX_COMMUNICATION;
                }
            }
        });
        this.serverAcceptor.bind(bindAddress, port);


    }

    private void receive(SendPacket sendPacket, PinpointServer pinpointServer) {
        try {
            worker.execute(new Dispatch(sendPacket.getPayload(), pinpointServer.getRemoteAddress()));
        } catch (RejectedExecutionException e) {
            // cause is clear - full stack trace not necessary 
            logger.warn("RejectedExecutionException Caused:{}", e.getMessage());
        }
    }

    private void requestResponse(RequestPacket requestPacket, PinpointServer pinpointServer) {
        try {
            worker.execute(new RequestResponseDispatch(requestPacket, pinpointServer));
        } catch (RejectedExecutionException e) {
            // cause is clear - full stack trace not necessary
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
                TBase<?, ?> tBase = SerializationUtils.deserialize(bytes, deserializerFactory);
                dispatchHandler.dispatchSendMessage(tBase);
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", remoteAddress, e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(bytes));
                }
            } catch (Exception e) {
                // there are cases where invalid headers are received
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
        private final PinpointServer pinpointServer;


        private RequestResponseDispatch(RequestPacket requestPacket, PinpointServer pinpointServer) {
            if (requestPacket == null) {
                throw new NullPointerException("requestPacket");
            }
            this.requestPacket = requestPacket;
            this.pinpointServer = pinpointServer;
        }

        @Override
        public void run() {

            byte[] bytes = requestPacket.getPayload();
            SocketAddress remoteAddress = pinpointServer.getRemoteAddress();
            try {
                TBase<?, ?> tBase = SerializationUtils.deserialize(bytes, deserializerFactory);
                if (tBase instanceof L4Packet) {
                    if (logger.isDebugEnabled()) {
                        L4Packet packet = (L4Packet) tBase;
                        logger.debug("tcp l4 packet {}", packet.getHeader());
                    }
                    return;
                }
                TBase result = dispatchHandler.dispatchRequestMessage(tBase);
                if (result != null) {
                    byte[] resultBytes = SerializationUtils.serialize(result, serializerFactory);
                    pinpointServer.response(requestPacket, resultBytes);
                }
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", remoteAddress, e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(bytes));
                }
            } catch (Exception e) {
                // there are cases where invalid headers are received
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
        serverAcceptor.close();
        worker.shutdown();
        try {
            worker.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
