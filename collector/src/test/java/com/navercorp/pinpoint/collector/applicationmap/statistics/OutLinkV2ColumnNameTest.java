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

package com.navercorp.pinpoint.collector.applicationmap.statistics;

import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v2.OutLinkV2ColumnName;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OutLinkV2ColumnNameTest {

    @Test
    void testEquals() {

        ServiceType calleeServiceType = ServiceType.TEST;
        short columnSlotNumber = 2;

        ColumnName name1 = OutLinkV2ColumnName.histogram("callerAgent", calleeServiceType, "calleeAgent", "host", columnSlotNumber);
        ColumnName name2 = OutLinkV2ColumnName.histogram("callerAgent", calleeServiceType, "calleeAgent", "host", columnSlotNumber);
        Assertions.assertEquals(name1, name2);

    }
}