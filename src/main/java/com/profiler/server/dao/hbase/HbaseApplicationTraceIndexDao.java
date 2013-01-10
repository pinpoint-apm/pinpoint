package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.APPLICATION_TRACE_INDEX;
import static com.profiler.common.hbase.HBaseTables.APPLICATION_TRACE_INDEX_CF_TRACE;

import java.util.List;

import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.AnnotationNames;
import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.BytesUtils;
import com.profiler.common.util.SpanUtils;
import com.profiler.server.dao.ApplicationTraceIndexDao;

/**
 * find traceids by application name
 * 
 * @author netspider
 */
public class HbaseApplicationTraceIndexDao implements ApplicationTraceIndexDao {

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Override
	public void insert(String applicationName, final Span span) {
		int elapsedTime = span.getElapsed();
		int resultCode = 0;

		List<Annotation> anns = span.getAnnotations();
		for (Annotation ann : anns) {
			if (AnnotationNames.EXCEPTION.equals(ann.getKey())) {
				resultCode = 1;
				break;
			}
		}

		byte[] value = new byte[8];
		BytesUtils.writeInt(elapsedTime, value, 0);
		BytesUtils.writeInt(resultCode, value, 4);

		// TODO 서버가 받은 시간으로 변경해야 될듯.
		Put put = new Put(SpanUtils.getApplicationTraceIndexRowKey(applicationName, span), span.getStartTime());
		put.add(APPLICATION_TRACE_INDEX_CF_TRACE, SpanUtils.getTraceId(span), value);

		hbaseTemplate.put(APPLICATION_TRACE_INDEX, put);
	}
}
