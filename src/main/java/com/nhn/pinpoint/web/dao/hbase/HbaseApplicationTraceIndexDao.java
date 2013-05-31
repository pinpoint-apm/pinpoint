package com.nhn.pinpoint.web.dao.hbase;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.nhn.pinpoint.web.vo.TraceId;
import com.nhn.pinpoint.web.vo.TraceIdWithTime;
import com.nhn.pinpoint.web.vo.scatter.Dot;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.SpanUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.common.util.TraceIdUtils;

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
	private RowMapper<List<TraceId>> traceIndexMapper;

	@Autowired
	@Qualifier("traceIndexScatterMapper")
	private RowMapper<List<Dot>> traceIndexScatterMapper;

	private int scanCacheSize = 200;

	public void setScanCacheSize(int scanCacheSize) {
		this.scanCacheSize = scanCacheSize;
	}

	@Override
	public List<List<TraceId>> scanTraceIndex(String applicationName, long start, long end) {
		Scan scan = createScan(applicationName, start, end);
		return hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, scan, traceIndexMapper);
	}

	@Override
	public List<List<List<TraceId>>> multiScanTraceIndex(String[] applicationNames, long start, long end) {
		final List<Scan> multiScan = new ArrayList<Scan>(applicationNames.length);
		for (String agent : applicationNames) {
			Scan scan = createScan(agent, start, end);
			multiScan.add(scan);
		}
		return hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, multiScan, traceIndexMapper);
	}

	private Scan createScan(String agent, long start, long end) {
		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);

		byte[] bAgent = Bytes.toBytes(agent);
		byte[] traceIndexStartKey = SpanUtils.getTraceIndexRowKey(bAgent, start);
		byte[] traceIndexEndKey = SpanUtils.getTraceIndexRowKey(bAgent, end);

		// key가 reverse되었기 떄문에 start, end가 뒤바뀌게 된다.
		scan.setStartRow(traceIndexEndKey);
		scan.setStopRow(traceIndexStartKey);

		scan.addFamily(HBaseTables.APPLICATION_TRACE_INDEX_CF_TRACE);
		scan.setId("ApplicationTraceIndexScan");

		// json으로 변화해서 로그를 찍어서. 최초 변환 속도가 느림.
		logger.debug("create scan:{}", scan);
		return scan;
	}

	@Override
	public List<List<Dot>> scanTraceScatter(String applicationName, long start, long end) {
		Scan scan = createScan(applicationName, start, end);
		return hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, scan, traceIndexScatterMapper);
	}

	@Override
	public List<Dot> scanTraceScatter2(String applicationName, long start, long end, final int limit) {
		Scan scan = createScan(applicationName, start, end);

		List<Dot> list = hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, scan, new ResultsExtractor<List<Dot>>() {
			@Override
			public List<Dot> extractData(ResultScanner results) throws Exception {
				List<Dot> list = new ArrayList<Dot>();
				for (Result result : results) {
					if (result == null) {
						continue;
					}

					KeyValue[] raw = result.raw();
					for (KeyValue kv : raw) {
						byte[] v = kv.getValue();

						int elapsed = BytesUtils.bytesToInt(v, 0);
						int exceptionCode = BytesUtils.bytesToInt(v, 4);

						long acceptedTime = TimeUtils.recoveryCurrentTimeMillis(BytesUtils.bytesToLong(kv.getRow(), 24));

						long[] tid = BytesUtils.bytesToLongLong(kv.getQualifier());
						String traceId = TraceIdUtils.formatString(tid[0], tid[1]);

						Dot dot = new Dot(traceId, acceptedTime, elapsed, exceptionCode);
						list.add(dot);
					}

					if (list.size() >= limit) {
						break;
					}
				}
				return list;
			}
		});
		return list;
	}

	@Override
	public List<TraceId> scanTraceScatterTraceIdList(String applicationName, long start, long end, final int limit) {
		Scan scan = createScan(applicationName, start, end);

		List<TraceId> list = hbaseOperations2.find(HBaseTables.APPLICATION_TRACE_INDEX, scan, new ResultsExtractor<List<TraceId>>() {
			@Override
			public List<TraceId> extractData(ResultScanner results) throws Exception {
				List<TraceId> list = new ArrayList<TraceId>();
				for (Result result : results) {
					if (result == null) {
						continue;
					}

					KeyValue[] raw = result.raw();
					for (KeyValue kv : raw) {
						long[] tid = BytesUtils.bytesToLongLong(kv.getQualifier());
						long acceptedTime = TimeUtils.recoveryCurrentTimeMillis(BytesUtils.bytesToLong(kv.getRow(), 24));
						list.add(new TraceIdWithTime(tid[0], tid[1], acceptedTime));
					}

					if (list.size() >= limit) {
						break;
					}
				}
				return list;
			}
		});
		return list;
	}
}
