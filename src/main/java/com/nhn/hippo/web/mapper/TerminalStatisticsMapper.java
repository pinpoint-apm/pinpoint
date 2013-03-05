package com.nhn.hippo.web.mapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.hippo.web.vo.TerminalStatistics;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.TerminalSpanUtils;

/**
 *
 */
@Component
public class TerminalStatisticsMapper implements RowMapper<Map<String, TerminalStatistics>> {

	/**
	 * <pre>
	 * rowkey = applicationName + timeslot
	 * cf = Cnt, ErrCnt
	 * cn = ServiceType + Slot + ApplicationName
	 * 
	 * output format
	 * {
	 * 	hippo={
	 * 		From=TOMCAT11, To=hippo, ToSvcType=2101, Histogram={ "1000" : 3, "3000" : 0, "5000" : 0 }
	 * 	},
	 * 	dev={
	 * 		From=TOMCAT11, To=dev, ToSvcType=8100, Histogram={ "100" : 1, "300" : 0, "500" : 0 }
	 * 	},
	 * 	MEMCACHED={
	 * 		From=TOMCAT11, To=MEMCACHED, ToSvcType=8050, Histogram={ "100" : 1, "300" : 0, "500" : 0 }
	 * 	},
	 * 	section.cafe.naver.com={
	 * 		From=TOMCAT11, To=section.cafe.naver.com, ToSvcType=9050, Histogram={ "1000" : 2, "3000" : 0, "5000" : 0 }
	 * 	},
	 * 	www.naver.com={
	 * 		From=TOMCAT11, To=www.naver.com, ToSvcType=9050, Histogram={ "1000" : 2, "3000" : 0, "5000" : 0 }
	 * 	}
	 * }
	 * 
	 * </pre>
	 */
	@Override
	public Map<String, TerminalStatistics> mapRow(Result result, int rowNum) throws Exception {
		KeyValue[] keyList = result.raw();

		// key is destApplicationName.
		Map<String, TerminalStatistics> stat = new HashMap<String, TerminalStatistics>();

		// key is destApplicationName
		Map<String, Set<String>> destAppHostMap = new HashMap<String, Set<String>>();

		for (KeyValue kv : keyList) {
			if (kv.getFamilyLength() != HBaseTables.TERMINAL_STATISTICS_CF_COUNTER.length) {
				continue;
			}

			byte[] qualifier = kv.getQualifier();

			String srcApplicationName = TerminalSpanUtils.getApplicationNameFromRowKey(kv.getRow());
			String destApplicationName = TerminalSpanUtils.getDestApplicationNameFromColumnName(qualifier);
			long requestCount = Bytes.toLong(kv.getValue());
			short destServiceType = TerminalSpanUtils.getDestServiceTypeFromColumnName(qualifier);
			short histogramSlot = TerminalSpanUtils.getHistogramSlotFromColumnName(qualifier);
			String host = TerminalSpanUtils.getHost(qualifier);
			boolean isError = histogramSlot == (short) -1;

			// hostname은 일단 따로 보관.
			if (host != null) {
				if (destAppHostMap.containsKey(destApplicationName)) {
					destAppHostMap.get(destApplicationName).add(host);
				} else {
					Set<String> set = new HashSet<String>();
					set.add(host);
					destAppHostMap.put(destApplicationName, set);
				}
			}

			if (stat.containsKey(destApplicationName)) {
				TerminalStatistics statistics = stat.get(destApplicationName);
				if (isError) {
					statistics.getHistogram().incrErrorCount(requestCount);
				} else {
					statistics.getHistogram().addSample(histogramSlot, requestCount);
				}
			} else {
				TerminalStatistics statistics = new TerminalStatistics(srcApplicationName, destApplicationName, destServiceType);
				if (isError) {
					statistics.getHistogram().incrErrorCount(requestCount);
				} else {
					statistics.getHistogram().addSample(histogramSlot, requestCount);
				}
				stat.put(destApplicationName, statistics);
			}
		}

		// statistics에 dest host정보 삽입.
		for (Entry<String, TerminalStatistics> entry : stat.entrySet()) {
			entry.getValue().addHosts(destAppHostMap.get(entry.getKey()));
		}

		return stat;
	}
}
