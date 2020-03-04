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

import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannel;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;

/**
 * @author koo.taejin
 */
public class StreamRouteCloseEvent extends DefaultRouteEvent {

    private final ClientStreamChannel producerStreamChannel;
    private final ServerStreamChannel consumerStreamChannel;

    public StreamRouteCloseEvent(TCommandTransfer deliveryCommand, ClientStreamChannel producerStreamChannel, ServerStreamChannel consumerStreamChannel) {
        super(deliveryCommand, consumerStreamChannel.getRemoteAddress());

        this.producerStreamChannel = producerStreamChannel;
        this.consumerStreamChannel = consumerStreamChannel;
    }

    public int getProducerStreamChannelId() {
        return producerStreamChannel.getStreamId();
    }
    
    public int getConsumerStreamChannelId() {
        return consumerStreamChannel.getStreamId();
    }

    public ClientStreamChannel getProducerStreamChannel() {
        return producerStreamChannel;
    }

    public ServerStreamChannel getConsumerStreamChannel() {
        return consumerStreamChannel;
    }

}
