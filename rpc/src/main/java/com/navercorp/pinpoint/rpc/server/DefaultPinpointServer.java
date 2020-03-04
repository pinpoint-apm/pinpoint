/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.rpc.ChannelWriteFailListenableFuture;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.RequestManager;
import com.navercorp.pinpoint.rpc.client.WriteFailFutureListener;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.cluster.Role;
import com.navercorp.pinpoint.rpc.common.CyclicStateChecker;
import com.navercorp.pinpoint.rpc.common.SocketStateChangeResult;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.Packet;
import com.navercorp.pinpoint.rpc.packet.PacketType;
import com.navercorp.pinpoint.rpc.packet.PingPacket;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.PongPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.packet.ServerClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPacket;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelManager;
import com.navercorp.pinpoint.rpc.stream.StreamException;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.IDGenerator;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Taejin Koo
 */
public class DefaultPinpointServer implements PinpointServer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final long startTimestamp = System.currentTimeMillis();

    private final Channel channel;
    private final RequestManager requestManager;

    private final DefaultPinpointServerState state;
    private final CyclicStateChecker stateChecker;

    private HealthCheckStateContext healthCheckStateContext = new HealthCheckStateContext();

    private final ServerMessageListener messageListener;

    private final List<ServerStateChangeEventHandler> stateChangeEventListeners;

    private final StreamChannelManager streamChannelManager;

    private final AtomicReference<Map<Object, Object>> properties = new AtomicReference<Map<Object, Object>>();

    private final String objectUniqName;

    private final ClusterOption localClusterOption;
    private ClusterOption remoteClusterOption;

    private final ChannelFutureListener serverCloseWriteListener;
    private final ChannelFutureListener responseWriteFailListener;

    private final WriteFailFutureListener pongWriteFutureListener = new WriteFailFutureListener(logger, "pong write fail.", "pong write success.");


    public DefaultPinpointServer(Channel channel, PinpointServerConfig serverConfig) {
        this(channel, serverConfig, null);
    }

    public DefaultPinpointServer(Channel channel, PinpointServerConfig serverConfig, ServerStateChangeEventHandler... stateChangeEventListeners) {
        this.channel = channel;

        this.messageListener = serverConfig.getMessageListener();

        StreamChannelManager streamChannelManager = new StreamChannelManager(channel, IDGenerator.createEvenIdGenerator(), serverConfig.getServerStreamMessageHandler());
        this.streamChannelManager = streamChannelManager;

        this.stateChangeEventListeners = new ArrayList<ServerStateChangeEventHandler>();
        List<ServerStateChangeEventHandler> configuredStateChangeEventHandlers = serverConfig.getStateChangeEventHandlers();
        if (configuredStateChangeEventHandlers != null) {
            for (ServerStateChangeEventHandler configuredStateChangeEventHandler : configuredStateChangeEventHandlers) {
                ListUtils.addIfValueNotNull(this.stateChangeEventListeners, configuredStateChangeEventHandler);
            }
        }
        ListUtils.addAllExceptNullValue(this.stateChangeEventListeners, stateChangeEventListeners);
        if (this.stateChangeEventListeners.isEmpty()) {
            this.stateChangeEventListeners.add(ServerStateChangeEventHandler.DISABLED_INSTANCE);
        }

        RequestManager requestManager = new RequestManager(serverConfig.getRequestManagerTimer(), serverConfig.getDefaultRequestTimeout());
        this.requestManager = requestManager;

        
        this.objectUniqName = ClassUtils.simpleClassNameAndHashCodeString(this);
        
        this.serverCloseWriteListener = new WriteFailFutureListener(logger, objectUniqName + " sendClosePacket() write fail.", "serverClosePacket write success");
        this.responseWriteFailListener = new WriteFailFutureListener(logger, objectUniqName + " response() write fail.");

        this.state = new DefaultPinpointServerState(this, this.stateChangeEventListeners);
        this.stateChecker = new CyclicStateChecker(5);

        this.localClusterOption = serverConfig.getClusterOption();
    }
    
    public void start() {
        logger.info("{} start() started. channel:{}.", objectUniqName, channel);
        
        state.toConnected();
        state.toRunWithoutHandshake();
        
        logger.info("{} start() completed.", objectUniqName);
    }
    
    public void stop() {
        logger.info("{} stop() started. channel:{}.", objectUniqName, channel);

        stop(false);
        
        logger.info("{} stop() completed.", objectUniqName);
    }
    
    public void stop(boolean serverStop) {
        try {
            SocketStateCode currentStateCode = getCurrentStateCode();
            if (SocketStateCode.BEING_CLOSE_BY_SERVER == currentStateCode) {
                state.toClosed();
            } else if (SocketStateCode.BEING_CLOSE_BY_CLIENT == currentStateCode) {
                state.toClosedByPeer();
            } else if (SocketStateCode.isRun(currentStateCode) && serverStop) {
                state.toUnexpectedClosed();
            } else if (SocketStateCode.isRun(currentStateCode)) {
                state.toUnexpectedClosedByPeer();
            } else if (SocketStateCode.isClosed(currentStateCode)) {
                logger.warn("{} stop(). Socket has closed state({}).", objectUniqName, currentStateCode);
            } else {
                state.toErrorUnknown();
                logger.warn("{} stop(). Socket has unexpected state.", objectUniqName, currentStateCode);
            }

            if (this.channel.isConnected()) {
                channel.close();
            }
        } finally {
            streamChannelManager.close();
        }
    }

    @Override
    public void send(byte[] payload) {
        Assert.requireNonNull(payload, "payload");
        if (!isEnableDuplexCommunication()) {
            throw new IllegalStateException("Send fail. Error: Illegal State. pinpointServer:" + toString());
        }
        
        SendPacket send = new SendPacket(payload);
        write0(send);
    }

    @Override
    public Future<ResponseMessage> request(byte[] payload) {
        Assert.requireNonNull(payload, "payload");
        if (!isEnableDuplexCommunication()) {
            throw new IllegalStateException("Request fail. Error: Illegal State. pinpointServer:" + toString());
        }

        final int requestId = this.requestManager.nextRequestId();
        RequestPacket requestPacket = new RequestPacket(requestId, payload);
        ChannelWriteFailListenableFuture<ResponseMessage> responseFuture = this.requestManager.register(requestPacket.getRequestId());
        write0(requestPacket, responseFuture);
        return responseFuture;
    }

    @Override
    public void response(int requestId, byte[] payload) {
        Assert.requireNonNull(payload, "payload");
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
            future.addListener(futureListener);
        }
        return future;
    }

    @Override
    public ClientStreamChannel openStream(byte[] payload, ClientStreamChannelEventHandler streamChannelEventHandler) throws StreamException {
        logger.info("{} createStream() started.", objectUniqName);

        ClientStreamChannel streamChannel = streamChannelManager.openStream(payload, streamChannelEventHandler);

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
        
        SocketStateChangeResult stateChangeResult = state.toBeingClose();
        if (stateChangeResult.isChange()) {
            ChannelFuture writeFuture = write0(ServerClosePacket.DEFAULT_SERVER_CLOSE_PACKET, serverCloseWriteListener);
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
            case PacketType.CONTROL_PING_PAYLOAD: {
                handlePingPacket(channel, (PingPayloadPacket) message);
                return;
            }
            case PacketType.CONTROL_PING: {
                handlePingPacket(channel, (PingPacket) message);
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

    private void handleHandshake(ControlHandshakePacket handshakePacket) {
        int requestId = handshakePacket.getRequestId();
        Map<Object, Object> handshakeData = decodeHandshakePacket(handshakePacket);

        logger.info("{} handleHandshake() started. requestId:{}, data:{}", objectUniqName, requestId, handshakeData);

        HandshakeResponseCode responseCode = messageListener.handleHandshake(handshakeData);
        if (responseCode != null) {
            boolean isFirst = setChannelProperties(handshakeData);
            if (isFirst) {
                if (HandshakeResponseCode.DUPLEX_COMMUNICATION == responseCode) {
                    this.remoteClusterOption = getClusterOption(handshakeData);
                    state.toRunDuplex();
                } else if (HandshakeResponseCode.SIMPLEX_COMMUNICATION == responseCode || HandshakeResponseCode.SUCCESS == responseCode) {
                    state.toRunSimplex();
                }
            }

            Map<String, Object> responseData = createHandshakeResponse(responseCode, isFirst);
            sendHandshakeResponse0(requestId, responseData);

            logger.info("{} handleHandshake() completed(isFirst:{}). requestId:{}, responseCode:{}", objectUniqName, isFirst, requestId, responseCode);
        } else {
            logger.info("{} to execute handleHandshake() is not ready", objectUniqName);
        }
    }

    private ClusterOption getClusterOption(Map handshakeResponse) {
        if (handshakeResponse == Collections.emptyMap()) {
            return ClusterOption.DISABLE_CLUSTER_OPTION;
        }

        Map cluster = (Map) handshakeResponse.get(ControlHandshakeResponsePacket.CLUSTER);
        if (cluster == null) {
            return ClusterOption.DISABLE_CLUSTER_OPTION;
        }

        String id = MapUtils.getString(cluster, "id", "");
        List<Role> roles = getRoles((List) cluster.get("roles"));

        if (StringUtils.isEmpty(id)) {
            return ClusterOption.DISABLE_CLUSTER_OPTION;
        } else {
            return new ClusterOption(true, id, roles);
        }
    }

    private List<Role> getRoles(List roleNames) {
        List<Role> roles = new ArrayList<Role>();
        for (Object roleName : roleNames) {
            if (roleName instanceof String && StringUtils.hasLength((String) roleName)) {
                roles.add(Role.getValue((String) roleName));
            }
        }
        return roles;
    }

    private void handleClosePacket(Channel channel) {
        logger.info("{} handleClosePacket() started.", objectUniqName);
        
        SocketStateChangeResult stateChangeResult = state.toBeingCloseByPeer();
        if (!stateChangeResult.isChange()) {
            logger.info("{} handleClosePacket() failed. Error: {}", objectUniqName, stateChangeResult);
        } else {
            logger.info("{} handleClosePacket() completed.", objectUniqName);
        }
    }
    
    private void handlePingPacket(Channel channel, PingPacket packet) {
        logger.debug("{} handleLegacyPingPacket() started. packet:{}", objectUniqName, packet);

        if (healthCheckStateContext.getState() == HealthCheckState.WAIT) {
            healthCheckStateContext.toReceivedLegacy();
        }

        // packet without status value
        if (packet == PingPacket.PING_PACKET) {
            writePong(channel);
            return;
        }

        PingPayloadPacket pingPayloadPacket = new PingPayloadPacket(packet.getPingId(), packet.getStateVersion(), packet.getStateCode());
        handlePingPacket0(channel, pingPayloadPacket);
    }

    private void handlePingPacket(Channel channel, PingPayloadPacket packet) {
        logger.debug("{} handlePingPacket() started. packet:{}", objectUniqName, packet);

        if (healthCheckStateContext.getState() == HealthCheckState.WAIT) {
            healthCheckStateContext.toReceived();
        }

        handlePingPacket0(channel, packet);
    }

    private void handlePingPacket0(Channel channel, PingPayloadPacket packet) {
        SocketStateCode statusCode = state.getCurrentStateCode();

        if (statusCode.getId() == packet.getStateCode()) {
            stateChecker.unmark();

            messageListener.handlePing(packet, this);

            writePong(channel);
        } else {
            logger.warn("Session state sync failed. channel:{}, packet:{}, server-state:{}", channel, packet, statusCode);

            if (stateChecker.markAndCheckCondition()) {
                state.toErrorSyncStateSession();
                stop();
            } else {
                writePong(channel);
            }
        }
    }

    private void writePong(Channel channel) {
        write0(PongPacket.PONG_PACKET, pongWriteFutureListener);
    }

    private Map<String, Object> createHandshakeResponse(HandshakeResponseCode responseCode, boolean isFirst) {
        final HandshakeResponseCode createdCode = getHandshakeResponseCode(responseCode, isFirst);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put(ControlHandshakeResponsePacket.CODE, createdCode.getCode());
        result.put(ControlHandshakeResponsePacket.SUB_CODE, createdCode.getSubCode());
        if (localClusterOption.isEnable()) {
            Map<String, Object> clusterOption = localClusterOption.toMap();
            result.put(ControlHandshakeResponsePacket.CLUSTER, clusterOption);
        }

        return result;
    }

    private HandshakeResponseCode getHandshakeResponseCode(HandshakeResponseCode responseCode, boolean isFirst) {
        if (isFirst) {
            return responseCode;
        }
        if (HandshakeResponseCode.DUPLEX_COMMUNICATION == responseCode) {
            return HandshakeResponseCode.ALREADY_DUPLEX_COMMUNICATION;
        } else if (HandshakeResponseCode.SIMPLEX_COMMUNICATION == responseCode) {
            return HandshakeResponseCode.ALREADY_SIMPLEX_COMMUNICATION;
        }

        return responseCode;
    }

    private void sendHandshakeResponse0(int requestId, Map<String, Object> data) {
        try {
            byte[] resultPayload = ControlMessageEncodingUtils.encode(data);
            ControlHandshakeResponsePacket packet = new ControlHandshakeResponsePacket(requestId, resultPayload);
            write0(packet);
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

        return Collections.emptyMap();
    }

    public boolean isEnableCommunication() {
        return state.isEnableCommunication();
    }
    
    public boolean isEnableDuplexCommunication() {
        return state.isEnableDuplexCommunication();
    }

    String getObjectUniqName() {
        return objectUniqName;
    }

    @Override
    public ClusterOption getLocalClusterOption() {
        return localClusterOption;
    }

    @Override
    public ClusterOption getRemoteClusterOption() {
        return remoteClusterOption;
    }

    @Override
    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public HealthCheckState getHealthCheckState() {
        return healthCheckStateContext.getState();
    }

    @Override
    public SocketStateCode getCurrentStateCode() {
        return state.getCurrentStateCode();
    }

    @Override
    public void close() {
        stop();
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
        log.append(", healthCheckState:");
        log.append(getHealthCheckState());
        log.append(")");
        
        return log.toString();
    }
    
}
