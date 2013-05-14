package com.nhn.pinpoint.web.dao.hbase;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.dao.TraceIndexDao;
import com.nhn.pinpoint.web.vo.TraceId;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.SpanUtils;

/**
 *
 */
@Repository
public class HbaseTraceIndexDao implements TraceIndexDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("traceIndexMapper")
	private RowMapper<List<TraceId>> traceIndexMapper;

	// cache size를 지정해야 되는거 같음.??
	private int scanCacheSize = 40;

	public void setScanCacheSize(int scanCacheSize) {
		this.scanCacheSize = scanCacheSize;
	}

	@Override
	public List<List<TraceId>> scanTraceIndex(String agent, long start, long end) {
		Scan scan = createScan(agent, start, end);
		return hbaseOperations2.find(HBaseTables.TRACE_INDEX, scan, traceIndexMapper);
	}

	@Override
	public List<List<List<TraceId>>> multiScanTraceIndex(String[] agents, long start, long end) {
		final List<Scan> multiScan = new ArrayList<Scan>(agents.length);
		for (String agent : agents) {
			Scan scan = createScan(agent, start, end);
			multiScan.add(scan);
		}
		return hbaseOperations2.find(HBaseTables.TRACE_INDEX, multiScan, traceIndexMapper);
	}

	private Scan createScan(String agent, long start, long end) {
		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);

		byte[] bAgent = Bytes.toBytes(agent);
		byte[] traceIndexStartKey = SpanUtils.getTraceIndexRowKey(bAgent, start);
		byte[] traceIndexEndKey = SpanUtils.getTraceIndexRowKey(bAgent, end);
		
		// timestamp가 reverse되었기 때문에 start, end를 바꿔서 조회.
		scan.setStartRow(traceIndexEndKey);
		scan.setStopRow(traceIndexStartKey);
		
		scan.addFamily(HBaseTables.TRACE_INDEX_CF_TRACE);
		scan.setId("traceIndexScan");

		// json으로 변화해서 로그를 찍어서. 최초 변환 속도가 느림.
		logger.debug("create scan:{}", scan);
		return scan;
	}
}
