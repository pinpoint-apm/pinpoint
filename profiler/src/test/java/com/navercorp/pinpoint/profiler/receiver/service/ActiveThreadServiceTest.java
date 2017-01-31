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

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.profiler.context.ActiveTrace;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCount;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;

import org.apache.thrift.TBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ActiveThreadServiceTest {

    // defence weak value
    private List<ActiveTrace> weakList;
    private long activeTraceId = 0;

    @Before
    public void setUp() throws Exception {
        this.weakList = new ArrayList<ActiveTrace>();
    }

    @Test
    public void serviceTest1() throws InterruptedException {
        ActiveTraceRepository activeTraceRepository = new ActiveTraceRepository();

        int normalCount = 5;
        long normalExecutionTime = 1500;
        addActiveTrace(activeTraceRepository, normalExecutionTime, normalCount);

        int fastCount = 3;
        long fastExecutionTime = 500;
        addActiveTrace(activeTraceRepository, fastExecutionTime, fastCount);

        ActiveThreadCountService service = new ActiveThreadCountService(activeTraceRepository);
        TBase<?, ?> tBase = service.requestCommandService(new TCmdActiveThreadCount());
        if (tBase instanceof TCmdActiveThreadCountRes) {
            List<Integer> activeThreadCount = ((TCmdActiveThreadCountRes) tBase).getActiveThreadCount();
            Assert.assertEquals(activeThreadCount.get(0), Integer.valueOf(fastCount));
            Assert.assertEquals(activeThreadCount.get(1), Integer.valueOf(normalCount));
        } else {
            Assert.fail();
        }
    }

    private void addActiveTrace(ActiveTraceRepository activeTraceRepository, long executionTime, int addCount) {
        for (int i = 0; i < addCount; i++) {
            ActiveTrace activeTrace = createActiveTrace(executionTime);
            this.weakList.add(activeTrace);
            activeTraceRepository.put(activeTrace);
        }
    }

    private ActiveTrace createActiveTrace(long executionTime) {
        ActiveTrace activeTrace = Mockito.mock(ActiveTrace.class);
        Mockito.when(activeTrace.getStartTime()).thenReturn(System.currentTimeMillis() - executionTime);
        Mockito.when(activeTrace.getId()).thenReturn(nextLocalTransactionId());
        return activeTrace;
    }

    private long nextLocalTransactionId() {
        return activeTraceId++;
    }

}
