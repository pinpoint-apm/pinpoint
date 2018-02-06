package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogType;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogV1Bo;
import org.apache.hadoop.hbase.client.Put;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.AGENT_STAT_TIMESPAN_MS;
import static org.junit.Assert.*;

/**
 * Created by suny on 2018/2/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class BusinessLogHbaseOperationFactoryTest {
    protected static final String TEST_AGENT_ID = "testAgentId";
    private static final  BusinessLogType TEST_AGENT_STAT_TYPE = BusinessLogType.BUSINESS_LOG_V1;
    @Autowired
    BusinessLogHbaseOperationFactory businessLogHbaseOperationFactory;
    @Mock
    private BusinessLogV1Serializer businessLogV1Serializer;
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreatePutsNull() {
        List<BusinessLogV1Bo> businessLogV1Bos = null;
        List<Put> puts = this.businessLogHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, businessLogV1Bos, this.businessLogV1Serializer);
        assertEquals(Collections.<Put>emptyList(), puts);
    }

    @Test
    public void create_should_return_empty_list_for_empty_dataPoints() {
        List<BusinessLogV1Bo> businessLogV1Bos = Collections.emptyList();
        List<Put> puts = this.businessLogHbaseOperationFactory.createPuts(TEST_AGENT_ID, TEST_AGENT_STAT_TYPE, businessLogV1Bos, this.businessLogV1Serializer);
        assertEquals(Collections.<Put>emptyList(), puts);
    }

    @Test
    public void testCreatePuts() throws Exception {
        // Given
        final int numDataPoints = 9;
        final List<BusinessLogV1Bo> businessLogV1Bo  = createBusinessLogV1Bo( numDataPoints);
        // When
        List<Put> puts = businessLogHbaseOperationFactory.createPuts(TEST_AGENT_ID, BusinessLogType.BUSINESS_LOG_V1, businessLogV1Bo, this.businessLogV1Serializer);
        // Then
        assertEquals(numDataPoints, puts.size());
        for (int i = 0; i < puts.size(); i++) {
            Put put = puts.get(i);
            assertEquals(TEST_AGENT_ID, this.businessLogHbaseOperationFactory.getAgentId(put.getRow()));
            assertEquals(TEST_AGENT_STAT_TYPE, this.businessLogHbaseOperationFactory.getAgentStatType(put.getRow()));
            assertEquals(new String("transactionId" + i + "#" + "spanId" + i), this.businessLogHbaseOperationFactory.getTransactionIdANDSpanId(put.getRow()).trim());
        }
    }

    private List<BusinessLogV1Bo> createBusinessLogV1Bo(int count){
        List<BusinessLogV1Bo> listBusinessLogV1Bo = new ArrayList<BusinessLogV1Bo>();
        for(int i = 0; i < count; i++){
            BusinessLogV1Bo log = new BusinessLogV1Bo();
            log.setAgentId(TEST_AGENT_ID);
            log.setClassName("className" + i);
            log.setLogLevel("logLevel" + i);
            log.setMessage("message" + i);
            log.setSpanId("spanId" + i);
            log.setStartTimestamp(System.currentTimeMillis());
            log.setThreadName("threadName" + i);
            log.setTime("time" + i);
            log.setTimestamp(System.currentTimeMillis());
            log.setTransactionId("transactionId" + i);
            listBusinessLogV1Bo.add(log);
        }
        return listBusinessLogV1Bo;
    }

}