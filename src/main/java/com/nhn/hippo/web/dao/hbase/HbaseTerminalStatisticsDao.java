package com.nhn.hippo.web.dao.hbase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.hippo.web.dao.TerminalStatisticsDao;
import com.nhn.hippo.web.vo.TerminalStatistics;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.TerminalSpanUtils;
import com.profiler.common.util.TimeSlot;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
@Repository
public class HbaseTerminalStatisticsDao implements TerminalStatisticsDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int scanCacheSize = 40;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("terminalStatisticsMapper")
	private RowMapper<Map<String, TerminalStatistics>> terminalStatisticsMapper;

	@Override
	public List<Map<String, TerminalStatistics>> selectTerminal(String applicationName, long from, long to) {
		Scan scan = createScan(applicationName, from, to);
        return hbaseOperations2.find(HBaseTables.TERMINAL_STATISTICS, scan, terminalStatisticsMapper);
	}

	private Scan createScan(String applicationName, long from, long to) {
        long startTime = TimeSlot.getStatisticsRowSlot(from);
        // hbase의 scanner를 사용하여 검색시 endTime은 검색 대상에 포함되지 않기 때문에, +1을 해줘야 된다.
        long endTime = TimeSlot.getStatisticsRowSlot(to) + 1;
        if (logger.isDebugEnabled()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
            logger.debug("scan startTime:{} endTime:{}", simpleDateFormat.format(new Date(startTime)), simpleDateFormat.format(new Date(endTime)));
        }
        byte[] startKey = TerminalSpanUtils.makeRowKey(applicationName, startTime);
		byte[] endKey = TerminalSpanUtils.makeRowKey(applicationName, endTime);

		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);
		scan.setStartRow(startKey);
		scan.setStopRow(endKey);
		scan.addFamily(HBaseTables.TERMINAL_STATISTICS_CF_COUNTER);
		scan.setId("terminalStatisticsScan");

		return scan;
	}
}
