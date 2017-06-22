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

import com.navercorp.pinpoint.collector.cluster.ClusterPointLocator;
import com.navercorp.pinpoint.collector.cluster.PinpointServerClusterPoint;
import com.navercorp.pinpoint.collector.cluster.TargetClusterPoint;
import com.navercorp.pinpoint.collector.cluster.route.filter.RouteFilter;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author koo.taejin
 */
public class StreamRouteHandler extends AbstractRouteHandler<StreamEvent> {

    public static final String ATTACHMENT_KEY = StreamRouteManager.class.getSimpleName();
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RouteFilterChain<StreamEvent> streamCreateFilterChain;
    private final RouteFilterChain<ResponseEvent> responseFilterChain;
    private final RouteFilterChain<StreamRouteCloseEvent> streamCloseFilterChain;

    @Autowired
    @Qualifier("commandHeaderTBaseSerializerFactory")
    private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    public StreamRouteHandler(ClusterPointLocator<TargetClusterPoint> targetClusterPointLocator,
            RouteFilterChain<StreamEvent> streamCreateFilterChain,
            RouteFilterChain<ResponseEvent> responseFilterChain,
            RouteFilterChain<StreamRouteCloseEvent> streamCloseFilterChain) {
        super(targetClusterPointLocator);

        this.streamCreateFilterChain = streamCreateFilterChain;
        this.responseFilterChain = responseFilterChain;
        this.streamCloseFilterChain = streamCloseFilterChain;
    }

    @Override
    public void addRequestFilter(RouteFilter<StreamEvent> filter) {
        this.streamCreateFilterChain.addLast(filter);
    }

    @Override
    public void addResponseFilter(RouteFilter<ResponseEvent> filter) {
        this.responseFilterChain.addLast(filter);
    }

    public void addCloseFilter(RouteFilter<StreamRouteCloseEvent> filter) {
        this.streamCloseFilterChain.addLast(filter);
    }

    @Override
    public TCommandTransferResponse onRoute(StreamEvent event) {
        streamCreateFilterChain.doEvent(event);

        TCommandTransferResponse routeResult = onRoute0(event);
        return routeResult;
    }

    private TCommandTransferResponse onRoute0(StreamEvent event) {
        TBase<?,?> requestObject = event.getRequestObject();
        if (requestObject == null) {
            return createResponse(TRouteResult.EMPTY_REQUEST);
        }

        TargetClusterPoint clusterPoint = findClusterPoint(event.getDeliveryCommand());
        if (clusterPoint == null) {
            return createResponse(TRouteResult.NOT_FOUND);
        }

        if (!clusterPoint.isSupportCommand(requestObject)) {
            return createResponse(TRouteResult.NOT_SUPPORTED_REQUEST);
        }

        try {
            if (clusterPoint instanceof PinpointServerClusterPoint) {
                StreamRouteManager routeManager = new StreamRouteManager(event);

                ServerStreamChannelContext consumerContext = event.getStreamChannelContext();
                consumerContext.setAttributeIfAbsent(ATTACHMENT_KEY, routeManager);

                ClientStreamChannelContext producerContext = createStreamChannel((PinpointServerClusterPoint) clusterPoint, event.getDeliveryCommand().getPayload(), routeManager);
                if (producerContext.getCreateFailPacket() == null) {
                    routeManager.setProducer(producerContext.getStreamChannel());
                    producerContext.getStreamChannel().addStateChangeEventHandler(routeManager);
                    return createResponse(TRouteResult.OK);
                }
            } else {
                return createResponse(TRouteResult.NOT_SUPPORTED_SERVICE);
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Create StreamChannel({}) failed. Error:{}", clusterPoint, e.getMessage(), e);
            }
        }

        return createResponse(TRouteResult.UNKNOWN);
    }
    
    private ClientStreamChannelContext createStreamChannel(PinpointServerClusterPoint clusterPoint, byte[] payload, ClientStreamChannelMessageListener messageListener) {
        PinpointServer pinpointServer = clusterPoint.getPinpointServer();
        return pinpointServer.openStream(payload, messageListener);
    }
    
    public void close(ServerStreamChannelContext consumerContext) {
        Object attachmentListener = consumerContext.getAttribute(ATTACHMENT_KEY);
        
        if (attachmentListener instanceof StreamRouteManager) {
            ((StreamRouteManager)attachmentListener).close();
        }
    }

    private TCommandTransferResponse createResponse(TRouteResult result) {
        return createResponse(result, new byte[0]);
    }

    private TCommandTransferResponse createResponse(TRouteResult result, byte[] payload) {
        TCommandTransferResponse response = new TCommandTransferResponse();
        response.setRouteResult(result);
        response.setPayload(payload);
        return response;
    }

    private byte[] serialize(TBase<?,?> result) {
        return SerializationUtils.serialize(result, commandSerializerFactory, null);
    }


    // fix me : StreamRouteManager will change worker thread pattern. 
    private class StreamRouteManager implements ClientStreamChannelMessageListener, StreamChannelStateChangeEventHandler<ClientStreamChannel> {

        private final StreamEvent streamEvent;
        private final ServerStreamChannel consumer;

        private ClientStreamChannel producer;

        public StreamRouteManager(StreamEvent streamEvent) {
            this.streamEvent = streamEvent;
            this.consumer = streamEvent.getStreamChannelContext().getStreamChannel();
        }

        @Override
        public void handleStreamData(ClientStreamChannelContext producerContext, StreamResponsePacket packet) {
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
        public void handleStreamClose(ClientStreamChannelContext producerContext, StreamClosePacket packet) {
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setMessage(packet.getPayload());

            StreamRouteCloseEvent event = new StreamRouteCloseEvent(streamEvent.getDeliveryCommand(), producerContext, streamEvent.getStreamChannelContext());
            streamCloseFilterChain.doEvent(event);

            consumer.close();
        }

        @Override
        public void eventPerformed(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) throws Exception {
            logger.info("eventPerformed streamChannel:{}, stateCode:{}", streamChannel, updatedStateCode);

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

        @Override
        public void exceptionCaught(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode, Throwable e) {
            logger.warn("exceptionCaught message:{}, streamChannel:{}, stateCode:{}", e.getMessage(), streamChannel, updatedStateCode, e);
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
