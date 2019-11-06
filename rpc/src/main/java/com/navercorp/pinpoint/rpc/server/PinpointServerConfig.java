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

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelMessageHandler;

import org.jboss.netty.util.Timer;

import java.util.List;

/**
 * @author Taejin Koo
 */
public interface PinpointServerConfig {

    long getDefaultRequestTimeout();

    Timer getRequestManagerTimer();

    ServerMessageListener getMessageListener();
    List<ServerStateChangeEventHandler> getStateChangeEventHandlers();

    ServerStreamChannelMessageHandler getServerStreamMessageHandler();

    ClusterOption getClusterOption();

}
