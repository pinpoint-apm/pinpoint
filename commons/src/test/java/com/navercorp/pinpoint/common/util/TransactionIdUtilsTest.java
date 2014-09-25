package com.nhn.pinpoint.common.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class TransactionIdUtilsTest {
    @Test
    public void testParseTransactionId() {
        TransactionId transactionId = TransactionIdUtils.parseTransactionId("test" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "1" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "2");
        Assert.assertEquals(transactionId.getAgentId(), "test");
        Assert.assertEquals(transactionId.getAgentStartTime(), 1L);
        Assert.assertEquals(transactionId.getTransactionSequence(), 2L);
    }

    @Test
    public void testParseTransactionId2() {
        TransactionId transactionId = TransactionIdUtils.parseTransactionId("test" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "1" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "2" + TransactionIdUtils.TRANSACTION_ID_DELIMITER);
        Assert.assertEquals(transactionId.getAgentId(), "test");
        Assert.assertEquals(transactionId.getAgentStartTime(), 1L);
        Assert.assertEquals(transactionId.getTransactionSequence(), 2L);
    }


    @Test
    public void testParseTransactionIdByte() {
        long time = System.currentTimeMillis();
        byte[] bytes = TransactionIdUtils.formatBytes("test", time, 2);
        TransactionId transactionId = TransactionIdUtils.parseTransactionId(bytes);
        Assert.assertEquals(transactionId.getAgentId(), "test");
        Assert.assertEquals(transactionId.getAgentStartTime(), time);
        Assert.assertEquals(transactionId.getTransactionSequence(), 2L);
    }

    @Test
    public void testParseTransactionIdByte_AgentIdisNull() {
        long time = System.currentTimeMillis();
        byte[] bytes = TransactionIdUtils.formatBytes(null, time, 1);
        TransactionId transactionId = TransactionIdUtils.parseTransactionId(bytes);
        Assert.assertEquals(transactionId.getAgentId(), null);
        Assert.assertEquals(transactionId.getAgentStartTime(), time);
        Assert.assertEquals(transactionId.getTransactionSequence(), 1L);
    }

}
