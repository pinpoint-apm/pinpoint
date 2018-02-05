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

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.StateChangeEventListener;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageListener;
import org.jboss.netty.util.Timer;

import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultPinpointClientHandlerFactory implements ClientHandlerFactory {

    private final ClientOption clientOption;
    private final Map<String, Object> handShakeData;
    private final ClusterOption clusterOption;

    private final MessageListener messageListener;
    private final ServerStreamChannelMessageListener serverStreamChannelMessageListener;
    private final List<StateChangeEventListener> stateChangeEventListeners;



    public DefaultPinpointClientHandlerFactory(ClientOption clientOption, Map<String, Object> handShakeData, ClusterOption clusterOption,
                                               MessageListener messageListener,
                                               ServerStreamChannelMessageListener serverStreamChannelMessageListener,
                                               List<StateChangeEventListener> stateChangeEventListeners) {

        this.clientOption = Assert.requireNonNull(clientOption, "clientOption must not be null");
        this.handShakeData = Assert.requireNonNull(handShakeData, "handShakeData must not be null");
        this.clusterOption = Assert.requireNonNull(clusterOption, "clusterOption must not be null");

        this.messageListener = Assert.requireNonNull(messageListener, "messageListener must not be null");
        this.serverStreamChannelMessageListener = Assert.requireNonNull(serverStreamChannelMessageListener, "serverStreamChannelMessageListener must not be null");
        this.stateChangeEventListeners = Assert.requireNonNull(stateChangeEventListeners, "stateChangeEventListeners must not be null");

    }


    @Override
    public DefaultPinpointClientHandler newClientHandler(ConnectionFactory connectionFactory, Timer channelTimer, boolean reconnect) {


        final DefaultPinpointClientHandler clientHandler = new DefaultPinpointClientHandler(connectionFactory, handShakeData, clusterOption,
                messageListener, serverStreamChannelMessageListener, stateChangeEventListeners,
                channelTimer, clientOption);

        if (reconnect) {
            clientHandler.initReconnect();
        }
        return clientHandler;
    }


}
