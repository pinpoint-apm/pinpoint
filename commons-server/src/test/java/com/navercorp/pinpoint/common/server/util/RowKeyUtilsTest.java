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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class RowKeyUtilsTest {

    @Test
    void concatFixedByteAndLongFuzzySlot() {
        byte[] agentId = "agentId".getBytes(StandardCharsets.UTF_8);
        int length = 24;
        long timestamp = 100;
        byte slot = 9;

        byte[] rowKey1 = RowKeyUtils.concatFixedByteAndLong(agentId, length, timestamp);
        byte[] rowKey2 = RowKeyUtils.concatFixedByteAndLongFuzzySlot(agentId, length, timestamp, slot);

        Assertions.assertArrayEquals(rowKey1, Arrays.copyOfRange(rowKey2, 0, rowKey2.length - 1));
        Assertions.assertEquals(slot, rowKey2[rowKey2.length - 1]);
    }

    @Test
    void concatFixedByteAndLongFuzzySlot_prefix0() {
        byte[] agentId = "agentId".getBytes(StandardCharsets.UTF_8);
        int length = 24;
        long timestamp = 100;
        byte slot = 9;

        byte[] rowKey1 = RowKeyUtils.concatFixedByteAndLongFuzzySlot(agentId, length, timestamp, slot);
        byte[] rowKey2 = RowKeyUtils.concatFixedByteAndLongFuzzySlot(0, agentId, length, timestamp, slot);

        Assertions.assertArrayEquals(rowKey1, rowKey2);
        Assertions.assertEquals(slot, rowKey2[rowKey2.length - 1]);
    }

    @Test
    void concatFixedByteAndLongFuzzySlot_prefix1() {
        byte[] agentId = "agentId".getBytes(StandardCharsets.UTF_8);
        int length = 24;
        long timestamp = 100;
        byte slot = 9;

        byte[] rowKey1 = RowKeyUtils.concatFixedByteAndLongFuzzySlot(agentId, length, timestamp, slot);
        byte[] rowKey2 = RowKeyUtils.concatFixedByteAndLongFuzzySlot(1, agentId, length, timestamp, slot);

        Assertions.assertArrayEquals(rowKey1, Arrays.copyOfRange(rowKey2, 1, rowKey2.length));
    }
}