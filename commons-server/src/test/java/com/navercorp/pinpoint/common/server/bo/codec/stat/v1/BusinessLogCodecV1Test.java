package com.navercorp.pinpoint.common.server.bo.codec.stat.v1;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.BusinessLogDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.BusinessLogDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogV1Bo;
import com.navercorp.pinpoint.thrift.dto.TBusinessLog;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogBatch;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogV1;
import org.apache.hadoop.hbase.shaded.org.apache.avro.generic.GenericData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Created by suny on 2018/2/3.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class BusinessLogCodecV1Test {
    @Autowired
    BusinessLogCodecV1 businessLogCodecV1;

    @Autowired
    BusinessLogDataPointCodec businessLogDataPointCodec;

    @Autowired
    BusinessLogDataPointCodec codec;

    private final int TIMESTAMP_SIZE = 10;
    private  long firstTimestamps;
    private final int BUSINESSLOGV1_SIZE = 10;
    private final String AGENTID = "agentId";
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetVersion() throws Exception {
        businessLogCodecV1.getVersion();
        Assert.assertEquals(businessLogCodecV1.getVersion(),1);
    }

    private  List<Long> createTimestamps(){
        List<Long> timestamps = new ArrayList<Long>();
        firstTimestamps = System.currentTimeMillis();
        long current = firstTimestamps;
        for(int i = 0; i < TIMESTAMP_SIZE; i++){
            timestamps.add(current);
            current += 10;
        }
        return timestamps;
    }

    @Test
    public void testTimestamps(){
        List<Long> origintimestamps = createTimestamps();
        Buffer encodeValueBuffer = new AutomaticBuffer();
        businessLogDataPointCodec.encodeTimestamps(encodeValueBuffer,origintimestamps);
        Buffer decodeValueBuffer = new FixedBuffer(encodeValueBuffer.getBuffer());
        List<Long> desttimestamps = businessLogDataPointCodec.decodeTimestamps(firstTimestamps,decodeValueBuffer,TIMESTAMP_SIZE);
        for(int i = 0; i < TIMESTAMP_SIZE; i++){
            Assert.assertEquals(origintimestamps.get(i),desttimestamps.get(i));
        }
    }

    private List<BusinessLogV1Bo> createBusinessLogV1BO(){
         List<BusinessLogV1Bo> list = new ArrayList<BusinessLogV1Bo>();
         for(int i = 0; i < BUSINESSLOGV1_SIZE; i++){
             BusinessLogV1Bo log = new BusinessLogV1Bo();
             log.setAgentId(AGENTID);
             log.setClassName("className" + i);
             log.setLogLevel("logLevel" + i);
             log.setMessage("message" + i);
             log.setSpanId("spanId" + i);
             log.setStartTimestamp(System.currentTimeMillis());
             log.setThreadName("threadName" + i);
             log.setTime("time" + i);
             log.setTimestamp(System.currentTimeMillis());
             log.setTransactionId("transactionId" + i);
             list.add(log);
         }
         return list;
    }

    @Test
    public void testCodec() throws Exception{
        Buffer valueBuffer = new AutomaticBuffer();
        List<BusinessLogV1Bo> originListBusinessLogV1Bos = createBusinessLogV1BO();
        businessLogCodecV1.encodeValues(valueBuffer,originListBusinessLogV1Bos);
        BusinessLogDecodingContext decodingContext = new BusinessLogDecodingContext();
        decodingContext.setAgentId(AGENTID);
        Buffer decodeValueBuffer = new FixedBuffer(valueBuffer.getBuffer());
        List<String> destList = businessLogCodecV1.decodeValues(decodeValueBuffer,decodingContext);
        Assert.assertNotNull(decodeValueBuffer);
    }
}