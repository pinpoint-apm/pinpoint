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

package com.navercorp.pinpoint.collector.applicationmap.dao.hbase;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.hbase.wd.SaltKey;
import com.navercorp.pinpoint.common.timeseries.window.DefaultTimeSlot;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.TimeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HbaseHostApplicationMapDaoTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TimeSlot timeSlot = new DefaultTimeSlot();

    @Test
    public void testCreateRowKey() {
        String parentApp = "parentApp";
        long statisticsRowSlot = timeSlot.getTimeSlot(System.currentTimeMillis());
        ServiceType standAlone = ServiceType.STAND_ALONE;
        SaltKey saltKey = ByteSaltKey.SALT;

        byte[] parentApps = HbaseHostApplicationMapDao.createRowKey0(saltKey.size(), parentApp, standAlone.getCode(), statisticsRowSlot, null);
        logger.debug("rowKey size:{}", parentApps.length);

        Buffer readBuffer = new FixedBuffer(parentApps);
        readBuffer.setOffset(saltKey.size());
        String appName = readBuffer.readPadStringAndRightTrim(HbaseTableConstants.APPLICATION_NAME_MAX_LEN);
        short code = readBuffer.readShort();
        long time = TimeUtils.recoveryTimeMillis(readBuffer.readLong());

        Assertions.assertEquals(parentApp, appName, "applicationName check");
        Assertions.assertEquals(standAlone.getCode(), code, "serviceType check");
        Assertions.assertEquals(statisticsRowSlot, time, "time check");
    }
}