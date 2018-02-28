/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.StateChangeEventListener;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.stream.DisabledServerStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultPinpointClientFactory extends AbstractPinpointClientFactory {

    public DefaultPinpointClientFactory() {
        this(1, 1);
    }

    public DefaultPinpointClientFactory(int bossCount, int workerCount) {
        super(bossCount, workerCount);
    }

    @Override
    protected ConnectionFactory createConnectionFactory() {
        final ClientOption clientOption = clientOptionBuilder.build();
        final ClusterOption clusterOption = ClusterOption.copy(this.clusterOption);

        final MessageListener messageListener = this.getMessageListener(SimpleMessageListener.INSTANCE);
        final ServerStreamChannelMessageListener serverStreamChannelMessageListener = this.getServerStreamChannelMessageListener(DisabledServerStreamChannelMessageListener.INSTANCE);
        final List<StateChangeEventListener> stateChangeEventListeners = this.getStateChangeEventListeners();

        Map<String, Object> copyProperties = new HashMap<String, Object>(this.properties);
        final HandshakerFactory handshakerFactory = new HandshakerFactory(socketId, copyProperties, clientOption, clusterOption);
        final ClientHandlerFactory clientHandlerFactory = new DefaultPinpointClientHandlerFactory(clientOption, clusterOption, handshakerFactory,
                messageListener, serverStreamChannelMessageListener, stateChangeEventListeners);

        final SocketOption socketOption = this.socketOptionBuilder.build();

        return new ConnectionFactory(timer, this.closed, this.channelFactory, socketOption, clientOption, clientHandlerFactory);
    }

}
