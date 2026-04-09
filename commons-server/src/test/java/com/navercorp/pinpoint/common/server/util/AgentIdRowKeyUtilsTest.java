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

package com.navercorp.pinpoint.common.server.util;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AgentIdRowKeyUtilsTest {

    @Test
    public void testExtractValues() {
        int serviceUid = 100;
        String applicationName = "testApp";
        int serviceTypeCode = 1000;
        String agentId = "agent-01";
        long agentStartTime = System.currentTimeMillis();

        byte[] row = AgentIdRowKeyUtils.createRow(serviceUid, applicationName, serviceTypeCode, agentId, agentStartTime);

        Assertions.assertEquals(serviceUid, AgentIdRowKeyUtils.extractServiceUid(row));
        Assertions.assertEquals(applicationName, AgentIdRowKeyUtils.extractApplicationName(row));
        Assertions.assertEquals(serviceTypeCode, AgentIdRowKeyUtils.extractServiceTypeCode(row));
        Assertions.assertEquals(agentId, AgentIdRowKeyUtils.extractAgentId(row));
        Assertions.assertEquals(agentStartTime, AgentIdRowKeyUtils.extractAgentStartTime(row));
    }

    @Test
    public void testExtractApplicationName_longName() {
        int serviceUid = 1;
        String applicationName = "a".repeat(64);
        int serviceTypeCode = 2000;
        String agentId = "agent-02";
        long agentStartTime = 1234567890L;

        byte[] row = AgentIdRowKeyUtils.createRow(serviceUid, applicationName, serviceTypeCode, agentId, agentStartTime);

        Assertions.assertEquals(applicationName, AgentIdRowKeyUtils.extractApplicationName(row));
    }

    @Test
    public void testCreateApplicationNamePredicate() {
        int serviceUid = 100;
        String applicationName = "testApp";
        int serviceTypeCode = 1000;
        String agentId = "agent-01";
        long agentStartTime = System.currentTimeMillis();

        byte[] row = AgentIdRowKeyUtils.createRow(serviceUid, applicationName, serviceTypeCode, agentId, agentStartTime);
        Result result = Result.create(new Cell[]{new KeyValue(row, new byte[0], new byte[0], new byte[0])});

        Assertions.assertTrue(AgentIdRowKeyUtils.createApplicationNamePredicate(applicationName).test(result));
        Assertions.assertFalse(AgentIdRowKeyUtils.createApplicationNamePredicate("otherApp").test(result));
    }
}