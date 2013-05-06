package com.nhn.hippo.web.dao.hbase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.hippo.web.dao.TraceDao;
import com.nhn.hippo.web.vo.TraceId;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;

/**
 *
 */
@Repository
public class HbaseTraceDao implements TraceDao {

	@Autowired
	private HbaseOperations2 template2;

	@Autowired
	@Qualifier("spanMapper")
	private RowMapper<List<SpanBo>> spanMapper;

	@Autowired
	@Qualifier("spanAnnotationMapper")
	private RowMapper<List<SpanBo>> spanAnnotationMapper;

	@Override
	public List<SpanBo> selectSpan(TraceId traceId) {
		byte[] traceIdBytes = traceId.getBytes();
		return template2.get(HBaseTables.TRACES, traceIdBytes, HBaseTables.TRACES_CF_SPAN, spanMapper);
	}

	public List<SpanBo> selectSpanAndAnnotation(TraceId traceId) {
		byte[] traceIdBytes = traceId.getBytes();
		Get get = new Get(traceIdBytes);
		get.addFamily(HBaseTables.TRACES_CF_SPAN);
		get.addFamily(HBaseTables.TRACES_CF_ANNOTATION);
		get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
		return template2.get(HBaseTables.TRACES, get, spanAnnotationMapper);
	}


	@Override
	public List<List<SpanBo>> selectSpans(List<TraceId> traceIdList) {
		List<Get> gets = new ArrayList<Get>(traceIdList.size());
		for (TraceId traceId : traceIdList) {
			byte[] traceIdBytes = traceId.getBytes();
			Get get = new Get(traceIdBytes);
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanMapper);
	}

	@Override
	public List<List<SpanBo>> selectSpans(Set<TraceId> traceIdSet) {
		List<Get> gets = new ArrayList<Get>(traceIdSet.size());
		for (TraceId traceId : traceIdSet) {
			Get get = new Get(traceId.getBytes());
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanMapper);
	}
	
	@Override
	public List<List<SpanBo>> selectAllSpans(Set<TraceId> traceIdSet) {
		List<Get> gets = new ArrayList<Get>(traceIdSet.size());
		for (TraceId traceId : traceIdSet) {
			Get get = new Get(traceId.getBytes());
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanMapper);
	}

	@Override
	public List<SpanBo> selectSpans(TraceId traceId) {
		Get get = new Get(traceId.getBytes());
		get.addFamily(HBaseTables.TRACES_CF_SPAN);
		get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
		return template2.get(HBaseTables.TRACES, get, spanMapper);
	}

	@Override
	public List<List<SpanBo>> selectSpansAndAnnotation(Set<TraceId> traceIdList) {
		List<Get> gets = new ArrayList<Get>(traceIdList.size());
		for (TraceId traceId : traceIdList) {
			Get get = new Get(traceId.getBytes());
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			get.addFamily(HBaseTables.TRACES_CF_ANNOTATION);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanAnnotationMapper);
	}
}
