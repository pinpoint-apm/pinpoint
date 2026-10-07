/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.alarm.service.AlarmRuleDeleter;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.dao.AgentIdDao;
import com.navercorp.pinpoint.common.server.dao.ApplicationDao;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.scatter.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.transaction.support.TransactionOperations;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationCleanupTaskletTest {

    private static final Application APPLICATION = new Application(Service.DEFAULT, "app", ServiceType.STAND_ALONE);

    private final ApplicationDao applicationDao = mock(ApplicationDao.class);
    private final AgentIdDao agentIdDao = mock(AgentIdDao.class);
    private final TraceIndexDao traceIndexDao = mock(TraceIndexDao.class);
    private final AlarmRuleDeleter alarmRuleDeleter = mock(AlarmRuleDeleter.class);

    ApplicationCleanupTaskletTest() {
        when(applicationDao.getApplications(ServiceUid.DEFAULT_SERVICE_UID_CODE)).thenReturn(List.of(APPLICATION));
        when(agentIdDao.countAgentIdEntry(anyInt(), anyString(), anyInt())).thenReturn(0);
        when(traceIndexDao.scanTraceScatterData(any(), anyString(), anyInt(), any(), anyInt()))
                .thenReturn(new LimitedScanResult<>(0, List.of()));
    }

    @Test
    void deletesTheRulesOfAnApplicationBeforeTheApplication() {
        tasklet(false).execute(null, null);

        InOrder order = inOrder(alarmRuleDeleter, applicationDao);
        ArgumentCaptor<AlarmApplication> captor = ArgumentCaptor.forClass(AlarmApplication.class);
        order.verify(alarmRuleDeleter).deleteRulesByApplication(captor.capture());
        order.verify(applicationDao).deleteApplication(eq(Service.DEFAULT.getServiceUid()), eq("app"),
                eq((int) ServiceType.STAND_ALONE.getCode()), anyLong());
        assertThat(captor.getValue().getServiceName()).isEqualTo(Service.DEFAULT.getServiceName());
        assertThat(captor.getValue().getApplicationName()).isEqualTo("app");
        assertThat(captor.getValue().getApplicationType()).isEqualTo("STAND_ALONE");
    }

    @Test
    void dryRunKeepsTheRulesButStillCountsThem() {
        tasklet(true).execute(null, null);

        verify(alarmRuleDeleter, never()).deleteRulesByApplication(any());
        verify(applicationDao, never()).deleteApplication(anyInt(), anyString(), anyInt(), anyLong());
        verify(alarmRuleDeleter).countRulesByApplication(any());
    }

    @Test
    void keepsTheApplicationWhenItsRulesSurviveTheDelete() {
        when(alarmRuleDeleter.deleteRulesByApplication(any())).thenThrow(new IllegalStateException("deadlock"));

        assertThatThrownBy(() -> tasklet(false).execute(null, null))
                .isInstanceOf(IllegalStateException.class);

        verify(applicationDao, never()).deleteApplication(anyInt(), anyString(), anyInt(), anyLong());
    }

    private ApplicationCleanupTasklet tasklet(boolean dryRun) {
        return new ApplicationCleanupTasklet(applicationDao, agentIdDao, traceIndexDao, mock(MapAgentResponseDao.class),
                alarmRuleDeleter, TransactionOperations.withoutTransaction(),
                dryRun, System.currentTimeMillis(), 30, Integer.MAX_VALUE, 7, Set.of());
    }
}
