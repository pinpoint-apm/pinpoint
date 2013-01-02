package com.nhn.hippo.web.dao.hbase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import com.profiler.common.util.BytesUtils;

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
	public List<SpanBo> selectSpan(UUID traceId) {
		byte[] uuidBytes = BytesUtils.longLongToBytes(traceId.getMostSignificantBits(), traceId.getLeastSignificantBits());
		return template2.get(HBaseTables.TRACES, uuidBytes, HBaseTables.TRACES_CF_SPAN, spanMapper);
	}

	public List<SpanBo> selectSpanAndAnnotation(UUID traceId) {
		byte[] uuidBytes = BytesUtils.longLongToBytes(traceId.getMostSignificantBits(), traceId.getLeastSignificantBits());
		Get get = new Get(uuidBytes);
		get.addFamily(HBaseTables.TRACES_CF_SPAN);
		get.addFamily(HBaseTables.TRACES_CF_ANNOTATION);
		get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
		return template2.get(HBaseTables.TRACES, get, spanAnnotationMapper);
	}

	@Override
	public List<SpanBo> selectSpan(long traceIdMost, long traceIdLeast) {
		byte[] uuidBytes = BytesUtils.longLongToBytes(traceIdMost, traceIdLeast);
		return template2.get(HBaseTables.TRACES, uuidBytes, HBaseTables.TRACES_CF_SPAN, spanMapper);
	}

	@Override
	public List<List<SpanBo>> selectSpans(List<UUID> traceIds) {
		List<Get> gets = new ArrayList<Get>(traceIds.size());
		for (UUID traceId : traceIds) {
			byte[] uuidBytes = BytesUtils.longLongToBytes(traceId.getMostSignificantBits(), traceId.getLeastSignificantBits());
			Get get = new Get(uuidBytes);
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanMapper);
	}

	@Override
	public List<List<SpanBo>> selectSpans(Set<TraceId> traceIds) {
		List<Get> gets = new ArrayList<Get>(traceIds.size());
		for (TraceId traceId : traceIds) {
			Get get = new Get(traceId.getBytes());
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
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
	public List<List<SpanBo>> selectSpansAndAnnotation(Set<TraceId> traceIds) {
		List<Get> gets = new ArrayList<Get>(traceIds.size());
		for (TraceId traceId : traceIds) {
			Get get = new Get(traceId.getBytes());
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			get.addFamily(HBaseTables.TRACES_CF_ANNOTATION);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanAnnotationMapper);
	}
}
