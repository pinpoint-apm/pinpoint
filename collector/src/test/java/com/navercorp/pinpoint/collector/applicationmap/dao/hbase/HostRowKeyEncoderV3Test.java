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

package com.navercorp.pinpoint.collector.applicationmap.dao.hbase;

import com.navercorp.pinpoint.collector.applicationmap.dao.v3.HostRowKeyEncoderV3;
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.OneByteSimpleHash;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidAppRowKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.window.DefaultTimeSlot;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HostRowKeyEncoderV3Test {

    private final TimeSlot timeSlot = new DefaultTimeSlot();

    @Test
    public void testCreateRowKey() {
        String parentApp = "parentApp";
        long statisticsRowSlot = timeSlot.getTimeSlot(System.currentTimeMillis());
        ServiceType standAlone = ServiceType.STAND_ALONE;

        ByteHasher hasher = new OneByteSimpleHash(32);
        HostRowKeyEncoder rowKeyEncoder = new HostRowKeyEncoderV3(hasher);
        byte[] parentApps = rowKeyEncoder.encodeRowKey(parentApp, standAlone.getCode(), ServiceUid.DEFAULT_SERVICE_UID_CODE, statisticsRowSlot);

        UidAppRowKey rowKey = UidAppRowKey.read(hasher.getSaltKey().size(), parentApps);

        Assertions.assertEquals(ServiceUid.DEFAULT_SERVICE_UID_CODE, rowKey.getServiceUid(), "serviceUid check");
        Assertions.assertEquals(parentApp, rowKey.getApplicationName(), "applicationName check");
        Assertions.assertEquals(standAlone.getCode(), rowKey.getServiceType(), "serviceType check");
        Assertions.assertEquals(statisticsRowSlot, rowKey.getTimestamp(), "time check");
    }
}
