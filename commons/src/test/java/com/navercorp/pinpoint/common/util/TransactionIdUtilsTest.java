/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

import org.junit.Assert;

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

    @Test(expected = Exception.class)
    public void testParseTransactionId_RpcHeaderDuplicateAdd_BugReproduce() {
        // #27 http://yobi.navercorp.com/pinpoint/pinpoint/issue/27
        String id1 = "test" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "1" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "2";
        String id2 = "test" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "1" + TransactionIdUtils.TRANSACTION_ID_DELIMITER + "3";
        TransactionId transactionId = TransactionIdUtils.parseTransactionId(id1 + ", " + id2);
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
