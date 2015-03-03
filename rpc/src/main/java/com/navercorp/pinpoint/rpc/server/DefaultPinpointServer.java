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

package com.navercorp.pinpoint.rpc.server;

import java.lang.reflect.Array;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.ChannelWriteFailListenableFuture;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.RequestManager;
import com.navercorp.pinpoint.rpc.client.WriteFailFutureListener;
import com.navercorp.pinpoint.rpc.common.SocketState;
import com.navercorp.pinpoint.rpc.common.SocketStateChangeResult;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.Packet;
import com.navercorp.pinpoint.rpc.packet.PacketType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.packet.ServerClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPacket;
import com.navercorp.pinpoint.rpc.server.handler.ChannelStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.StreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.StreamChannelManager;
import com.navercorp.pinpoint.rpc.util.AssertUtils;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.IDGenerator;
import com.navercorp.pinpoint.rpc.util.ListUtils;

/**
 * @author Taejin Koo
 */
public class DefaultPinpointServer implements PinpointServer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Channel channel;
    private final RequestManager requestManager;

    private final SocketState state;

    private final ServerMessageListener messageListener;

    private final List<ChannelStateChangeEventHandler> stateChangeEventListeners;

    private final StreamChannelManager streamChannelManager;

    private final AtomicReference<Map<Object, Object>> properties = new AtomicReference<Map<Object, Object>>();

    private final String objectUniqName;
    
    private final ChannelFutureListener serverCloseWriteListener;
    private final ChannelFutureListener responseWriteFailListener;
    
    public DefaultPinpointServer(Channel channel, PinpointServerConfig serverConfig) {
        this(channel, serverConfig, null);
    }

    public DefaultPinpointServer(Channel channel, PinpointServerConfig serverConfig, ChannelStateChangeEventHandler... stateChangeEventListeners) {
        this.channel = channel;

        this.messageListener = serverConfig.getMessageListener();

        StreamChannelManager streamChannelManager = new StreamChannelManager(channel, IDGenerator.createEvenIdGenerator(), serverConfig.getStreamMessageListener());
        this.streamChannelManager = streamChannelManager;

        if (stateChangeEventListeners == null) {
            this.stateChangeEventListeners = new ArrayList<ChannelStateChangeEventHandler>(1);
        } else {
            this.stateChangeEventListeners = new ArrayList<ChannelStateChangeEventHandler>(Array.getLength(stateChangeEventListeners) + 1);
        }
        
        ListUtils.addIfValueNotNull(this.stateChangeEventListeners, serverConfig.getStateChangeEventHandler());
        ListUtils.addAllExceptNullValue(this.stateChangeEventListeners, stateChangeEventListeners);

        RequestManager requestManager = new RequestManager(serverConfig.getRequestManagerTimer(), serverConfig.getDefaultRequestTimeout());
        this.requestManager = requestManager;

        this.state = new SocketState();
        
        this.objectUniqName = ClassUtils.simpleClassNameAndHashCodeString(this);
        
        this.serverCloseWriteListener = new WriteFailFutureListener(logger, objectUniqName + " sendClosePacket() write fail.", "serverClosePacket write success");
        this.responseWriteFailListener = new WriteFailFutureListener(logger, objectUniqName + " response() write fail.");
    }
    
    public void start() {
        logger.info("{} start() started. channel:{}.", objectUniqName, channel);
        
        stateToConnected();
        stateToRunWithoutHandshake();
        
        logger.info("{} start() completed.", objectUniqName);
    }
    
    public void stop() {
        logger.info("{} stop() started. channel:{}.", objectUniqName, channel);

        stop(false);
        
        logger.info("{} stop() completed.", objectUniqName);
    }
    
    public void stop(boolean serverStop) {
        SocketStateCode currentStateCode = state.getCurrentState();
        if (SocketStateCode.BEING_CLOSE_BY_SERVER == currentStateCode) {
            stateToClosed();
        } else if (SocketStateCode.BEING_CLOSE_BY_CLIENT == currentStateCode) {
            stateToClosedByPeer();
        } else if (SocketStateCode.isRun(currentStateCode) && serverStop) {
            stateToUnexpectedClosed();
        } else if (SocketStateCode.isRun(currentStateCode) ) {
            stateToUnexpectedClosedByPeer();
        } else if (SocketStateCode.isClosed(currentStateCode)){
            logger.warn("{} stop(). Socket has closed state({}).", objectUniqName, currentStateCode);
        } else {
            stateToErrorUnknown();
            logger.warn("{} stop(). Socket has unexpected state.", objectUniqName, currentStateCode);
        }
        
        if (this.channel.isConnected()) {
            channel.close();
        }
        
        streamChannelManager.close();
    }

    @Override
    public void send(byte[] payload) {
        AssertUtils.assertNotNull(payload, "payload may not be null.");
        if (!isEnableDuplexCommunication()) {
            throw new IllegalStateException("Send fail. Error: Illegal State. pinpointServer:" + toString());
        }
        
        SendPacket send = new SendPacket(payload);
        write0(send);
    }

    @Override
    public Future request(byte[] payload) {
        AssertUtils.assertNotNull(payload, "payload may not be null.");
        if (!isEnableDuplexCommunication()) {
            throw new IllegalStateException("Request fail. Error: Illegal State. pinpointServer:" + toString());
        }

        RequestPacket requestPacket = new RequestPacket(payload);
        ChannelWriteFailListenableFuture<ResponseMessage> messageFuture = this.requestManager.register(requestPacket);
        write0(requestPacket, messageFuture);
        return messageFuture;
    }

    @Override
    public void response(RequestPacket requestPacket, byte[] payload) {
        response(requestPacket.getRequestId(), payload);
    }

    @Override
    public void response(int requestId, byte[] payload) {
        AssertUtils.assertNotNull(payload, "payload may not be null.");
        if (!isEnableCommunication()) {
            throw new IllegalStateException("Response fail. Error: Illegal State. pinpointServer:" + toString());
        }

        ResponsePacket responsePacket = new ResponsePacket(requestId, payload);
        write0(responsePacket, responseWriteFailListener);
    }
    
    private ChannelFuture write0(Object message) {
        return write0(message, null);
    }

    private ChannelFuture write0(Object message, ChannelFutureListener futureListener) {
        ChannelFuture future = channel.write(message);
        if (futureListener != null) {
            future.addListener(futureListener);;
        }
        return future;
    }

    public StreamChannelContext getStreamChannel(int channelId) {
        return streamChannelManager.findStreamChannel(channelId);
    }

    @Override
    public ClientStreamChannelContext createStream(byte[] payload, ClientStreamChannelMessageListener clientStreamChannelMessageListener) {
        logger.info("{} createStream() started.", objectUniqName);

        ClientStreamChannelContext streamChannel = streamChannelManager.openStreamChannel(payload, clientStreamChannelMessageListener);
        
        logger.info("{} createStream() completed.", objectUniqName);
        return streamChannel;
    }

    public void closeAllStreamChannel() {
        logger.info("{} closeAllStreamChannel() started.", objectUniqName);

        streamChannelManager.close();

        logger.info("{} closeAllStreamChannel() completed.", objectUniqName);
    }
    
    @Override
    public Map<Object, Object> getChannelProperties() {
        Map<Object, Object> properties = this.properties.get();
        return properties == null ? Collections.emptyMap() : properties;
    }

    public boolean setChannelProperties(Map<Object, Object> value) {
        if (value == null) {
            return false;
        }

        return this.properties.compareAndSet(null, Collections.unmodifiableMap(value));
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
    }

    public ChannelFuture sendClosePacket() {
        logger.info("{} sendClosePacket() started.", objectUniqName);
        
        SocketStateChangeResult stateChangeResult = stateToBeingClose();
        if (stateChangeResult.isChange()) {
            final ChannelFuture writeFuture = this.channel.write(ServerClosePacket.DEFAULT_SERVER_CLOSE_PACKET);
            writeFuture.addListener(serverCloseWriteListener);

            logger.info("{} sendClosePacket() completed.", objectUniqName);
            return writeFuture;
        } else {
            logger.info("{} sendClosePacket() failed. Error:{}.", objectUniqName, stateChangeResult);
            return null;
        }
    }

    @Override
    public void messageReceived(Object message) {
        if (!isEnableCommunication()) {
            // FIXME need change rules.
            // as-is : do nothing when state is not run.
            // candidate : close channel when state is not run.
            logger.warn("{} messageReceived() failed. Error: Illegal state this message({}) will be ignore.", objectUniqName, message);
            return;
        }
        
        final short packetType = getPacketType(message);
        switch (packetType) {
            case PacketType.APPLICATION_SEND: {
                handleSend((SendPacket) message);
                return;
            }
            case PacketType.APPLICATION_REQUEST: {
                handleRequest((RequestPacket) message);
                return;
            }
            case PacketType.APPLICATION_RESPONSE: {
                handleResponse((ResponsePacket) message);
                return;
            }
            case PacketType.APPLICATION_STREAM_CREATE:
            case PacketType.APPLICATION_STREAM_CLOSE:
            case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
            case PacketType.APPLICATION_STREAM_CREATE_FAIL:
            case PacketType.APPLICATION_STREAM_RESPONSE:
            case PacketType.APPLICATION_STREAM_PING:
            case PacketType.APPLICATION_STREAM_PONG:
                handleStreamEvent((StreamPacket) message);
                return;
            case PacketType.CONTROL_HANDSHAKE:
                handleHandshake((ControlHandshakePacket) message);
                return;
            case PacketType.CONTROL_CLIENT_CLOSE: {
                handleClosePacket(channel);
                return;
            }
            default: {
                logger.warn("invalid messageReceived msg:{}, connection:{}", message, channel);
            }
        }
    }

    private short getPacketType(Object packet) {
        if (packet == null) {
            return PacketType.UNKNOWN;
        }

        if (packet instanceof Packet) {
            return ((Packet) packet).getPacketType();
        }

        return PacketType.UNKNOWN;
    }

    private void handleSend(SendPacket sendPacket) {
        messageListener.handleSend(sendPacket, this);
    }

    private void handleRequest(RequestPacket requestPacket) {
        messageListener.handleRequest(requestPacket, this);
    }

    private void handleResponse(ResponsePacket responsePacket) {
        this.requestManager.messageReceived(responsePacket, this);
    }

    private void handleStreamEvent(StreamPacket streamPacket) {
        streamChannelManager.messageReceived(streamPacket);
    }

    private void handleHandshake(ControlHandshakePacket handshakepacket) {
        logger.info("{} handleHandshake() started. Packet:{}", objectUniqName, handshakepacket);
        
        int requestId = handshakepacket.getRequestId();
        Map<Object, Object> handshakeData = decodeHandshakePacket(handshakepacket);
        HandshakeResponseCode responseCode = messageListener.handleHandshake(handshakeData);
        boolean isFirst = setChannelProperties(handshakeData);
        if (isFirst) {
            if (HandshakeResponseCode.DUPLEX_COMMUNICATION == responseCode) {
                stateToRunDuplex();
            } else if (HandshakeResponseCode.SIMPLEX_COMMUNICATION == responseCode) {
                stateToRunSimplex();
            }
        }

        logger.info("{} handleHandshake(). ResponseCode:{}", objectUniqName, responseCode);

        Map<String, Object> responseData = createHandshakeResponse(responseCode, isFirst);
        sendHandshakeResponse0(requestId, responseData);
        
        logger.info("{} handleHandshake() completed.", objectUniqName);
    }

    private void handleClosePacket(Channel channel) {
        logger.info("{} handleClosePacket() started.", objectUniqName);
        
        SocketStateChangeResult stateChangeResult = stateToBeingCloseByPeer();
        if (!stateChangeResult.isChange()) {
            logger.info("{} handleClosePacket() failed. Error: {}", objectUniqName, stateChangeResult);
        } else {
            logger.info("{} handleClosePacket() completed.", objectUniqName);
        }
    }

    private Map<String, Object> createHandshakeResponse(HandshakeResponseCode responseCode, boolean isFirst) {
        HandshakeResponseCode createdCode = null;
        if (isFirst) {
            createdCode = responseCode;
        } else {
            if (HandshakeResponseCode.DUPLEX_COMMUNICATION == responseCode) {
                createdCode = HandshakeResponseCode.ALREADY_DUPLEX_COMMUNICATION;
            } else if (HandshakeResponseCode.SIMPLEX_COMMUNICATION == responseCode) {
                createdCode = HandshakeResponseCode.ALREADY_SIMPLEX_COMMUNICATION;
            } else {
                createdCode = responseCode;
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put(ControlHandshakeResponsePacket.CODE, createdCode.getCode());
        result.put(ControlHandshakeResponsePacket.SUB_CODE, createdCode.getSubCode());

        return result;
    }

    private void sendHandshakeResponse0(int requestId, Map<String, Object> data) {
        try {
            byte[] resultPayload = ControlMessageEncodingUtils.encode(data);
            ControlHandshakeResponsePacket packet = new ControlHandshakeResponsePacket(requestId, resultPayload);

            channel.write(packet);
        } catch (ProtocolException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private Map<Object, Object> decodeHandshakePacket(ControlHandshakePacket message) {
        try {
            byte[] payload = message.getPayload();
            Map<Object, Object> properties = (Map) ControlMessageEncodingUtils.decode(payload);
            return properties;
        } catch (ProtocolException e) {
            logger.warn(e.getMessage(), e);
        }

        return null;
    }
    
    @Override
    public SocketStateCode getCurrentStateCode() {
        return state.getCurrentState();
    }

    private SocketStateChangeResult stateToConnected() {
        SocketStateCode nextState = SocketStateCode.CONNECTED;
        return stateTo(nextState);
    }
    
    private SocketStateChangeResult stateToRunWithoutHandshake() {
        SocketStateCode nextState = SocketStateCode.RUN_WITHOUT_HANDSHAKE;
        return stateTo(nextState);
    }

    private SocketStateChangeResult stateToRunSimplex() {
        SocketStateCode nextState = SocketStateCode.RUN_SIMPLEX;
        return stateTo(nextState);
    }
    
    private SocketStateChangeResult stateToRunDuplex() {
        SocketStateCode nextState = SocketStateCode.RUN_DUPLEX;
        return stateTo(nextState);
    }

    private SocketStateChangeResult stateToBeingClose() {
        SocketStateCode nextState = SocketStateCode.BEING_CLOSE_BY_SERVER;
        return stateTo(nextState);
    }
    
    private SocketStateChangeResult stateToBeingCloseByPeer() {
        SocketStateCode nextState = SocketStateCode.BEING_CLOSE_BY_CLIENT;
        return stateTo(nextState);
    }
    
    private SocketStateChangeResult stateToClosed() {
        SocketStateCode nextState = SocketStateCode.CLOSED_BY_SERVER;
        return stateTo(nextState);
    }
    
    private SocketStateChangeResult stateToClosedByPeer() {
        SocketStateCode nextState = SocketStateCode.CLOSED_BY_CLIENT;
        return stateTo(nextState);
    }
    
    private SocketStateChangeResult stateToUnexpectedClosed() {
        SocketStateCode nextState = SocketStateCode.UNEXPECTED_CLOSE_BY_SERVER;
        return stateTo(nextState);
    }
    
    private SocketStateChangeResult stateToUnexpectedClosedByPeer() {
        SocketStateCode nextState = SocketStateCode.UNEXPECTED_CLOSE_BY_CLIENT;
        return stateTo(nextState);
    }
    
    private SocketStateChangeResult stateToErrorUnknown() {
        SocketStateCode nextState = SocketStateCode.ERROR_UNKOWN;
        return stateTo(nextState);
    }

    private SocketStateChangeResult stateTo(SocketStateCode nextState) {
        logger.debug("{} stateTo() started. to:{}", objectUniqName, nextState);

        SocketStateChangeResult stateChangeResult = state.changeState(nextState);
        if (stateChangeResult.isChange()) {
            executeChangeEventHandler(this, nextState);
        }

        logger.info("{} stateTo() completed. {}", objectUniqName, stateChangeResult);

        return stateChangeResult;
    }
    
    private void executeChangeEventHandler(DefaultPinpointServer pinpointServer, SocketStateCode nextState) {
         for (ChannelStateChangeEventHandler eachListener : this.stateChangeEventListeners) {
             try {
                 eachListener.eventPerformed(this, nextState);
             } catch (Exception e) {
                 eachListener.exceptionCaught(this, nextState, e);
             }
         }
    }
    
    public boolean isEnableCommunication() {
        return SocketStateCode.isRun(getCurrentStateCode());
    }
    
    public boolean isEnableDuplexCommunication() {
        return SocketStateCode.isRunDuplex(getCurrentStateCode());
    }

    @Override
    public String toString() {
        StringBuilder log = new StringBuilder(32);
        log.append(objectUniqName);
        log.append("(");
        log.append("remote:");
        log.append(getRemoteAddress());
        log.append(", state:");
        log.append(getCurrentStateCode());
        log.append(")");
        
        return log.toString();
    }
    
}
