/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultTraceIdTest {

    @Test
    public void parseTest() {
        String agent = "test";
        long agentStartTime = System.currentTimeMillis();
        long agentTransactionCount = 10;
        TraceId traceId = new DefaultTraceId(TransactionId.of(agent, agentStartTime, agentTransactionCount));

        String id = traceId.getTransactionId();

        TransactionId transactionid = TransactionIdUtils.parseTransactionId(id);

        Assertions.assertEquals(agent, transactionid.getAgentId());
        Assertions.assertEquals(agentStartTime, transactionid.getAgentStartTime());
        Assertions.assertEquals(agentTransactionCount, transactionid.getTransactionSequence());
    }
}
