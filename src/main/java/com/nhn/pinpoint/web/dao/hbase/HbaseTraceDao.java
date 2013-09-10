package com.nhn.pinpoint.web.dao.hbase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.nhn.pinpoint.web.vo.TransactionId;
import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.dao.TraceDao;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;

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
	public List<SpanBo> selectSpan(TransactionId traceId) {
		byte[] traceIdBytes = traceId.getBytes();
		return template2.get(HBaseTables.TRACES, traceIdBytes, HBaseTables.TRACES_CF_SPAN, spanMapper);
	}

	public List<SpanBo> selectSpanAndAnnotation(TransactionId traceId) {
		byte[] traceIdBytes = traceId.getBytes();
		Get get = new Get(traceIdBytes);
		get.addFamily(HBaseTables.TRACES_CF_SPAN);
		get.addFamily(HBaseTables.TRACES_CF_ANNOTATION);
		get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
		return template2.get(HBaseTables.TRACES, get, spanAnnotationMapper);
	}


	@Override
	public List<List<SpanBo>> selectSpans(List<TransactionId> traceIdList) {
		List<Get> gets = new ArrayList<Get>(traceIdList.size());
		for (TransactionId traceId : traceIdList) {
			byte[] traceIdBytes = traceId.getBytes();
			Get get = new Get(traceIdBytes);
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanMapper);
	}

	@Override
	public List<List<SpanBo>> selectSpans(Set<TransactionId> traceIdSet) {
		List<Get> gets = new ArrayList<Get>(traceIdSet.size());
		for (TransactionId traceId : traceIdSet) {
			Get get = new Get(traceId.getBytes());
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanMapper);
	}
	
	@Override
	public List<List<SpanBo>> selectAllSpans(Collection<TransactionId> traceIdSet) {
		List<Get> gets = new ArrayList<Get>(traceIdSet.size());
		for (TransactionId traceId : traceIdSet) {
			Get get = new Get(traceId.getBytes());
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanMapper);
	}

	@Override
	public List<SpanBo> selectSpans(TransactionId traceId) {
		Get get = new Get(traceId.getBytes());
		get.addFamily(HBaseTables.TRACES_CF_SPAN);
		get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
		return template2.get(HBaseTables.TRACES, get, spanMapper);
	}

	@Override
	public List<List<SpanBo>> selectSpansAndAnnotation(Set<TransactionId> traceIdList) {
		List<Get> gets = new ArrayList<Get>(traceIdList.size());
		for (TransactionId traceId : traceIdList) {
			Get get = new Get(traceId.getBytes());
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			get.addFamily(HBaseTables.TRACES_CF_ANNOTATION);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanAnnotationMapper);
	}
}
