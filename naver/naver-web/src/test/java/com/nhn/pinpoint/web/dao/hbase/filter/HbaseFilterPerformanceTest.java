package com.nhn.pinpoint.web.dao.hbase.filter;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.HbaseConfigurationFactoryBean;
import org.springframework.data.hadoop.hbase.HbaseSystemException;

import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseTemplate2;
import com.nhn.pinpoint.common.util.SpanUtils;
import com.nhn.pinpoint.web.mapper.TraceIndexScatterMapper;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.ResponseTimeRange;
import com.nhn.pinpoint.web.vo.SelectedScatterArea;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.scatter.Dot;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix.OneByteSimpleHash;

public class HbaseFilterPerformanceTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


	private static HbaseConfigurationFactoryBean hbaseConfigurationFactoryBean;
	private static AbstractRowKeyDistributor traceIdRowKeyDistributor;

	@BeforeClass
	public static void beforeClass() throws IOException {
//		Properties properties = PropertyUtils.loadPropertyFromClassPath("hbase.properties");

		Configuration cfg = HBaseConfiguration.create();
		cfg.set("hbase.zookeeper.quorum", "dev.zk.pinpoint.navercorp.com");
		cfg.set("hbase.zookeeper.property.clientPort", "2181");
		hbaseConfigurationFactoryBean = new HbaseConfigurationFactoryBean();
		hbaseConfigurationFactoryBean.setConfiguration(cfg);
		hbaseConfigurationFactoryBean.afterPropertiesSet();

		OneByteSimpleHash applicationTraceIndexHash = new com.sematext.hbase.wd.RowKeyDistributorByHashPrefix.OneByteSimpleHash(32);
		traceIdRowKeyDistributor = new com.sematext.hbase.wd.RowKeyDistributorByHashPrefix(applicationTraceIndexHash);
	}

	@AfterClass
	public static void afterClass() {
		if (hbaseConfigurationFactoryBean != null) {
			hbaseConfigurationFactoryBean.destroy();
		}
	}

	private Scan createScan(String applicationName, Range range) {
		Scan scan = new Scan();
		scan.setCaching(256);

		byte[] bAgent = Bytes.toBytes(applicationName);
		byte[] traceIndexStartKey = SpanUtils.getTraceIndexRowKey(bAgent, range.getFrom());
		byte[] traceIndexEndKey = SpanUtils.getTraceIndexRowKey(bAgent, range.getTo());

		scan.setStartRow(traceIndexEndKey);
		scan.setStopRow(traceIndexStartKey);

		scan.addFamily(HBaseTables.APPLICATION_TRACE_INDEX_CF_TRACE);
		scan.setId("ApplicationTraceIndexScan");

		return scan;
	}

	private Filter makePrefixFilter(SelectedScatterArea area, TransactionId offsetTransactionId, int offsetTransactionElapsed) {
		// filter by response time
		ResponseTimeRange responseTimeRange = area.getResponseTimeRange();
		byte[] responseFrom = Bytes.toBytes(responseTimeRange.getFrom());
		byte[] responseTo = Bytes.toBytes(responseTimeRange.getTo());
		FilterList filterList = new FilterList(Operator.MUST_PASS_ALL);
		filterList.addFilter(new QualifierFilter(CompareOp.GREATER_OR_EQUAL, new BinaryPrefixComparator(responseFrom)));
		filterList.addFilter(new QualifierFilter(CompareOp.LESS_OR_EQUAL, new BinaryPrefixComparator(responseTo)));

		// add offset
		if (offsetTransactionId != null) {
			final Buffer buffer = new AutomaticBuffer(32);
			buffer.put(offsetTransactionElapsed);
			buffer.putPrefixedString(offsetTransactionId.getAgentId());
			buffer.putSVar(offsetTransactionId.getAgentStartTime());
			buffer.putVar(offsetTransactionId.getTransactionSequence());
			byte[] qualifierOffset = buffer.getBuffer();

			filterList.addFilter(new QualifierFilter(CompareOp.GREATER, new BinaryPrefixComparator(qualifierOffset)));
		}

		return filterList;
	}

	@Test
	@Ignore
	public void usingFilter() throws Exception {

		HbaseTemplate2 hbaseTemplate2 = new HbaseTemplate2();
		hbaseTemplate2.setConfiguration(hbaseConfigurationFactoryBean.getObject());
		hbaseTemplate2.afterPropertiesSet();

		try {
			long oneday = 60 * 60 * 24 * 1000;
			int fetchLimit = 1000009;
			long timeTo = 1395989385734L;
			long timeFrom = timeTo - oneday;
			int responseTimeFrom = 0;
			int responseTimeTo = 10000;
			SelectedScatterArea area = new SelectedScatterArea(timeFrom, timeTo, responseTimeFrom, responseTimeTo);

			Scan scan = createScan("API.GATEWAY.DEV", area.getTimeRange());

			scan.setFilter(makePrefixFilter(area, null, -1));

			long startTime = System.currentTimeMillis();
			List<List<Dot>> dotListList = hbaseTemplate2.find(HBaseTables.APPLICATION_TRACE_INDEX, scan, traceIdRowKeyDistributor, fetchLimit, new TraceIndexScatterMapper());
			logger.debug("elapsed : {}ms", (System.currentTimeMillis() - startTime));
            logger.debug("fetched size : {}", dotListList.size());
		} catch (HbaseSystemException e) {
			e.printStackTrace();
		} finally {
			hbaseTemplate2.destroy();
		}

	}
}
