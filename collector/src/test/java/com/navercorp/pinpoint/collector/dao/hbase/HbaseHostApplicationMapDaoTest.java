/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstatns;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.server.util.DefaultTimeSlot;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import com.navercorp.pinpoint.common.util.TimeUtils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HbaseHostApplicationMapDaoTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TimeSlot timeSlot = new DefaultTimeSlot();

    @Test
    public void testCreateRowKey() throws Exception {
        long statisticsRowSlot = timeSlot.getTimeSlot(System.currentTimeMillis());
        byte[] parentApps = HbaseHostApplicationMapDao.createRowKey0("parentApp", ServiceType.STAND_ALONE.getCode(), statisticsRowSlot, null);
        logger.debug("rowKey size:{}", parentApps.length);

        Buffer readBuffer = new FixedBuffer(parentApps);
        String appName = readBuffer.readPadStringAndRightTrim(HbaseTableConstatns.APPLICATION_NAME_MAX_LEN);
        short code = readBuffer.readShort();
        long time = TimeUtils.recoveryTimeMillis(readBuffer.readLong());

        Assert.assertEquals("applicationName check",appName, "parentApp");
        Assert.assertEquals("serviceType check", code, ServiceType.STAND_ALONE.getCode());
        Assert.assertEquals("time check", statisticsRowSlot, time);
    }
}