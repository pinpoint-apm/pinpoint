package com.nhn.pinpoint.collector.dao.hbase;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.thrift.dto.TSpan;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.common.hbase.HBaseAdminTemplate;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.RowKeyUtils;
import com.nhn.pinpoint.common.util.SpanUtils;

/**
 * @author emeroad
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ContextConfiguration("classpath:applicationContext-test.xml")
@Ignore
public class TraceDaoTest {

    private static final String TRACE = "Trace";
    private static final String ID = "ID";

    @Autowired
    private HbaseOperations2 hbaseOperations;

    @Autowired
    private HBaseAdminTemplate hBaseAdminTemplate;


    @Autowired
    @Qualifier("testTraceIndex")
    private HbaseTraceIndexDao traceIndex;

    @Autowired
    private AcceptedTimeService acceptedTimeService;


    //	@BeforeClass
    @Before
    public void init() throws IOException {
        if (hBaseAdminTemplate == null) {
            throw new RuntimeException("hBaseAdminTemplate is required");
        }
        String tableName = traceIndex.getTableName();

        HTableDescriptor testTrace = new HTableDescriptor(traceIndex.getTableName());
        testTrace.addFamily(new HColumnDescriptor(TRACE));
        hBaseAdminTemplate.createTableIfNotExist(testTrace);
    }

    //	@AfterClass
    @After
    public void destroy() throws IOException {
        String tableName = traceIndex.getTableName();
        hBaseAdminTemplate.dropTableIfExist(tableName);

    }

    RowMapper<byte[]> valueRowMapper = new RowMapper<byte[]>() {
        @Override
        public byte[] mapRow(Result result, int rowNum) throws Exception {
            return result.getRow();
        }
    };

    @Test
    public void insertSpan() throws InterruptedException, UnsupportedEncodingException {

        final TSpan span = createSpan();

        acceptedTimeService.accept();
        traceIndex.insert(span);

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        // 키를 꺼구로 돌려야 한다.
        byte[] rowKey = RowKeyUtils.concatFixedByteAndLong(Bytes.toBytes(span.getAgentId()), HBaseTables.AGENT_NAME_MAX_LEN, TimeUtils.reverseCurrentTimeMillis(acceptedTime));
        byte[] resultRowKey = hbaseOperations.get(traceIndex.getTableName(), rowKey, Bytes.toBytes("Trace"), SpanUtils.getTransactionId(span), valueRowMapper);

        // 결과값 비교가 애매함
        Assert.assertArrayEquals(rowKey, resultRowKey);
    }

    private TSpan createSpan() {
        List<TAnnotation> ano = Collections.emptyList();
        long l = System.currentTimeMillis();

        TSpan span = new TSpan();
        span.setAgentId("UnitTest");
        span.setApplicationName("testApplication");
        span.setAgentStartTime(123);


        span.setStartTime(l);
        span.setElapsed(5);
        span.setRpc("RPC");
        span.setServiceType(ServiceType.UNKNOWN.getCode());
        span.setAnnotations(ano);
        
        return span;
    }
}
