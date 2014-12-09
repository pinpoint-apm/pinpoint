package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.hbase.HbaseHostApplicationMapDao;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.util.DefaultTimeSlot;
import com.navercorp.pinpoint.common.util.TimeSlot;
import com.navercorp.pinpoint.common.util.TimeUtils;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HbaseHostApplicationMapDaoTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TimeSlot timeSlot = new DefaultTimeSlot();

    @Test
    public void testCreateRowKey() throws Exception {
        HbaseHostApplicationMapDao dao = new HbaseHostApplicationMapDao();
        long statisticsRowSlot = timeSlot.getTimeSlot(System.currentTimeMillis());
        byte[] parentApps = dao.createRowKey0("parentApp", ServiceType.TOMCAT.getCode(), statisticsRowSlot, null);
        logger.debug("rowKey size:{}", parentApps.length);

        Buffer readBuffer = new FixedBuffer(parentApps);
        String appName = readBuffer.readPadStringAndRightTrim(HBaseTables.APPLICATION_NAME_MAX_LEN);
        short code = readBuffer.readShort();
        long time = TimeUtils.recoveryTimeMillis(readBuffer.readLong());

        Assert.assertEquals("applicationName check",appName, "parentApp");
        Assert.assertEquals("serviceType check", code, ServiceType.TOMCAT.getCode());
        Assert.assertEquals("time check", statisticsRowSlot, time);
    }
}