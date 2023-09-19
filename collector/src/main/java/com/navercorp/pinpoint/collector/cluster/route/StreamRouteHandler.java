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

package com.navercorp.pinpoint.collector.cluster.route;

import com.navercorp.pinpoint.collector.cluster.ClusterPoint;
import com.navercorp.pinpoint.collector.cluster.ClusterPointLocator;
import com.navercorp.pinpoint.collector.cluster.GrpcAgentConnection;
import com.navercorp.pinpoint.collector.cluster.route.filter.RouteFilter;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannel;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.rpc.stream.StreamException;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Objects;

/**
 * @author koo.taejin
 */
public class StreamRouteHandler extends AbstractRouteHandler<StreamEvent> {

    public static final String ATTACHMENT_KEY = StreamRouteManager.class.getSimpleName();

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final RouteFilterChain<StreamEvent> streamCreateFilterChain;
    private final RouteFilterChain<ResponseEvent> responseFilterChain;
    private final RouteFilterChain<StreamRouteCloseEvent> streamCloseFilterChain;

    private final SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    public StreamRouteHandler(ClusterPointLocator<ClusterPoint<?>> targetClusterPointLocator,
                              RouteFilterChain<StreamEvent> streamCreateFilterChain,
                              RouteFilterChain<ResponseEvent> responseFilterChain,
                              RouteFilterChain<StreamRouteCloseEvent> streamCloseFilterChain,
                              @Qualifier("commandHeaderTBaseSerializerFactory") SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory) {
        super(targetClusterPointLocator);

        this.streamCreateFilterChain = streamCreateFilterChain;
        this.responseFilterChain = responseFilterChain;
        this.streamCloseFilterChain = streamCloseFilterChain;
        this.commandSerializerFactory = Objects.requireNonNull(commandSerializerFactory, "commandSerializerFactory");
    }

    @Override
    public void addRequestFilter(RouteFilter<StreamEvent> filter) {
        this.streamCreateFilterChain.addLast(filter);
    }

    @Override
    public void addResponseFilter(RouteFilter<ResponseEvent> filter) {
        this.responseFilterChain.addLast(filter);
    }

    @Override
    public TCommandTransferResponse onRoute(StreamEvent event) {
        streamCreateFilterChain.doEvent(event);

        return onRoute0(event);
    }

    private TCommandTransferResponse onRoute0(StreamEvent event) {
        TBase<?, ?> requestObject = event.getRequestObject();
        if (requestObject == null) {
            return createResponse(TRouteResult.EMPTY_REQUEST);
        }

        ClusterPoint<?> clusterPoint = findClusterPoint(event.getDeliveryCommand());
        if (clusterPoint == null) {
            return createResponse(TRouteResult.NOT_FOUND);
        }

        if (!clusterPoint.isSupportCommand(requestObject)) {
            logger.warn("Create StreamChannel failed. target:{}, message:{} is not supported command", clusterPoint, requestObject.getClass().getName());
            return createResponse(TRouteResult.NOT_SUPPORTED_REQUEST);
        }

        try {
            if (clusterPoint instanceof GrpcAgentConnection) {
                StreamRouteManager routeManager = new StreamRouteManager(event);

                ServerStreamChannel consumerStreamChannel = event.getStreamChannel();
                consumerStreamChannel.setAttributeIfAbsent(ATTACHMENT_KEY, routeManager);

                ClientStreamChannel producerStreamChannel = ((GrpcAgentConnection) clusterPoint).openStream(event.getRequestObject(), routeManager);
                routeManager.setProducer(producerStreamChannel);
                return createResponse(TRouteResult.OK);
            } else {
                return createResponse(TRouteResult.NOT_SUPPORTED_SERVICE);
            }
        } catch (StreamException e) {
            StreamCode streamCode = e.getStreamCode();
            return createResponse(TRouteResult.STREAM_CREATE_ERROR, streamCode.name());
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Create StreamChannel failed. target:{}, message:{}", clusterPoint, e.getMessage(), e);
            }
        }

        return createResponse(TRouteResult.UNKNOWN);
    }

    public void close(ServerStreamChannel consumerStreamChannel) {
        Object attachmentListener = consumerStreamChannel.getAttribute(ATTACHMENT_KEY);

        if (attachmentListener instanceof StreamRouteManager) {
            ((StreamRouteManager) attachmentListener).close();
        }
    }

    private byte[] serialize(TBase<?, ?> result) {
        return SerializationUtils.serialize(result, commandSerializerFactory, null);
    }

    // fix me : StreamRouteManager will change worker thread pattern. 
    private class StreamRouteManager extends ClientStreamChannelEventHandler {

        private final StreamEvent streamEvent;
        private final ServerStreamChannel consumer;

        private ClientStreamChannel producer;

        public StreamRouteManager(StreamEvent streamEvent) {
            this.streamEvent = streamEvent;
            this.consumer = streamEvent.getStreamChannel();
        }

        @Override
        public void handleStreamResponsePacket(ClientStreamChannel streamChannel, StreamResponsePacket packet) {
            StreamChannelStateCode stateCode = consumer.getCurrentState();
            if (StreamChannelStateCode.CONNECTED == stateCode) {
                TCommandTransferResponse response = createResponse(TRouteResult.OK, packet.getPayload());
                responseFilterChain.doEvent(new ResponseEvent(streamEvent, -1, response));
                consumer.sendData(serialize(response));
            } else {
                logger.warn("Can not route stream data to consumer.(state:{})", stateCode);
                if (StreamChannelStateCode.CONNECT_ARRIVED != stateCode) {
                    close();
                }
            }
        }

        @Override
        public void handleStreamClosePacket(ClientStreamChannel streamChannel, StreamClosePacket packet) {
            StreamRouteCloseEvent event = new StreamRouteCloseEvent(streamEvent.getDeliveryCommand(), streamChannel, streamEvent.getStreamChannel());
            streamCloseFilterChain.doEvent(event);

            consumer.close();
        }

        @Override
        public void stateUpdated(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) {
            logger.info("stateUpdated() streamChannel:{}, updatedStateCode:{}", streamChannel, updatedStateCode);

            switch (updatedStateCode) {
                case CLOSED:
                case ILLEGAL_STATE:
                    if (consumer != null) {
                        consumer.close();
                    }
                    break;
                default:
                    break;
            }
        }

        public void close() {
            if (consumer != null) {
                consumer.close();
            }

            if (producer != null) {
                producer.close();
            }
        }

        public ClientStreamChannel getProducer() {
            return producer;
        }

        public void setProducer(ClientStreamChannel sourceStreamChannel) {
            this.producer = sourceStreamChannel;
        }

    }

}
