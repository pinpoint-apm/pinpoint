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

package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.packet.PacketType;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreateFailPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPingPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.util.IDGenerator;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author koo.taejin
 */
public class StreamChannelManager {

    private static final LoggingStreamChannelStateChangeEventHandler LOGGING_STATE_CHANGE_HANDLER = new LoggingStreamChannelStateChangeEventHandler();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Channel channel;

    private final IDGenerator idGenerator;

    private final ServerStreamChannelMessageListener streamChannelMessageListener;

    private final StreamChannelRepository streamChannelRepository = new StreamChannelRepository();

    public StreamChannelManager(Channel channel, IDGenerator idGenerator) {
        this(channel, idGenerator, DisabledServerStreamChannelMessageListener.INSTANCE);
    }

    public StreamChannelManager(Channel channel, IDGenerator idGenerator, ServerStreamChannelMessageListener serverStreamChannelMessageListener) {
        Assert.requireNonNull(channel, "Channel must not be null.");
        Assert.requireNonNull(idGenerator, "IDGenerator must not be null.");
        Assert.requireNonNull(serverStreamChannelMessageListener, "ServerStreamChannelMessageListener must not be null.");

        this.channel = channel;
        this.idGenerator = idGenerator;
        this.streamChannelMessageListener = serverStreamChannelMessageListener;
    }

    public void close() {
        Set<Integer> keySet = streamChannelRepository.getStreamIdSet();

        for (Integer key : keySet) {
            StreamChannel unregister = streamChannelRepository.unregister(key);
            if (unregister != null) {
                unregister.close(StreamCode.STATE_CLOSED);
            }
        }
    }

    public ClientStreamChannel openStream(byte[] payload, ClientStreamChannelMessageListener messageListener) throws StreamException {
        return openStream(payload, messageListener, LOGGING_STATE_CHANGE_HANDLER);
    }

    public ClientStreamChannel openStream(byte[] payload, ClientStreamChannelMessageListener messageListener, StreamChannelStateChangeEventHandler<ClientStreamChannel> stateChangeListener) throws StreamException {
        logger.info("Open streamChannel initialization started. Channel:{} ", channel);

        final int streamChannelId = idGenerator.generate();

        ClientStreamChannel newStreamChannel = new ClientStreamChannel(channel, streamChannelId, streamChannelRepository, messageListener);
        if (stateChangeListener != null) {
            newStreamChannel.addStateChangeEventHandler(stateChangeListener);
        }

        newStreamChannel.changeStateOpen();

        streamChannelRepository.registerIfAbsent(newStreamChannel);

        // the order of below code is very important.
        newStreamChannel.changeStateConnectAwait();
        newStreamChannel.sendCreate(payload);

        boolean connected = newStreamChannel.awaitOpen(3000);
        if (connected) {
            logger.info("Open streamChannel initialization completed. Channel:{}, streamChannel:{} ", channel, newStreamChannel);
        } else {
            newStreamChannel.close(StreamCode.CONNECTION_TIMEOUT);
            throw new StreamException(StreamCode.CONNECTION_TIMEOUT);
        }
        return newStreamChannel;
    }

    public void messageReceived(StreamPacket packet) {
        final int streamChannelId = packet.getStreamChannelId();
        final short packetType = packet.getPacketType();

        logger.debug("StreamChannel message received. (Channel:{}, StreamId:{}, Packet:{}).", channel, streamChannelId, packet);

        if (PacketType.APPLICATION_STREAM_CREATE == packetType) {
            handleCreate((StreamCreatePacket) packet);
            return;
        }

        StreamChannel streamChannel = streamChannelRepository.getStreamChannel(streamChannelId);
        if (streamChannel == null) {
            if (!(PacketType.APPLICATION_STREAM_CLOSE == packetType)) {
                streamChannel.close(StreamCode.ID_NOT_FOUND);
            }
        } else {
            if (streamChannel instanceof ServerStreamChannel) {
                messageReceived((ServerStreamChannel) streamChannel, packet);
            } else if (streamChannel instanceof ClientStreamChannel) {
                messageReceived((ClientStreamChannel) streamChannel, packet);
            } else {
                streamChannel.close(StreamCode.UNKNWON_ERROR);
            }
        }
    }

