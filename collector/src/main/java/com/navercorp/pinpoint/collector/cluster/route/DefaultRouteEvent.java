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

import org.jboss.netty.channel.Channel;

import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;

/**
 * @author koo.taejin
 */
public class DefaultRouteEvent implements RouteEvent {

    private final TCommandTransfer deliveryCommand;

    private final Channel sourceChannel;

    public DefaultRouteEvent(TCommandTransfer deliveryCommand, Channel sourceChannel) {
        this.deliveryCommand = deliveryCommand;
        this.sourceChannel = sourceChannel;
    }

    @Override
    public TCommandTransfer getDeliveryCommand() {
        return deliveryCommand;
    }

    @Override
    public Channel getSourceChannel() {
        return sourceChannel;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("{");
        sb.append("{sourceChannel=").append(sourceChannel).append(",");
        sb.append("applicationName=").append(deliveryCommand.getApplicationName()).append(",");
        sb.append("agentId=").append(deliveryCommand.getAgentId()).append(",");
        sb.append("startTimeStamp=").append(deliveryCommand.getStartTime());
        sb.append('}');
        return sb.toString();
    }

}
