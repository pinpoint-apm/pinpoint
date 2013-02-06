package com.nhn.hippo.web.dao.hbase;

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

import com.nhn.hippo.web.dao.ApplicationTraceIndexDao;
import com.nhn.hippo.web.vo.scatter.Dot;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.SpanUtils;

/**
 *
 */
@Repository
public class HbaseApplicationTraceIndexDao implements ApplicationTraceIndexDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("traceIndexMapper")
	private RowMapper<List<byte[]>> traceIndexMapper;

	@Autowired
	@Qualifier("traceIndexScatterMapper")
	private RowMapper<List<Dot>> traceIndexScatterMapper;

	private int scanCacheSize = 200;

	public void setScanCacheSize(int scanCacheSize) {
		this.scanCacheSize = scanCacheSize;
	}

	@Override
	public List<List<byte[]>> scanTraceIndex(String applicationName, long start, long end) {
		Scan scan = createScan(applicationName, start, end);
		return hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, scan, traceIndexMapper);
	}

	@Override
	public List<List<List<byte[]>>> multiScanTraceIndex(String[] applicationNames, long start, long end) {
		final List<Scan> multiScan = new ArrayList<Scan>(applicationNames.length);
		for (String agent : applicationNames) {
			Scan scan = createScan(agent, start, end);
			multiScan.add(scan);
		}
		return hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, multiScan, traceIndexMapper);
	}

	private Scan createScan(String agent, long start, long end) {
		Scan scan = new Scan();
		// cache size를 지정해야 되는거 같음.??
		scan.setCaching(this.scanCacheSize);

		byte[] bAgent = Bytes.toBytes(agent);
		byte[] traceIndexStartKey = SpanUtils.getTraceIndexRowKey(bAgent, start);
		scan.setStartRow(traceIndexStartKey);
		// TODO 추가 filter를 구현하여 scan시 중복된 값을 제가 할수 있음. 단 server에도 Filter 클래스가
		// 배포되어야 한다.
		// scan.setFilter(new ValueFilter());

		byte[] traceIndexEndKey = SpanUtils.getTraceIndexRowKey(bAgent, end);
		scan.setStopRow(traceIndexEndKey);
		scan.addFamily(HBaseTables.APPLICATION_TRACE_INDEX_CF_TRACE);
		scan.setId("traceIndexScan");

		// json으로 변화해서 로그를 찍어서. 최초 변환 속도가 느림.
		logger.debug("create scan:{}", scan);
		return scan;
	}

	@Override
	public List<List<Dot>> scanTraceScatter(String applicationName, long start, long end) {
		Scan scan = createScan(applicationName, start, end);
		return hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, scan, traceIndexScatterMapper);
	}
}
