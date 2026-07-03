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

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UidLinkRowKeyTest {

    @Test
    void roundTrip_withLinkServiceUid() {
        UidLinkRowKey key = (UidLinkRowKey) UidLinkRowKey.of(
                1, "self-app", ServiceType.STAND_ALONE, 3000,
                7, "link-app", ServiceType.JAVA.getCode(), "sub-link");

        byte[] bytes = key.getRowKey(1);
        UidLinkRowKey read = UidLinkRowKey.read(1, bytes);

        assertThat(read).isEqualTo(key);
        assertThat(read.getLinkServiceUid()).isEqualTo(7);
    }

    @Test
    void roundTrip_defaultServiceUid_isStored() {
        // DEFAULT(0) is a real serviceUid and must be persisted, not treated as unknown
        UidLinkRowKey key = (UidLinkRowKey) UidLinkRowKey.of(
                1, "self-app", ServiceType.STAND_ALONE, 3000,
                ServiceUid.DEFAULT_SERVICE_UID_CODE, "link-app", ServiceType.JAVA.getCode(), "sub-link");

        byte[] bytes = key.getRowKey(1);
        UidLinkRowKey read = UidLinkRowKey.read(1, bytes);

        assertThat(read.getLinkServiceUid()).isEqualTo(ServiceUid.DEFAULT_SERVICE_UID_CODE);
        assertThat(read).isEqualTo(key);
    }

    @Test
    void legacyRow_withoutTail_readsAsDefault() {
        // a legacy row omits the trailing serviceUid; simulate it by stripping the tail int
        byte[] rowKey = UidLinkRowKey.makeRowKey(0,
                1, "self-app", ServiceType.STAND_ALONE.getCode(), 3000,
                7, "link-app", ServiceType.JAVA.getCode(), "sub-link");
        byte[] legacy = Arrays.copyOf(rowKey, rowKey.length - 4);

        UidLinkRowKey read = UidLinkRowKey.read(0, legacy);

        assertThat(read.getServiceUid()).isEqualTo(1);
        assertThat(read.getLinkApplicationName()).isEqualTo("link-app");
        assertThat(read.getLinkServiceType()).isEqualTo(ServiceType.JAVA.getCode());
        // no non-DEFAULT legacy data exists, so a missing tail resolves to DEFAULT
        assertThat(read.getLinkServiceUid()).isEqualTo(ServiceUid.DEFAULT_SERVICE_UID_CODE);
    }

    @Test
    void truncatedTail_failsFast() {
        byte[] rowKey = UidLinkRowKey.makeRowKey(0,
                1, "self-app", ServiceType.STAND_ALONE.getCode(), 3000,
                7, "link-app", ServiceType.JAVA.getCode(), "sub-link");

        // 1~3 trailing bytes is neither a legacy row (0) nor a valid tail (4) -> corrupt
        for (int truncated = 1; truncated <= 3; truncated++) {
            byte[] corrupt = Arrays.copyOf(rowKey, rowKey.length - truncated);
            assertThatThrownBy(() -> UidLinkRowKey.read(0, corrupt))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
