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

package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.hadoop.hbase.util.Bytes;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TraceIndexScanKeyTest {

    private static final int SALT_KEY_SIZE = 0;
    // applicationNameHash(4) + serviceUid(4) + serviceType(4) + reverseTimestamp(8)
    private static final int PREFIX_SIZE = 4 + 4 + 4 + 8;

    private static final String APPLICATION_NAME = "testApp";
    private static final int SERVICE_UID = 7;
    private static final int SERVICE_TYPE_CODE = ServiceType.TEST.getCode();

    private static byte[] rowKey(long timestamp) {
        return TraceIndexRowKeyUtils.createRowKeyWithSaltSize(
                SALT_KEY_SIZE, SERVICE_UID, APPLICATION_NAME, SERVICE_TYPE_CODE, timestamp,
                1L, 100, 0, "agentId"
        );
    }

    @Test
    public void scanKey_prefix_matches_rowKey() {
        Range range = Range.between(1000L, 2000L);
        TraceIndexScanKey scanKey = new TraceIndexScanKey(SERVICE_UID, APPLICATION_NAME, SERVICE_TYPE_CODE, range);

        Assertions.assertThat(Arrays.copyOf(scanKey.startKey(), PREFIX_SIZE))
                .isEqualTo(Arrays.copyOf(rowKey(range.getTo()), PREFIX_SIZE));
        Assertions.assertThat(Arrays.copyOf(scanKey.endKey(), PREFIX_SIZE))
                .isEqualTo(Arrays.copyOf(rowKey(range.getFrom()), PREFIX_SIZE));
    }

    @Test
    public void startKey_is_lower_than_endKey() {
        Range range = Range.between(1000L, 2000L);
        TraceIndexScanKey scanKey = new TraceIndexScanKey(SERVICE_UID, APPLICATION_NAME, SERVICE_TYPE_CODE, range);

        // reversed timestamp: the later time produces the smaller row key
        Assertions.assertThat(Bytes.compareTo(scanKey.startKey(), scanKey.endKey())).isNegative();
    }

    @Test
    public void rowKey_is_within_scan_range() {
        Range range = Range.between(1000L, 2000L);
        TraceIndexScanKey scanKey = new TraceIndexScanKey(SERVICE_UID, APPLICATION_NAME, SERVICE_TYPE_CODE, range);
        byte[] startKey = scanKey.startKey();
        byte[] endKey = scanKey.endKey();

        byte[] inside = rowKey(1500L);
        byte[] before = rowKey(999L);
        byte[] after = rowKey(2001L);

        Assertions.assertThat(Bytes.compareTo(startKey, inside)).isNegative();
        Assertions.assertThat(Bytes.compareTo(inside, endKey)).isNegative();
        // 999 < from : reversed timestamp is larger than endKey -> excluded
        Assertions.assertThat(Bytes.compareTo(before, endKey)).isPositive();
        // 2001 > to : reversed timestamp is smaller than startKey -> excluded
        Assertions.assertThat(Bytes.compareTo(after, startKey)).isNegative();
    }
}
