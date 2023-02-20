/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.web.realtime.atc.service;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.web.realtime.atc.dao.ATCValueDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.memory.ATCSessionRepository;
import com.navercorp.pinpoint.web.realtime.atc.dto.ATCSession;
import com.navercorp.pinpoint.web.realtime.atc.service.SupplyFlushServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class SupplyFlushServiceImplTest {

    @Mock ATCValueDao valueDao;
    @Mock
    ATCSessionRepository repo;

    @Test
    public void testFlush() throws IOException {
        final String app1 = "test-application-1";
        final String app2 = "test-application-2";

        final ClusterKey clusterKey11 = new ClusterKey(app1, "test-agent-1-1", 999911);
        final ClusterKey clusterKey12 = new ClusterKey(app1, "test-agent-1-2", 999912);
        final ClusterKey clusterKey21 = new ClusterKey(app2, "test-agent-2-1", 999921);
        final ClusterKey clusterKey22 = new ClusterKey(app2, "test-agent-2-2", 999922);

        final ATCSession session1 = Mockito.mock(ATCSession.class);
        final ATCSession session2 = Mockito.mock(ATCSession.class);

        final long now = System.nanoTime();

        when(repo.getSessions()).thenReturn(List.of(session1, session2));

        when(session1.getDemandApplicationName()).thenReturn(app1);
        when(session1.getCreatedAt()).thenReturn(now);
        when(session2.getDemandApplicationName()).thenReturn(app2);
        when(session2.getCreatedAt()).thenReturn(now);

        when(valueDao.getActiveAgents(app1)).thenReturn(List.of(clusterKey11, clusterKey12));
        when(valueDao.getActiveAgents(app2)).thenReturn(List.of(clusterKey21, clusterKey22));

        when(valueDao.query(any(), anyLong())).thenReturn(List.of(0, 0, 2, 0));

        final SupplyFlushServiceImpl service = new SupplyFlushServiceImpl(repo, valueDao, 4);
        service.flush();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        verify(valueDao, times(4)).query(any(), anyLong());

        verify(valueDao).getActiveAgents(app1);
        verify(valueDao).getActiveAgents(app2);

        verify(session1).sendMessage(any());
        verify(session2).sendMessage(any());
    }

}
