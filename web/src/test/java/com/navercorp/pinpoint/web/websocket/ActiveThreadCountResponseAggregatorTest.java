/*
 * Copyright 2022 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.web.cluster.ClusterKeyAndStatus;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.task.TimerTaskDecorator;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class ActiveThreadCountResponseAggregatorTest {

    @Mock
    private AgentService agentService;
    @Mock
    private Timer timer;
    @Mock
    private TimerTaskDecorator timerTaskDecorator;
    @Mock
    private ClientStreamChannel channel;
    @Mock
    private WebSocketSession session;

    final String applicationName = "sample-app";
    final ClusterKey clusterKey = ClusterKey.parse("sample-app:sample-agent-1:1234");
    final AgentStatus agentStatus = new AgentStatus("sample-agent-1", AgentLifeCycleState.RUNNING, 1234);
    final ClusterKeyAndStatus cks = new ClusterKeyAndStatus(clusterKey, agentStatus);

    ActiveThreadCountResponseAggregator aggregator;

    @BeforeEach
    public void beforeEach() throws Exception {
        when(this.agentService.getRecentAgentInfoList(eq(applicationName), anyLong()))
                .thenReturn(List.of(cks));
        when(this.agentService.openStream(eq(clusterKey), any(TBase.class), any()))
                .thenReturn(this.channel);
        when(this.channel.awaitOpen(anyLong()))
                .thenReturn(true);

        this.aggregator = new ActiveThreadCountResponseAggregator(
                applicationName,
                this.agentService,
                this.timer,
                this.timerTaskDecorator
        );
    }

    @Test
    public void shouldActivateWorkerByAddingFirstSession() throws Exception {
        aggregator.start();
        aggregator.addWebSocketSession(this.session);

        assertEquals(1, aggregator.countWebSocketSession(), "# of sessions should be 1");

        verify(this.agentService).getRecentAgentInfoList(eq(applicationName), anyLong());
        verify(this.agentService).openStream(eq(clusterKey), any(TBase.class), any());
    }

    @Test
    public void shouldDeactivateWorkerByRemovingLastSession() {
        aggregator.start();

        aggregator.addWebSocketSession(this.session);
        verify(this.channel, never()).close();
        assertEquals(1, aggregator.countWebSocketSession(), "# of sessions should be 1");

        aggregator.addWebSocketSession(session);
        verify(this.channel, never()).close();
        assertEquals(2, aggregator.countWebSocketSession(), "# of sessions should be 2");

        aggregator.removeWebSocketSessionAndGetIsCleared(session);
        verify(this.channel, never()).close();
        assertEquals(1, aggregator.countWebSocketSession(), "# of sessions should be 1");

        aggregator.removeWebSocketSessionAndGetIsCleared(session);
        verify(this.channel).close();
        assertEquals(0, aggregator.countWebSocketSession(), "# of sessions should be 0");
    }
}
