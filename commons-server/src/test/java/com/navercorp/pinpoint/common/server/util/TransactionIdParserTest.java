package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

class TransactionIdParserTest {

    public static final String AGENT_ID = "test";

    @Test
    public void testParseTransactionIdByte_AgentIdisNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            long time = System.currentTimeMillis();
            byte[] bytes = TransactionIdUtils.formatBytes(null, time, 1);
            TransactionIdParser.parse(bytes, null);
        });
    }

    @Test
    public void testParseTransactionIdByte1() {
        long time = System.currentTimeMillis();
        byte[] bytes = TransactionIdUtils.formatBytes(AGENT_ID, time, 2);
        TransactionId transactionId = TransactionIdParser.parse(bytes, AGENT_ID);
        Assertions.assertEquals(AGENT_ID, transactionId.getAgentId());
        Assertions.assertEquals(time, transactionId.getAgentStartTime());
        Assertions.assertEquals(2L, transactionId.getTransactionSequence());
    }

    @Test
    public void testParseTransactionIdByte2() {
        long time = Long.MAX_VALUE;
        byte[] bytes = TransactionIdUtils.formatBytes(AGENT_ID, time, Long.MAX_VALUE);
        TransactionId transactionId = TransactionIdParser.parse(bytes, AGENT_ID);
        Assertions.assertEquals(AGENT_ID, transactionId.getAgentId());
        Assertions.assertEquals(Long.MAX_VALUE, transactionId.getAgentStartTime());
        Assertions.assertEquals(Long.MAX_VALUE, transactionId.getTransactionSequence());
    }

    @Test
    public void testParseTransactionIdByte3() {
        long time = Long.MIN_VALUE;
        byte[] bytes = TransactionIdUtils.formatBytes(AGENT_ID, time, Long.MIN_VALUE);
        TransactionId transactionId = TransactionIdParser.parse(bytes, AGENT_ID);
        Assertions.assertEquals(AGENT_ID, transactionId.getAgentId());
        Assertions.assertEquals(Long.MIN_VALUE, transactionId.getAgentStartTime());
        Assertions.assertEquals(Long.MIN_VALUE, transactionId.getTransactionSequence());
    }


    @Test
    public void testParseTransactionIdByte_compatibility1() {
        long time = System.currentTimeMillis();
        ByteBuffer byteBuffer1 = ByteBuffer.wrap(TransactionIdUtils.formatBytes(AGENT_ID, time, 2));
        ByteBuffer byteBuffer2 = writeTransactionId_for_compatibility(AGENT_ID, time, 2);
        Assertions.assertEquals(byteBuffer1, byteBuffer2);
    }

    @Test
    public void testParseTransactionIdByte_compatibility2() {
        long time = System.currentTimeMillis();
        ByteBuffer byteBuffer1 = ByteBuffer.wrap(TransactionIdUtils.formatBytes(null, time, 2));
        ByteBuffer byteBuffer2 = writeTransactionId_for_compatibility(null, time, 2);
        Assertions.assertEquals(byteBuffer1, byteBuffer2);
    }


    private static ByteBuffer writeTransactionId_for_compatibility(String agentId, long agentStartTime, long transactionSequence) {
        final Buffer buffer = new AutomaticBuffer(1 + 5 + 24 + 10 + 10);
        buffer.putByte(TransactionIdUtils.VERSION);
        buffer.putPrefixedString(agentId);
        buffer.putVLong(agentStartTime);
        buffer.putVLong(transactionSequence);
        return buffer.wrapByteBuffer();
    }

}