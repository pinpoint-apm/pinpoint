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

import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;

/**
 * @author koo.taejin
 */
public class StreamEvent extends DefaultRouteEvent {

    private final ServerStreamChannelContext streamChannelContext;
    private final TBase requestObject;
    
    public StreamEvent(RouteEvent routeEvent, ServerStreamChannelContext streamChannelContext, TBase requestObject) {
        this(routeEvent.getDeliveryCommand(), streamChannelContext, requestObject);
    }

    public StreamEvent(TCommandTransfer deliveryCommand, ServerStreamChannelContext streamChannelContext, TBase requestObject) {
        super(deliveryCommand, streamChannelContext.getStreamChannel().getChannel().getRemoteAddress());

        this.streamChannelContext = streamChannelContext;
        this.requestObject = requestObject;
    }

    public ServerStreamChannelContext getStreamChannelContext() {
        return streamChannelContext;
    }
    
    public int getStreamChannelId() {
        return getStreamChannelContext().getStreamId();
    }

    public TBase getRequestObject() {
        return requestObject;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("{");
        sb.append("{remoteAddress=").append(getRemoteAddress()).append(",");
        sb.append("applicationName=").append(getDeliveryCommand().getApplicationName()).append(",");
        sb.append("agentId=").append(getDeliveryCommand().getAgentId()).append(",");
        sb.append("startTimeStamp=").append(getDeliveryCommand().getStartTime());
        sb.append("streamChannelContext=").append(getStreamChannelContext());
        sb.append("streamChannelId=").append(getStreamChannelId());
        sb.append("requestObject=").append(requestObject);
        sb.append('}');
        return sb.toString();
    }

}
