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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.hbase.distributor.RangeOneByteSimpleHash;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceRowKeyEncoderV2Test {

    private RowKeyDistributorByHashPrefix distributorByHashPrefix = newDistributorByHashPrefix();

    private RowKeyDistributorByHashPrefix newDistributorByHashPrefix() {
        int startOffsetForMod = 32;
        int endOffsetForMod = 40;
        int maxBucketSize = 256;
        RowKeyDistributorByHashPrefix.Hasher oneByteSimpleHash = new RangeOneByteSimpleHash(startOffsetForMod, endOffsetForMod, maxBucketSize);
        return new RowKeyDistributorByHashPrefix(oneByteSimpleHash);
    }

    private RowKeyEncoder<TransactionId> traceRowKeyEncoder = new TraceRowKeyEncoderV2(distributorByHashPrefix);

    private RowKeyDecoder<TransactionId> traceRowKeyDecoder = new TraceRowKeyDecoderV2();

    @Test
    public void encodeRowKey() {

        TransactionId spanTransactionId = new TransactionId("traceAgentId", System.currentTimeMillis(), RandomUtils.nextLong(0, 10000));

        byte[] rowKey = traceRowKeyEncoder.encodeRowKey(spanTransactionId);
        TransactionId transactionId = traceRowKeyDecoder.decodeRowKey(rowKey);

        Assertions.assertEquals(transactionId, spanTransactionId);

    }

}