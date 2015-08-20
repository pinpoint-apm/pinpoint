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

package com.navercorp.pinpoint.collector.cluster;

import javax.annotation.PreDestroy;

import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import org.apache.thrift.TBase;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.collector.cluster.route.DefaultRouteHandler;
import com.navercorp.pinpoint.collector.cluster.route.RequestEvent;
import com.navercorp.pinpoint.collector.cluster.route.StreamEvent;
import com.navercorp.pinpoint.collector.cluster.route.StreamRouteHandler;
import com.navercorp.pinpoint.rpc.client.MessageListener;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageListener;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;

/**
 * @author koo.taejin
 * @author HyunGil Jeong
 */
public class ClusterPointRouter implements MessageListener, ServerStreamChannelMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClusterPointRepository<TargetClusterPoint> targetClusterPointRepository;

    private final DefaultRouteHandler routeHandler;
    private final StreamRouteHandler streamRouteHandler;

    @Autowired
    private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    @Autowired
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    public ClusterPointRouter(ClusterPointRepository<TargetClusterPoint> targetClusterPointRepository,
            DefaultRouteHandler defaultRouteHandler, StreamRouteHandler streamRouteHandler) {
        if (targetClusterPointRepository == null) {
            throw new NullPointerException("targetClusterPointRepository may not be null");
        }
        if (defaultRouteHandler == null) {
            throw new NullPointerException("defaultRouteHandler may not be null");
        }
        if (streamRouteHandler == null) {
            throw new NullPointerException("streamRouteHandler may not be null");
        }
        this.targetClusterPointRepository = targetClusterPointRepository;
        this.routeHandler = defaultRouteHandler;
        this.streamRouteHandler = streamRouteHandler;
    }

    @PreDestroy
    public void stop() {
    }

    @Override
    public void handleSend(SendPacket packet, Channel channel) {
        logger.info("Message received {}. channel:{}, packet:{}.", packet.getClass().getSimpleName(), channel, packet);
    }

    @Override
    public void handleRequest(RequestPacket packet, Channel channel) {
        logger.info("Message received {}. channel:{}, packet:{}.", packet.getClass().getSimpleName(), channel, packet);

        TBase<?, ?> request = deserialize(packet.getPayload());
        if (request == null) {
            handleRouteRequestFail("Protocol decoding failed.", packet, channel);
        } else if (request instanceof TCommandTransfer) {
            handleRouteRequest((TCommandTransfer)request, packet, channel);
        } else {
            handleRouteRequestFail("Unknown error.", packet, channel);
        }
    }

    @Override
    public short handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet) {
        logger.info("Message received {}. streamChannel:{}, packet:{}.", packet.getClass().getSimpleName(), streamChannelContext, packet);

        TBase<?, ?> request = deserialize(packet.getPayload());
        if (request == null) {
            return (short) TRouteResult.EMPTY_REQUEST.getValue();
        } else if (request instanceof TCommandTransfer) {
            return (short) handleStreamRouteCreate((TCommandTransfer)request, packet, streamChannelContext).getValue();
        } else {
            return (short) TRouteResult.UNKNOWN.getValue();
        }
    }

    @Override
    public void handleStreamClose(ServerStreamChannelContext streamChannelContext, StreamClosePacket packet) {
        logger.info("Message received {}. streamChannel:{}, packet:{}.", packet.getClass().getSimpleName(), streamChannelContext, packet);

        streamRouteHandler.close(streamChannelContext);
    }

    private boolean handleRouteRequest(TCommandTransfer request, RequestPacket requestPacket, Channel channel) {
        byte[] payload = ((TCommandTransfer)request).getPayload();
        TBase<?,?> command = deserialize(payload);

        TCommandTransferResponse response = routeHandler.onRoute(new RequestEvent((TCommandTransfer) request, channel, requestPacket.getRequestId(), command));
        channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(response)));

        return response.getRouteResult() == TRouteResult.OK;
    }

    private void handleRouteRequestFail(String message, RequestPacket requestPacket, Channel channel) {
        TResult tResult = new TResult(false);
        tResult.setMessage(message);

        channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(tResult)));
    }

    private TRouteResult handleStreamRouteCreate(TCommandTransfer request, StreamCreatePacket packet, ServerStreamChannelContext streamChannelContext) {
        byte[] payload = ((TCommandTransfer)request).getPayload();
        TBase<?,?> command = deserialize(payload);

        TCommandTransferResponse response = streamRouteHandler.onRoute(new StreamEvent((TCommandTransfer) request, streamChannelContext, command));
        return response.getRouteResult();
    }

    public ClusterPointRepository<TargetClusterPoint> getTargetClusterPointRepository() {
        return targetClusterPointRepository;
    }

    private byte[] serialize(TBase<?,?> result) {
        return SerializationUtils.serialize(result, commandSerializerFactory, null);
    }

    private TBase<?,?> deserialize(byte[] objectData) {
        return SerializationUtils.deserialize(objectData, commandDeserializerFactory, null);
    }

}
