package com.nhn.hippo.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


import org.apache.hadoop.hbase.client.Delete;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.vo.TraceId;
import com.profiler.common.AnnotationKey;
import com.profiler.common.ServiceType;
import com.profiler.common.dto2.thrift.Annotation;
import com.profiler.common.dto2.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseTemplate2;
import com.profiler.common.util.SpanUtils;
import com.profiler.server.dao.TracesDao;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-web-applicationContext.xml")
public class SpanServiceTest {

	@Autowired
	private TracesDao traceDao;

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
		logger.debug("uuid:{}", new UUID(span.getMostTraceId(), span.getLeastTraceId()));
		insert(span);
		deleteSpans.add(span);

		Span subSpan1 = createSpanEvent(span);
		insert(subSpan1);
		deleteSpans.add(subSpan1);

		Span subSpan1_2 = createSpanEvent(span);
		insert(subSpan1_2);
		deleteSpans.add(subSpan1_2);

		Span subSpan2 = createSpanEvent(subSpan1);
		insert(subSpan2);
		deleteSpans.add(subSpan2);

		Span subSpan3 = createSpanEvent(subSpan1);
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
			Delete delete = new Delete(SpanUtils.getTraceId(span));
			list.add(delete);
		}
		template2.delete(HBaseTables.TRACES, list);
		deleteSpans.clear();
	}

	@Test
	public void testReadSpan() throws TException {
		doRead(root);
	}

	@Test
	public void testReadSpanAndAnnotation() throws TException {
		doRead(root);
	}

	private void doRead(Span span) {
		TraceId traceId = new TraceId(span.getMostTraceId(), span.getLeastTraceId());

		List<SpanAlign> sort = spanService.selectSpan(traceId);
		for (SpanAlign spanAlign : sort) {
			logger.info("depth:{} {}", spanAlign.getDepth(), spanAlign.getSpanBo());
		}
		// reorder(spans);
	}

	private void insert(Span span) throws TException {
		traceDao.insert(span);
	}

	AtomicInteger id = new AtomicInteger(0);

	private Span createRootSpan() {
		// 별도 생성기로 뽑을것.
		UUID uuid = UUID.randomUUID();
		List<Annotation> ano = Collections.emptyList();
		long time = System.currentTimeMillis();
		int andIncrement = id.getAndIncrement();

		Span span = new Span();

		span.setAgentId("UnitTest");
		span.setApplicationName("ApplicationId");
		span.setMostTraceId(uuid.getMostSignificantBits());
		span.setLeastTraceId(uuid.getLeastSignificantBits());
		span.setStartTime(time);
		span.setElapsed(5);
		span.setRpc("RPC");

		span.setServiceType(ServiceType.UNKNOWN.getCode());
		span.setAnnotations(ano);

		span.setParentSpanId(-1);
		List<Annotation> annotations = new ArrayList<Annotation>();
		Annotation annotation = new Annotation(AnnotationKey.API.getCode());
		annotation.setStringValue("");
		annotations.add(annotation);
		span.setAnnotations(annotations);
		return span;
	}

	private Span createSpanEvent(Span span) {
		List<Annotation> ano = Collections.emptyList();
		long time = System.currentTimeMillis();
		int andIncrement = id.getAndIncrement();

		Span sub = new Span();

		sub.setAgentId("UnitTest");
		sub.setApplicationName("ApplicationId");
        sub.setAgentStartTime(123);

		sub.setMostTraceId(span.getMostTraceId());
		sub.setLeastTraceId(span.getLeastTraceId());
		sub.setStartTime(time);
		sub.setElapsed(5);
		sub.setRpc("RPC");
		sub.setServiceType(ServiceType.UNKNOWN.getCode());
		sub.setAnnotations(ano);

		sub.setParentSpanId(span.getSpanId());
		List<Annotation> annotations = new ArrayList<Annotation>();
		Annotation annotation = new Annotation(AnnotationKey.API.getCode());
		annotation.setStringValue("");
		annotations.add(annotation);
		sub.setAnnotations(annotations);
		return sub;
	}

}
