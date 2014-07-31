package com.nhn.pinpoint.collector.dao.hbase;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.util.DefaultTimeSlot;
import com.nhn.pinpoint.common.util.TimeSlot;
import com.nhn.pinpoint.common.util.TimeUtils;
import junit.framework.Assert;
import org.apache.hadoop.hbase.client.HTable;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

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