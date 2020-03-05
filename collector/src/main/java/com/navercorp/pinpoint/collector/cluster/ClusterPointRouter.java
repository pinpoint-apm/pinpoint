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

package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.cluster.route.DefaultRouteHandler;
import com.navercorp.pinpoint.collector.cluster.route.RequestEvent;
import com.navercorp.pinpoint.collector.cluster.route.StreamEvent;
import com.navercorp.pinpoint.collector.cluster.route.StreamRouteHandler;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageHandler;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PreDestroy;
import java.util.Objects;

/**
 * @author koo.taejin
 * @author HyunGil Jeong
 */
public class ClusterPointRouter extends ServerStreamChannelMessageHandler implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClusterPointRepository<ClusterPoint> targetClusterPointRepository;

    private final DefaultRouteHandler routeHandler;
    private final StreamRouteHandler streamRouteHandler;

    @Autowired
    @Qualifier("commandHeaderTBaseSerializerFactory")
    private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    @Autowired
    @Qualifier("commandHeaderTBaseDeserializerFactory")
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    public ClusterPointRouter(ClusterPointRepository<ClusterPoint> targetClusterPointRepository,
            DefaultRouteHandler defaultRouteHandler, StreamRouteHandler streamRouteHandler) {
        this.targetClusterPointRepository = Objects.requireNonNull(targetClusterPointRepository, "targetClusterPointRepository");
        this.routeHandler = Objects.requireNonNull(defaultRouteHandler, "defaultRouteHandler");
        this.streamRouteHandler = Objects.requireNonNull(streamRouteHandler, "streamRouteHandler");
    }

    @PreDestroy
    public void stop() {
    }

    @Override
    public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
        logger.info("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
    }

    @Override
    public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        logger.info("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());

        TBase<?, ?> request = deserialize(requestPacket.getPayload());
        if (request == null) {
            handleRouteRequestFail("Protocol decoding failed.", requestPacket, pinpointSocket);
        } else if (request instanceof TCommandTransfer) {
            handleRouteRequest((TCommandTransfer)request, requestPacket, pinpointSocket);
        } else {
            handleRouteRequestFail("Unknown error.", requestPacket, pinpointSocket);
        }
    }

    @Override
    public StreamCode handleStreamCreatePacket(ServerStreamChannel streamChannel, StreamCreatePacket packet) {
        logger.info("handleStreamCreatePacket() streamChannel:{}, packet:{}", streamChannel, packet);

        TBase<?, ?> request = deserialize(packet.getPayload());
        if (request == null) {
            return StreamCode.TYPE_UNKNOWN;
        } else if (request instanceof TCommandTransfer) {
            return handleStreamRouteCreate((TCommandTransfer)request, packet, streamChannel);
        } else {
            return StreamCode.TYPE_UNSUPPORT;
        }
    }

    @Override
    public void handleStreamClosePacket(ServerStreamChannel streamChannel, StreamClosePacket packet) {
        logger.info("handleStreamClosePacket() streamChannel:{}, packet:{}", streamChannel, packet);

        streamRouteHandler.close(streamChannel);
    }

    private boolean handleRouteRequest(TCommandTransfer request, RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        byte[] payload = request.getPayload();
        TBase<?,?> command = deserialize(payload);

        RequestEvent event = new RequestEvent(request, pinpointSocket.getRemoteAddress(), requestPacket.getRequestId(), command);
        TCommandTransferResponse response = routeHandler.onRoute(event);
        pinpointSocket.response(requestPacket.getRequestId(), serialize(response));

        return response.getRouteResult() == TRouteResult.OK;
    }

    private void handleRouteRequestFail(String message, RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        TResult tResult = new TResult(false);
        tResult.setMessage(message);

        pinpointSocket.response(requestPacket.getRequestId(), serialize(tResult));
    }

    private StreamCode handleStreamRouteCreate(TCommandTransfer request, StreamCreatePacket packet, ServerStreamChannel serverStreamChannel) {
        byte[] payload = request.getPayload();
        TBase<?,?> command = deserialize(payload);
        if (command == null) {
            return StreamCode.TYPE_UNKNOWN;
        }

        TCommandTransferResponse response = streamRouteHandler.onRoute(new StreamEvent(request, serverStreamChannel, command));
        TRouteResult routeResult = response.getRouteResult();
        if (routeResult != TRouteResult.OK) {
            logger.warn("handleStreamRouteCreate failed. command:{}, routeResult:{}", command, routeResult);
            return convertToStreamCode(routeResult);
        }

        return StreamCode.OK;
    }

    public ClusterPointRepository<ClusterPoint> getTargetClusterPointRepository() {
        return targetClusterPointRepository;
    }

    private byte[] serialize(TBase<?,?> result) {
        return SerializationUtils.serialize(result, commandSerializerFactory, null);
    }

    private TBase<?,?> deserialize(byte[] objectData) {
        final Message<TBase<?, ?>> deserialize = SerializationUtils.deserialize(objectData, commandDeserializerFactory, null);
        if (deserialize == null) {
            return null;
        }
        return deserialize.getData();
    }

    private StreamCode convertToStreamCode(TRouteResult routeResult) {
        switch (routeResult) {
            case NOT_SUPPORTED_REQUEST:
                return StreamCode.TYPE_UNSUPPORT;
            case NOT_ACCEPTABLE:
            case NOT_SUPPORTED_SERVICE:
                return StreamCode.CONNECTION_UNSUPPORT;
            default:
                return StreamCode.ROUTE_ERROR;
        }
    }

}
