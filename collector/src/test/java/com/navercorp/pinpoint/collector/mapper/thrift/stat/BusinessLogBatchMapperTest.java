package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogBo;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogV1Bo;
import com.navercorp.pinpoint.thrift.dto.TBusinessLog;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogBatch;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogV1;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by suny on 2018/2/3.
 */
public class BusinessLogBatchMapperTest {
    @InjectMocks
    BusinessLogBatchMapper businessLogBatchMapper;
    @Mock
    TBusinessLogBatch tBusinessLogBatch;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMap() throws Exception {
        tBusinessLogBatch.clear();
        Assert.assertNull(businessLogBatchMapper.map(tBusinessLogBatch));
    }

    @Test
    public void mapperTest() {
        mapperTest0();
    }

    private void mapperTest0() {
        for (int i = 0; i < 10; i++) {
            TBusinessLogBatch tBusinessLogBatch = create();
            BusinessLogBo businessLogBo = convert(tBusinessLogBatch);
            verify(tBusinessLogBatch, businessLogBo);
        }
    }


    protected TBusinessLogBatch create() {
        //create TBusinessLogBatch
        List<TBusinessLog> listBusinessLog = new ArrayList<TBusinessLog>();
        TBusinessLogBatch tBusinessLogBatch = new TBusinessLogBatch("agentId", System.currentTimeMillis(), listBusinessLog);
        //create TBusinessLog
        TBusinessLog tBusinessLog = new TBusinessLog();
        listBusinessLog.add(tBusinessLog);
        //create TBusinessLogV1
        TBusinessLogV1 tBusinessLogV1 = new TBusinessLogV1();
        tBusinessLogV1.setTime("time");
        tBusinessLogV1.setThreadName("threadName");
        tBusinessLogV1.setLogLevel("level");
        tBusinessLogV1.setClassName("class");
        tBusinessLogV1.setMessage("message");
        tBusinessLogV1.setTransactionId("transactionId");
        tBusinessLogV1.setSpanId("spanId");
        tBusinessLog.addToBusinessLogV1s(tBusinessLogV1);
        return tBusinessLogBatch;
    }


    protected BusinessLogBo convert(TBusinessLogBatch original) {
        return businessLogBatchMapper.map(original);
    }


    protected void verify(TBusinessLogBatch original, BusinessLogBo mappedStatDataPoint) {
        List<TBusinessLog> listTBusinessLog = original.getBusinessLogs();
        int originalSize = 0;
        for (TBusinessLog tBusinessLog : listTBusinessLog) {
            originalSize += tBusinessLog.getBusinessLogV1sSize();
        }
        int mappedSize = mappedStatDataPoint.getBusinessLogs().size();
        TBusinessLogV1 tBusinessLogV1 = null;
        BusinessLogV1Bo businessLogV1Bo = null;
        if (originalSize > 0)
            tBusinessLogV1 = listTBusinessLog.get(0).getBusinessLogV1s().get(0);
        if (mappedSize > 0)
            businessLogV1Bo = mappedStatDataPoint.getBusinessLogs().get(0);
        Assert.assertEquals("agentId", original.getAgentId(), mappedStatDataPoint.getAgentId());
        Assert.assertEquals("startTimestamp", original.getStartTimestamp(), mappedStatDataPoint.getStartTimestamp());
        Assert.assertEquals("size", originalSize, mappedSize);
        if (tBusinessLogV1 != null && businessLogV1Bo != null) {
            Assert.assertEquals("className", tBusinessLogV1.getClassName(), businessLogV1Bo.getClassName());
            Assert.assertEquals("logLevel", tBusinessLogV1.getLogLevel(), businessLogV1Bo.getLogLevel());
            Assert.assertEquals("message", tBusinessLogV1.getMessage(), businessLogV1Bo.getMessage());
            Assert.assertEquals("transactionId", tBusinessLogV1.getTransactionId(), businessLogV1Bo.getTransactionId());
            Assert.assertEquals("spanId", tBusinessLogV1.getSpanId(), businessLogV1Bo.getSpanId());
            Assert.assertEquals("threadName", tBusinessLogV1.getThreadName(), businessLogV1Bo.getThreadName());
            Assert.assertEquals("time", tBusinessLogV1.getTime(), businessLogV1Bo.getTime());
        }
    }
}