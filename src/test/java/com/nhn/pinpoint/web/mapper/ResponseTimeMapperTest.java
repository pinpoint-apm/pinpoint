package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.vo.ResponseTime;
import junit.framework.Assert;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

/**
 * @author emeroad
 */
public class ResponseTimeMapperTest {

    @Test
    public void testResponseTimeMapperTest() throws Exception {
        ResponseTimeMapper responseTimeMapper = new ResponseTimeMapper();
        ResponseTime responseTime = new ResponseTime("applicaionName", ServiceType.TOMCAT.getCode(), System.currentTimeMillis());

        Buffer buffer = new AutomaticBuffer();
        HistogramSlot histogramSlot = ServiceType.TOMCAT.getHistogramSchema().findHistogramSlot(1000);
        short histogramSlotTime = histogramSlot.getSlotTime();
        buffer.put(histogramSlotTime);
        buffer.put(Bytes.toBytes("agent"));

        responseTimeMapper.recordColumn(responseTime, buffer.getBuffer(), Bytes.toBytes(1L), 0);

        Histogram agentHistogram = responseTime.findHistogram("agent");
        long fastCount = agentHistogram.getFastCount();
        Assert.assertEquals(fastCount, 1);
        long normal = agentHistogram.getNormalCount();
        Assert.assertEquals(normal, 0);
        long slow = agentHistogram.getSlowCount();
        Assert.assertEquals(slow, 0);

    }
}
