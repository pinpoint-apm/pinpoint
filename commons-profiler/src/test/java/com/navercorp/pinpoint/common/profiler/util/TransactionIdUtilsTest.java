/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.profiler.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class TransactionIdUtilsTest {

    public static final String AGENT_ID = "test";

    @Test
    public void testParseTransactionId() {
        TransactionId transactionId = TransactionIdUtils.parseTransactionId(AGENT_ID + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "1" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "2");
        Assertions.assertEquals("test", transactionId.getAgentId());
        Assertions.assertEquals(1L, transactionId.getAgentStartTime());
        Assertions.assertEquals(2L, transactionId.getTransactionSequence());
    }

    @Test
    public void testParseTransactionId2() {
        TransactionId transactionId = TransactionIdUtils.parseTransactionId(AGENT_ID + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "1" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "2" + TransactionIdUtils.TRANSACTION_ID_DELIMITER);
        Assertions.assertEquals(AGENT_ID, transactionId.getAgentId());
        Assertions.assertEquals(1L, transactionId.getAgentStartTime());
        Assertions.assertEquals(2L, transactionId.getTransactionSequence());
    }


    @Test
    public void testParseTransactionId_RpcHeaderDuplicateAdd_BugReproduce() {
        Assertions.assertThrows(Exception.class, () -> {
            // #27 http://yobi.navercorp.com/pinpoint/pinpoint/issue/27
            String id1 = AGENT_ID + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "1" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "2";
            String id2 = AGENT_ID + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "1" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "3";
            TransactionId transactionId = TransactionIdUtils.parseTransactionId(id1 + ", " + id2);
            Assertions.assertEquals("test", transactionId.getAgentId());
            Assertions.assertEquals(1L, transactionId.getAgentStartTime());
            Assertions.assertEquals(2L, transactionId.getTransactionSequence());
        });
    }

    @Test
    public void validateAgentId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            TransactionIdUtils.parseTransactionId("ag$$ent^1^2");
        });
    }

    @Test
    public void longAgentId() {
        String agentId = StringUtils.repeat('a', PinpointConstants.AGENT_ID_MAX_LEN);
        TransactionId transactionId = TransactionIdUtils.parseTransactionId(agentId + "^1^2");
        Assertions.assertEquals(agentId, transactionId.getAgentId());
    }

    @Test
    public void tooLongAgentId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String agentId = StringUtils.repeat('a', PinpointConstants.AGENT_ID_MAX_LEN + 1);
            TransactionId transactionId = TransactionIdUtils.parseTransactionId(agentId + "^1^2");
            Assertions.assertEquals(agentId, transactionId.getAgentId());
        });
    }

}
