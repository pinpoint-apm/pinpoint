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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.hbase.wd.RangeOneByteSimpleHash;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceRowKeyEncoderV2Test {

    private final Random random = new Random();
    private final RowKeyDistributorByHashPrefix distributorByHashPrefix = newDistributorByHashPrefix();

    private RowKeyDistributorByHashPrefix newDistributorByHashPrefix() {
        int startOffsetForMod = 32;
        int endOffsetForMod = 40;
        int maxBucketSize = 256;
        ByteHasher oneByteSimpleHash = new RangeOneByteSimpleHash(startOffsetForMod, endOffsetForMod, maxBucketSize);
        return new RowKeyDistributorByHashPrefix(oneByteSimpleHash);
    }

    private final RowKeyEncoder<ServerTraceId> traceRowKeyEncoder = new TraceRowKeyEncoderV2(distributorByHashPrefix);

    private final RowKeyDecoder<ServerTraceId> traceRowKeyDecoder = new TraceRowKeyDecoderV2(ByteSaltKey.SALT);

    @Test
    public void encodeRowKey() {

        ServerTraceId spanTransactionId = new PinpointServerTraceId("traceAgentId", System.currentTimeMillis(), random.nextLong(0, 10000));

        byte[] rowKey = traceRowKeyEncoder.encodeRowKey(spanTransactionId);
        ServerTraceId transactionId = traceRowKeyDecoder.decodeRowKey(rowKey);

        Assertions.assertEquals(transactionId, spanTransactionId);

    }

}