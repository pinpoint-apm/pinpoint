package com.nhn.pinpoint.web.dao.hbase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.nhn.pinpoint.common.util.TimeSlot;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.RangeFactory;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.dao.HostApplicationMapDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;

/**
 * 
 * @author netspider
 * @author emeroad
 * 
 */
@Repository
public class HbaseHostApplicationMapDao implements HostApplicationMapDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int scanCacheSize = 40;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("hostApplicationMapper")
	private RowMapper<Application> hostApplicationMapper;

    @Autowired
    private RangeFactory rangeFactory;

	@Override
	public Application findApplicationName(String host, Range range) {
        if (host == null) {
            throw new NullPointerException("host must not be null");
        }
		Scan scan = createScan(host, range);
		List<Application> result = hbaseOperations2.find(HBaseTables.HOST_APPLICATION_MAP, scan, hostApplicationMapper);
		if (result != null && result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}

	private Scan createScan(String host, Range range) {
        long startTime = TimeUtils.reverseTimeMillis(TimeSlot.getStatisticsRowSlot(range.getFrom()));
		long endTime = TimeUtils.reverseTimeMillis(TimeSlot.getStatisticsRowSlot(range.getTo()) + 1);

		if (logger.isDebugEnabled()) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
			logger.debug("scan startTime:{} endTime:{}", simpleDateFormat.format(new Date(startTime)), simpleDateFormat.format(new Date(endTime)));
		}

		// timestamp가 reverse되었기 때문에 start, end를 바꿔서 조회.
		byte[] startKey = Bytes.toBytes(endTime);
		byte[] endKey = Bytes.toBytes(startTime);

		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);
		scan.setStartRow(startKey);
		scan.setStopRow(endKey);
		scan.addColumn(HBaseTables.HOST_APPLICATION_MAP_CF_MAP, Bytes.toBytes(host));
		scan.setId("HostApplicationScan");

		return scan;
	}

}
