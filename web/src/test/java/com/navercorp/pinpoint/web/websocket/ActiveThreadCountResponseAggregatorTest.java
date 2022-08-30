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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Timer;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author youngjin.kim2
 */
@RunWith(MockitoJUnitRunner.class)
public class ActiveThreadCountResponseAggregatorTest {

    @Mock
    AgentService agentService;
    @Mock
    Timer timer;
    @Mock
    TimerTaskDecorator timerTaskDecorator;
    @Mock
    ClientStreamChannel channel;
    @Mock
    WebSocketSession session;

    final String applicationName = "sample-app";
    final ClusterKey clusterKey = ClusterKey.parse("sample-app:sample-agent-1:1234");
    final AgentStatus agentStatus = new AgentStatus("sample-agent-1", AgentLifeCycleState.RUNNING, 1234);
    final ClusterKeyAndStatus cks = new ClusterKeyAndStatus(clusterKey, agentStatus);

    ActiveThreadCountResponseAggregator aggregator;

    @Before
    public void before() throws Exception {
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

        assertEquals("# of sessions should be 1", 1, aggregator.countWebSocketSession());

        verify(this.agentService, times(1)).getRecentAgentInfoList(eq(applicationName), anyLong());
        verify(this.agentService, times(1)).openStream(eq(clusterKey), any(TBase.class), any());
    }

    @Test
    public void shouldDeactivateWorkerByRemovingLastSession() {
        aggregator.start();

        aggregator.addWebSocketSession(this.session);
        verify(this.channel, times(0)).close();
        assertEquals("# of sessions should be 1", 1, aggregator.countWebSocketSession());

        aggregator.addWebSocketSession(session);
        verify(this.channel, times(0)).close();
        assertEquals("# of sessions should be 2", 2, aggregator.countWebSocketSession());

        aggregator.removeWebSocketSessionAndGetIsCleared(session);
        verify(this.channel, times(0)).close();
        assertEquals("# of sessions should be 1", 1, aggregator.countWebSocketSession());

        aggregator.removeWebSocketSessionAndGetIsCleared(session);
        verify(this.channel, times(1)).close();
        assertEquals("# of sessions should be 0", 0, aggregator.countWebSocketSession());
    }
}
