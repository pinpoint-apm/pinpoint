package com.nhn.hippo.web.service;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.BinaryAnnotation;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseTemplate2;
import com.profiler.common.util.SpanUtils;
import com.profiler.server.dao.Traces;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-web-applicationContext.xml")
public class SpanServiceTest {


    @Autowired
    private Traces traces;

    @Autowired
    private SpanService spanService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseTemplate2 template2;

    private Span root;
    private List<Span> deleteSpans = new LinkedList<Span>();

    @Before
    public void before() throws TException {
        Span span = createRootSpan();
        logger.debug("uuid:{}", new UUID(span.getMostTraceID(), span.getLeastTraceID()));
        insert(span);
        deleteSpans.add(span);

        Span subSpan1 = createSubSpan(span);
        insert(subSpan1);
        deleteSpans.add(subSpan1);

        Span subSpan1_2 = createSubSpan(span);
        insert(subSpan1_2);
        deleteSpans.add(subSpan1_2);

        Span subSpan2 = createSubSpan(subSpan1);
        insert(subSpan2);
        deleteSpans.add(subSpan2);

        Span subSpan3 = createSubSpan(subSpan1);
        insert(subSpan3);
        deleteSpans.add(subSpan3);

        root = span;
        logger.info(subSpan1.toString());
        logger.info(subSpan1_2.toString());
        logger.info(subSpan2.toString());
        logger.info(subSpan3.toString());

    }

    public void after() {
        List list = new LinkedList();
        for (Span span : deleteSpans) {
            Delete delete = new Delete(SpanUtils.getTracesRowkey(span));
            list.add(delete);
        }
        template2.delete(HBaseTables.TRACES, list);
        deleteSpans.clear();
    }

    @Test
    public void testReadSpan() throws TException {
        doRead(root);
    }


    private void doRead(Span span) {
        UUID uuid = new UUID(span.getMostTraceID(), span.getLeastTraceID());

        List<SpanAlign> sort = spanService.selectSpan(uuid.toString());
        for (SpanAlign spanAlign : sort) {
            logger.info("depth:{} {}", spanAlign.getDepth(), spanAlign.getSpan());
        }
//        reorder(spans);
    }


    private void insert(Span span) throws TException {
        TSerializer se = new TSerializer();
        byte[] serialize = se.serialize(span);
        traces.insert(span, serialize);
    }

    AtomicInteger id = new AtomicInteger(0);

    private Span createRootSpan() {
        // 별도 생성기로 뽑을것.
        UUID uuid = UUID.randomUUID();
        List<Annotation> ano = Collections.emptyList();
        List<BinaryAnnotation> bano = Collections.emptyList();
        long time = System.currentTimeMillis();
        int andIncrement = id.getAndIncrement();
        Span span = new Span("UnitTest", time, uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), "test", "rpc" + andIncrement, andIncrement, ano, bano, "protocol:ip:port", false);
        span.setParentSpanId(-1);
        return span;
    }

    private Span createSubSpan(Span span) {

        List<Annotation> ano = Collections.emptyList();
        List<BinaryAnnotation> bano = Collections.emptyList();

        long time = System.currentTimeMillis();
        int andIncrement = id.getAndIncrement();
        Span sub = new Span("UnitTest", time, span.getMostTraceID(), span.getLeastTraceID(), "test", "rpc" + andIncrement, andIncrement, ano, bano, "protocol:ip:port", false);
        sub.setParentSpanId(span.getSpanID());
        return sub;
    }

}
