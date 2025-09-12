/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.applicationmap.statistics;

import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.RangeDoubleHash;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class UidLinkRowKeyTest {

    public static final int KEY_RANGE = 262;

    static int secondaryKeySpace = 4;

    ByteHasher hasher = RangeDoubleHash.ofSecondary(0, KEY_RANGE, ByteHasher.MAX_BUCKETS, secondaryKeySpace, KEY_RANGE, 270);

    @Test
    void uidRowKey() {
        RowKey rowKey = UidLinkRowKey.of(12, "appName", ServiceType.STAND_ALONE, 1000);

        byte[] rowKeyBytes = rowKey.getRowKey(1);

        UidLinkRowKey read = UidLinkRowKey.read(1, rowKeyBytes);

        Assertions.assertEquals(rowKey, read);
    }

    @Test
    void hashing() {
        int saltKeySize = hasher.getSaltKey().size();

        RowKey rowKey = UidLinkRowKey.of(12, "appName", ServiceType.STAND_ALONE, 1000);
        byte[] bytes = rowKey.getRowKey(saltKeySize);

        byte[] saltRowKey = hasher.writeSaltKey(bytes);

        UidLinkRowKey read = UidLinkRowKey.read(saltKeySize, saltRowKey);

        Assertions.assertEquals(rowKey, read);
    }

    @Test
    void hashing_secondaryKeySpace() {
        int saltKeySize = hasher.getSaltKey().size();

        Set<Byte> saltKeySet = new HashSet<>();

        long timestamp = 1000;
        for (int i = 0; i < 16; i++) {
            RowKey rowKey = UidLinkRowKey.of(12, "appName", ServiceType.STAND_ALONE, timestamp++);
            byte[] saltKey = hasher.writeSaltKey(rowKey.getRowKey(saltKeySize));
            saltKeySet.add(saltKey[0]);
        }

        assertThat(saltKeySet).hasSize(secondaryKeySpace);
    }
}