/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.web.vo.activethread.AgentActiveThreadCount;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.Executor;

/**
 * @author Taejin Koo
 */
public interface PinpointWebSocketResponseAggregator {

    void start();

    void stop();

    void flush() throws Exception;

    void flush(Executor executor) throws Exception;

    void response(AgentActiveThreadCount activeThreadCount);

    void addWebSocketSession(WebSocketSession webSocketSession);

    // return when aggregator cleared.
    boolean removeWebSocketSessionAndGetIsCleared(WebSocketSession webSocketSession);

    void addActiveWorker(ClusterKey clusterKey);

    String getApplicationName();

}
