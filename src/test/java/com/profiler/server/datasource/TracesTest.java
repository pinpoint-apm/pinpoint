package com.profiler.server.datasource;

import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.BinaryAnnotation;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseClient;
import com.profiler.common.util.SpanUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.HbaseOperations;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.data.hadoop.hbase.TableCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
//@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ContextConfiguration("classpath:test-applicationContext.xml")
public class TracesTest {

    private static final String TRACE = "Trace";
    private static final String ID = "ID";

    @Autowired
    private HbaseOperations hbaseOperations;

    // static 하니 inject가 잘안됨 방안을 찾아봐야 될듯.
    @Autowired
    public HBaseClient hbaseClient;


    @Autowired
    @Qualifier("testTraceIndex")
    private TraceIndex traceIndex;


    //	@BeforeClass
    @Before
    public void init() {
        if (hbaseClient == null) {
            System.out.println("hbaseClient is null-------");
            return;
        }
        if (!hbaseClient.isTableExists(traceIndex.getTableName())) {
            HTableDescriptor testTrace = new HTableDescriptor(traceIndex.getTableName());

            testTrace.addFamily(new HColumnDescriptor(TRACE));

            hbaseClient.createTable(testTrace);
        }

    }

    //	@AfterClass
    @After
    public void destroy() {
        if (hbaseClient.isTableExists(traceIndex.getTableName())) {
            hbaseClient.dropTable(traceIndex.getTableName());
        }
        hbaseClient.close();
    }

    RowMapper<byte[]> valueMapper = new RowMapper<byte[]>() {
        @Override
        public byte[] mapRow(Result result, int rowNum) throws Exception {
            return result.value();
        }
    };

    @Test
    public void insertSpan() throws InterruptedException, UnsupportedEncodingException {
        final Span span = createSpan();

        traceIndex.insert(span);

        // hbaseOperations 이거는 이거대로 string 기반이라 api가 불편한거 같음. 추가로 byte[]로 만들어야 될듯.
        Result result = hbaseOperations.execute(traceIndex.getTableName(), new TableCallback<Result>() {
            @Override
            public Result doInTable(HTable table) throws Throwable {
                byte[] s = ArrayUtils.addAll(Bytes.toBytes(span.getAgentID()), Bytes.toBytes(span.getTimestamp()));
                Get get = new Get(s);
                get.addColumn(Bytes.toBytes("Trace"), Bytes.toBytes("ID"));
                return table.get(get);
            }
        });

        Assert.assertArrayEquals(SpanUtils.getTraceId(span), result.value());
    }

    private Span createSpan() {
        UUID uuid = UUID.randomUUID();
        List<Annotation> ano = Collections.emptyList();
        List<BinaryAnnotation> bano = Collections.emptyList();
        Span span = new Span("UnitTest", System.currentTimeMillis(), uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), "test", 1, ano, bano);
        return span;
    }
}