    private void messageReceived(ServerStreamChannel serverStreamChannel, StreamPacket packet) {
        final short packetType = packet.getPacketType();
        final int streamChannelId = packet.getStreamChannelId();

        switch (packetType) {
            case PacketType.APPLICATION_STREAM_CLOSE:
                handleStreamClose(serverStreamChannel, (StreamClosePacket) packet);
                break;
            case PacketType.APPLICATION_STREAM_PING:
                handlePing(serverStreamChannel, (StreamPingPacket) packet);
                break;
            case PacketType.APPLICATION_STREAM_PONG:
                // handlePong((StreamPongPacket) packet);
                break;
            default:
                serverStreamChannel.close(StreamCode.PACKET_UNKNOWN);
                logger.info("Unknown StreamPacket received Channel:{}, StreamId:{}, Packet;{}.", channel, streamChannelId, packet);
        }
    }

    private void messageReceived(ClientStreamChannel clientStreamChannel, StreamPacket packet) {
        final short packetType = packet.getPacketType();
        final int streamChannelId = packet.getStreamChannelId();

        switch (packetType) {
            case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
                clientStreamChannel.changeStateConnected();
                break;
            case PacketType.APPLICATION_STREAM_CREATE_FAIL:
                clientStreamChannel.disconnect(((StreamCreateFailPacket) packet).getCode());
                break;
            case PacketType.APPLICATION_STREAM_RESPONSE:
                handleStreamResponse(clientStreamChannel, (StreamResponsePacket) packet);
                break;
            case PacketType.APPLICATION_STREAM_CLOSE:
                handleStreamClose(clientStreamChannel, (StreamClosePacket) packet);
                break;
            case PacketType.APPLICATION_STREAM_PING:
                handlePing(clientStreamChannel, (StreamPingPacket) packet);
                break;
            case PacketType.APPLICATION_STREAM_PONG:
                // handlePong((StreamPongPacket) packet);
                break;
            default:
                clientStreamChannel.close(StreamCode.PACKET_UNKNOWN);
                logger.info("Unknown StreamPacket received Channel:{}, StreamId:{}, Packet;{}.", channel, streamChannelId, packet);
        }
    }

    private void handleCreate(StreamCreatePacket packet) {
        final int streamChannelId = packet.getStreamChannelId();

        ServerStreamChannel streamChannel = new ServerStreamChannel(this.channel, streamChannelId, streamChannelRepository, streamChannelMessageListener);

        StreamCode streamCode = null;
        try {
            registerStreamChannel(streamChannel);
            streamCode = streamChannel.handleStreamCreate(packet);

            if (streamCode == StreamCode.OK) {
                streamChannel.changeStateConnected();
                streamChannel.sendCreateSuccess();
            }
        } catch (StreamException e) {
            streamCode = e.getStreamCode();
        }

        if (streamCode != StreamCode.OK) {
            streamChannel.close(new StreamCreateFailPacket(streamChannelId, streamCode));
        }
    }

    private StreamCode registerStreamChannel(ServerStreamChannel streamChannel) throws StreamException {
        streamChannel.changeStateOpen();

        streamChannelRepository.registerIfAbsent(streamChannel);

        if (!streamChannel.changeStateConnectArrived()) {
            throw new StreamException(StreamCode.STATE_ERROR);
        }

        return StreamCode.OK;
    }

    private void handleStreamResponse(ClientStreamChannel clientStreamChannel, StreamResponsePacket packet) {
        StreamChannelStateCode currentCode = clientStreamChannel.getCurrentState();

        if (StreamChannelStateCode.CONNECTED == currentCode) {
            clientStreamChannel.handleStreamData(packet);
        } else if (StreamChannelStateCode.CONNECT_AWAIT == currentCode) {
            // may happen in the timing
        } else {
            clientStreamChannel.close(StreamCode.STATE_NOT_CONNECTED);
        }
    }

    private void handleStreamClose(StreamChannel clientStreamChannel, StreamClosePacket packet) {
        clientStreamChannel.handleStreamClose(packet);
        clientStreamChannel.disconnect(packet.getCode());
    }

    private void handlePing(StreamChannel streamChannel, StreamPingPacket packet) {
        try {
            streamChannel.sendPong(packet.getRequestId());
        } catch (PinpointSocketException e) {
            streamChannel.close(StreamCode.STATE_NOT_CONNECTED);
        }
    }

    public StreamChannel findStreamChannel(int channelId) {
        return streamChannelRepository.getStreamChannel(channelId);
    }

}
