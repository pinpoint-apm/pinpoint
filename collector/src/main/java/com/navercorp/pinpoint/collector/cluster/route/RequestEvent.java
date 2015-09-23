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

import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import org.apache.thrift.TBase;

import java.net.SocketAddress;

/**
 * @author koo.taejin
 */
public class RequestEvent extends DefaultRouteEvent {

    private final int requestId;

    private final TBase requestObject;

    public RequestEvent(RouteEvent routeEvent, int requestId, TBase requestObject) {
        this(routeEvent.getDeliveryCommand(), routeEvent.getRemoteAddress(), requestId, requestObject);
    }

    public RequestEvent(TCommandTransfer deliveryCommand, SocketAddress remoteAddress, int requestId, TBase requestObject) {
        super(deliveryCommand, remoteAddress);

        this.requestId = requestId;
        this.requestObject = requestObject;
    }

    public int getRequestId() {
        return requestId;
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
        sb.append("requestId=").append(requestId);
        sb.append("requestObject=").append(requestObject);
        sb.append('}');
        return sb.toString();
    }

}
