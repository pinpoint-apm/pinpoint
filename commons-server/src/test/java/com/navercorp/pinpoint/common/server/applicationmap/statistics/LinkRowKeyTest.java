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

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LinkRowKeyTest {

    @Test
    public void makeRowKey() {
        String applicationName = "TESTAPP";
        short serviceType = 123;
        long timestamp = System.currentTimeMillis();

        byte[] bytes = LinkRowKey.makeRowKey(ByteSaltKey.NONE.size(), applicationName, serviceType, timestamp);

        Buffer buffer = new FixedBuffer(bytes);
        String readApplicationName = buffer.read2PrefixedString();
        short readApplicationType = buffer.readShort();
        Assertions.assertEquals(applicationName, readApplicationName);
        Assertions.assertEquals(serviceType, readApplicationType);
    }


    @Test
    public void linkRowKey() {
        String applicationName = "TESTAPP";
        ServiceType serviceType = ServiceType.TEST;
        long timestamp = System.currentTimeMillis();

        RowKey linkRowKey = LinkRowKey.of(applicationName, serviceType, timestamp);
        byte[] rowKey = linkRowKey.getRowKey(0);

        LinkRowKey read = LinkRowKey.read(0, rowKey);

        Assertions.assertEquals(linkRowKey, read);
    }

}