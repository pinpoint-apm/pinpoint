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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.RangeDoubleHash;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class UidAppRowKeyTest {

    public static final int KEY_RANGE = DistributorConfiguration.UID_START_KEY_RANGE;

    static int secondaryKeySpace = 4;

    ByteHasher hasher = RangeDoubleHash.ofSecondary(0, KEY_RANGE, ByteHasher.MAX_BUCKETS, secondaryKeySpace, KEY_RANGE, KEY_RANGE + 4);

    @Test
    void uidRowKey() {
        RowKey rowKey = UidAppRowKey.of(12, "appName", ServiceType.STAND_ALONE, 3000);

        byte[] rowKeyBytes = rowKey.getRowKey(1);

        UidAppRowKey read = UidAppRowKey.read(1, rowKeyBytes);


        Assertions.assertEquals(rowKey, read);
    }

    @Test
    void hashing() {
        int saltKeySize = hasher.getSaltKey().size();

        RowKey rowKey = UidAppRowKey.of(12, "appName", ServiceType.STAND_ALONE, 3000);
        byte[] bytes = rowKey.getRowKey(saltKeySize);

        byte[] saltRowKey = hasher.writeSaltKey(bytes);

        UidAppRowKey read = UidAppRowKey.read(saltKeySize, saltRowKey);

        Assertions.assertEquals(rowKey, read);
    }

    @Test
    void hashing_secondaryKeySpace() {
        int saltKeySize = hasher.getSaltKey().size();

        Set<Byte> saltKeySet = new HashSet<>();

        long timestamp = 9000;
        int second = 1000;
        for (int i = 0; i < 16; i++) {
            RowKey rowKey = UidAppRowKey.of(12, "appName", ServiceType.STAND_ALONE, timestamp += second);
            byte[] saltKey = hasher.writeSaltKey(rowKey.getRowKey(saltKeySize));
            saltKeySet.add(saltKey[0]);
        }

        assertThat(saltKeySet).hasSize(secondaryKeySpace);
    }

    @Test
    void makeRow() {
        UidAppRowKey key = (UidAppRowKey) UidAppRowKey.of(ServiceUid.DEFAULT_SERVICE_UID_CODE,
                "a".repeat(PinpointConstants.APPLICATION_NAME_MAX_LEN_V3),
                ServiceType.STAND_ALONE, 1000);

        byte[] bytes = UidAppRowKey.makeRowKey(0,
                key.getServiceUid(),
                key.getApplicationName(),
                key.getServiceType(),
                key.getTimestamp());

        UidAppRowKey rowKey = UidAppRowKey.read(0, bytes);
        Assertions.assertEquals(key, rowKey);
    }

    @Test
    void makeRow_error() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            UidAppRowKey.makeRowKey(0,
                    ServiceUid.DEFAULT_SERVICE_UID_CODE,
                    "a".repeat(UidPrefix.KEY_SIZE + 1),
                    ServiceType.STAND_ALONE.getCode(),
                    1000);
        });
    }

    @Test
    void makeRow_254() {
        byte[] bytes = UidAppRowKey.makeRowKey(0,
                ServiceUid.DEFAULT_SERVICE_UID_CODE,
                "a".repeat(PinpointConstants.APPLICATION_NAME_MAX_LEN_V3),
                ServiceType.STAND_ALONE.getCode(),
                1000);

        UidAppRowKey rowKey = UidAppRowKey.read(0, bytes);
        Assertions.assertEquals(PinpointConstants.APPLICATION_NAME_MAX_LEN_V3, rowKey.getApplicationName().length());
    }
}