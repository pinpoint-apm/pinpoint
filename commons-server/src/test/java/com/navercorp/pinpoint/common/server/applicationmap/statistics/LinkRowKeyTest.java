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

import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LinkRowKeyTest {

    @Test
    public void makeRowKey() {
        String applicationName = "TESTAPP";
        short serviceType = 123;
        long time = System.currentTimeMillis();

        byte[] bytes = LinkRowKey.makeRowKey(ByteSaltKey.NONE.size(), applicationName, serviceType, time);

        Assertions.assertEquals(applicationName, ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(bytes));
        Assertions.assertEquals(serviceType, ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(bytes));
    }
}