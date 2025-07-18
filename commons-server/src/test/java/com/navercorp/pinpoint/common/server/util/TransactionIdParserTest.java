/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

class TransactionIdParserTest {

    public static final String AGENT_ID = "test";

    @Test
    public void testParseTransactionIdByte_AgentIdisNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            long time = System.currentTimeMillis();
            byte[] bytes = TransactionIdParser.writeTransactionId(null, time, 1);
            TransactionIdParser.parse(bytes, null);
        });
    }

    @Test
    public void testParseTransactionIdByte1() {
        long time = System.currentTimeMillis();
        byte[] bytes = TransactionIdParser.writeTransactionId(AGENT_ID, time, 2);
        TransactionId transactionId = TransactionIdParser.parse(bytes, AGENT_ID);
        Assertions.assertEquals(AGENT_ID, transactionId.getAgentId());
        Assertions.assertEquals(time, transactionId.getAgentStartTime());
        Assertions.assertEquals(2L, transactionId.getTransactionSequence());
    }

    @Test
    public void testParseTransactionIdByte2() {
        long time = Long.MAX_VALUE;
        byte[] bytes = TransactionIdParser.writeTransactionId(AGENT_ID, time, Long.MAX_VALUE);
        TransactionId transactionId = TransactionIdParser.parse(bytes, AGENT_ID);
        Assertions.assertEquals(AGENT_ID, transactionId.getAgentId());
        Assertions.assertEquals(Long.MAX_VALUE, transactionId.getAgentStartTime());
        Assertions.assertEquals(Long.MAX_VALUE, transactionId.getTransactionSequence());
    }

    @Test
    public void testParseTransactionIdByte3() {
        long time = Long.MIN_VALUE;
        byte[] bytes = TransactionIdParser.writeTransactionId(AGENT_ID, time, Long.MIN_VALUE);
        TransactionId transactionId = TransactionIdParser.parse(bytes, AGENT_ID);
        Assertions.assertEquals(AGENT_ID, transactionId.getAgentId());
        Assertions.assertEquals(Long.MIN_VALUE, transactionId.getAgentStartTime());
        Assertions.assertEquals(Long.MIN_VALUE, transactionId.getTransactionSequence());
    }


    @Test
    public void testParseTransactionIdByte_compatibility1() {
        long time = System.currentTimeMillis();
        ByteBuffer byteBuffer1 = ByteBuffer.wrap(TransactionIdParser.writeTransactionId(AGENT_ID, time, 2));
        ByteBuffer byteBuffer2 = writeTransactionId_for_compatibility(AGENT_ID, time, 2);
        Assertions.assertEquals(byteBuffer1, byteBuffer2);
    }

    @Test
    public void testParseTransactionIdByte_compatibility2() {
        long time = System.currentTimeMillis();
        ByteBuffer byteBuffer1 = ByteBuffer.wrap(TransactionIdParser.writeTransactionId(null, time, 2));
        ByteBuffer byteBuffer2 = writeTransactionId_for_compatibility(null, time, 2);
        Assertions.assertEquals(byteBuffer1, byteBuffer2);
    }


    private static ByteBuffer writeTransactionId_for_compatibility(String agentId, long agentStartTime, long transactionSequence) {
        final Buffer buffer = new AutomaticBuffer(1 + 5 + 24 + 10 + 10);
        buffer.putByte(TransactionIdParser.VERSION);
        buffer.putPrefixedString(agentId);
        buffer.putVLong(agentStartTime);
        buffer.putVLong(transactionSequence);
        return buffer.wrapByteBuffer();
    }

    @Test
    public void testParseTransactionId() {
        TransactionId transactionId = TransactionIdParser.parseTransactionId(AGENT_ID + TransactionIdParser.TRANSACTION_ID_DELIMITER + "1" + TransactionIdParser.TRANSACTION_ID_DELIMITER + "2");
        Assertions.assertEquals("test", transactionId.getAgentId());
        Assertions.assertEquals(1L, transactionId.getAgentStartTime());
        Assertions.assertEquals(2L, transactionId.getTransactionSequence());
    }

    @Test
    public void testParseTransactionId2() {
        TransactionId transactionId = TransactionIdParser.parseTransactionId(AGENT_ID + TransactionIdParser.TRANSACTION_ID_DELIMITER + "1" + TransactionIdParser.TRANSACTION_ID_DELIMITER + "2" + TransactionIdParser.TRANSACTION_ID_DELIMITER);
        Assertions.assertEquals(AGENT_ID, transactionId.getAgentId());
        Assertions.assertEquals(1L, transactionId.getAgentStartTime());
        Assertions.assertEquals(2L, transactionId.getTransactionSequence());
    }


    @Test
    public void testParseTransactionId_RpcHeaderDuplicateAdd_BugReproduce() {
        Assertions.assertThrows(Exception.class, () -> {
            // #27 http://yobi.navercorp.com/pinpoint/pinpoint/issue/27
            String id1 = AGENT_ID + TransactionIdParser.TRANSACTION_ID_DELIMITER + "1" + TransactionIdParser.TRANSACTION_ID_DELIMITER + "2";
            String id2 = AGENT_ID + TransactionIdParser.TRANSACTION_ID_DELIMITER + "1" + TransactionIdParser.TRANSACTION_ID_DELIMITER + "3";
            TransactionId transactionId = TransactionIdParser.parseTransactionId(id1 + ", " + id2);
            Assertions.assertEquals("test", transactionId.getAgentId());
            Assertions.assertEquals(1L, transactionId.getAgentStartTime());
            Assertions.assertEquals(2L, transactionId.getTransactionSequence());
        });
    }

    @Test
    public void validateAgentId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            TransactionIdParser.parseTransactionId("ag$$ent^1^2");
        });
    }

    @Test
    public void longAgentId() {
        String agentId = StringUtils.repeat('a', PinpointConstants.AGENT_ID_MAX_LEN);
        TransactionId transactionId = TransactionIdParser.parseTransactionId(agentId + "^1^2");
        Assertions.assertEquals(agentId, transactionId.getAgentId());
    }

    @Test
    public void tooLongAgentId() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            String agentId = StringUtils.repeat('a', PinpointConstants.AGENT_ID_MAX_LEN + 1);
            TransactionId transactionId = TransactionIdParser.parseTransactionId(agentId + "^1^2");
            Assertions.assertEquals(agentId, transactionId.getAgentId());
        });
    }

    @Test
    public void parseLong() {
        String longString = " 123 ";
        long l = TransactionIdParser.parseLong(longString, 1, 4);
        Assertions.assertEquals(123L, l);
    }

    @Test
    public void parseLong1() {
        String longString = "123";
        long l0 = TransactionIdParser.parseLong(longString, 0, 3);
        Assertions.assertEquals(123L, l0);

        long l1 = TransactionIdParser.parseLong(longString, 0, 2);
        Assertions.assertEquals(12L, l1);

        long l2 = TransactionIdParser.parseLong(longString, 1, 3);
        Assertions.assertEquals(23L, l2);
    }
}