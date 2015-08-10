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

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.thrift.io.TCommandTypeVersion;

/**
 * @author koo.taejin
 */
public class StreamRouteHandler extends AbstractRouteHandler<StreamEvent> {

    public static final String ATTACHMENT_KEY = StreamRouteManager.class.getSimpleName();
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RouteFilterChain<StreamEvent> streamCreateFilterChain;
    private final RouteFilterChain<ResponseEvent> responseFilterChain;
    private final RouteFilterChain<StreamRouteCloseEvent> streamCloseFilterChain;

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
    public RouteResult onRoute(StreamEvent event) {
        streamCreateFilterChain.doEvent(event);

        RouteResult routeResult = onRoute0(event);
        return routeResult;
    }

    private RouteResult onRoute0(StreamEvent event) {
        TBase<?,?> requestObject = event.getRequestObject();
        if (requestObject == null) {
            return new RouteResult(RouteStatus.BAD_REQUEST);
        }

        TargetClusterPoint clusterPoint = findClusterPoint(event.getDeliveryCommand());
        if (clusterPoint == null) {
            return new RouteResult(RouteStatus.NOT_FOUND);
        }

        TCommandTypeVersion commandVersion = TCommandTypeVersion.getVersion(clusterPoint.gerVersion());
        if (!commandVersion.isSupportCommand(requestObject)) {
            return new RouteResult(RouteStatus.NOT_ACCEPTABLE);
        }

        try {
            if (clusterPoint instanceof PinpointServerClusterPoint) {
                StreamRouteManager routeManager = new StreamRouteManager(event);

                ServerStreamChannelContext consumerContext = event.getStreamChannelContext();
                consumerContext.setAttributeIfAbsent(ATTACHMENT_KEY, routeManager);

                ClientStreamChannelContext producerContext = createStreamChannel((PinpointServerClusterPoint) clusterPoint, event.getDeliveryCommand().getPayload(), routeManager);
                routeManager.setProducer(producerContext.getStreamChannel());

                return new RouteResult(RouteStatus.OK);
            } else {
                return new RouteResult(RouteStatus.NOT_ACCEPTABLE_AGENT_TYPE);
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Create StreamChannel(" + clusterPoint  + ") failed. Error:" + e.getMessage(), e);
            }
        }
        
        return new RouteResult(RouteStatus.NOT_ACCEPTABLE_UNKNOWN);
    }
    
    private ClientStreamChannelContext createStreamChannel(PinpointServerClusterPoint clusterPoint, byte[] payload, ClientStreamChannelMessageListener messageListener) {
        PinpointServer pinpointServer = clusterPoint.getPinpointServer();
        return pinpointServer.createStream(payload, messageListener);
    }
    
    public void close(ServerStreamChannelContext consumerContext) {
        Object attachmentListener = consumerContext.getAttribute(ATTACHMENT_KEY);
        
        if (attachmentListener != null && attachmentListener instanceof StreamRouteManager) {
            ((StreamRouteManager)attachmentListener).close();
        }
    }

    // fix me : StreamRouteManager will change worker thread pattern. 
    private class StreamRouteManager implements ClientStreamChannelMessageListener {

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
            if (StreamChannelStateCode.RUN == stateCode) {
                ResponseMessage responseMessage = new ResponseMessage();
                responseMessage.setMessage(packet.getPayload());

                responseFilterChain.doEvent(new ResponseEvent(streamEvent, -1, new RouteResult(RouteStatus.OK, responseMessage)));

                consumer.sendData(packet.getPayload());
            } else {
                logger.warn("Can route stream data to consumer.(state:{})", stateCode);
                if (StreamChannelStateCode.OPEN_ARRIVED != stateCode) {
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

        public void close() {
            if (this.consumer != null) {
                consumer.close();
            }

            if (this.producer != null) {
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
