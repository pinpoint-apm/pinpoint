package com.nhn.pinpoint.web.dao.hbase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.nhn.pinpoint.web.vo.TransactionId;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
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
 * @author emeroad
 */
@Repository
public class HbaseTraceDao implements TraceDao {

	@Autowired
	private HbaseOperations2 template2;

    @Autowired
    @Qualifier("traceIdRowKeyDistributor")
    private AbstractRowKeyDistributor rowKeyDistributor;

	@Autowired
	@Qualifier("spanMapper")
	private RowMapper<List<SpanBo>> spanMapper;

	@Autowired
	@Qualifier("spanAnnotationMapper")
	private RowMapper<List<SpanBo>> spanAnnotationMapper;

	@Override
	public List<SpanBo> selectSpan(TransactionId transactionId) {
		byte[] traceIdBytes = rowKeyDistributor.getDistributedKey(transactionId.getBytes());
		return template2.get(HBaseTables.TRACES, traceIdBytes, HBaseTables.TRACES_CF_SPAN, spanMapper);
	}

	public List<SpanBo> selectSpanAndAnnotation(TransactionId transactionId) {
		byte[] traceIdBytes = rowKeyDistributor.getDistributedKey(transactionId.getBytes());
		Get get = new Get(traceIdBytes);
		get.addFamily(HBaseTables.TRACES_CF_SPAN);
		get.addFamily(HBaseTables.TRACES_CF_ANNOTATION);
		get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
		return template2.get(HBaseTables.TRACES, get, spanAnnotationMapper);
	}


	@Override
	public List<List<SpanBo>> selectSpans(List<TransactionId> transactionIdList) {
		List<Get> gets = new ArrayList<Get>(transactionIdList.size());
		for (TransactionId traceId : transactionIdList) {
			byte[] traceIdBytes = rowKeyDistributor.getDistributedKey(traceId.getBytes());
			Get get = new Get(traceIdBytes);
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanMapper);
	}

	@Override
	public List<List<SpanBo>> selectSpans(Set<TransactionId> transactionIdList) {
		List<Get> gets = new ArrayList<Get>(transactionIdList.size());
		for (TransactionId transactionId : transactionIdList) {
            byte[] transactionIdBytes = this.rowKeyDistributor.getDistributedKey(transactionId.getBytes());
			Get get = new Get(transactionIdBytes);
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanMapper);
	}
	
	@Override
	public List<List<SpanBo>> selectAllSpans(Collection<TransactionId> transactionIdList) {
		List<Get> gets = new ArrayList<Get>(transactionIdList.size());
		for (TransactionId transactionId : transactionIdList) {
            byte[] transactionIdBytes = this.rowKeyDistributor.getDistributedKey(transactionId.getBytes());
            Get get = new Get(transactionIdBytes);
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanMapper);
	}

	@Override
	public List<SpanBo> selectSpans(TransactionId transactionId) {
        byte[] transactionIdBytes = this.rowKeyDistributor.getDistributedKey(transactionId.getBytes());
        Get get = new Get(transactionIdBytes);
		get.addFamily(HBaseTables.TRACES_CF_SPAN);
		get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
		return template2.get(HBaseTables.TRACES, get, spanMapper);
	}

	@Override
	public List<List<SpanBo>> selectSpansAndAnnotation(Set<TransactionId> transactionIdList) {
		List<Get> gets = new ArrayList<Get>(transactionIdList.size());
		for (TransactionId transactionId : transactionIdList) {
            byte[] transactionIdBytes = this.rowKeyDistributor.getDistributedKey(transactionId.getBytes());
			Get get = new Get(transactionIdBytes);
			get.addFamily(HBaseTables.TRACES_CF_SPAN);
			get.addFamily(HBaseTables.TRACES_CF_ANNOTATION);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanAnnotationMapper);
	}
}
