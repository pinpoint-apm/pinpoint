package com.nhn.pinpoint.web.dao.hbase;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.util.TimeSlot;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.service.map.AcceptApplication;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.RangeFactory;
import org.apache.commons.collections.CollectionUtils;
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
	private int scanCacheSize = 10;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("hostApplicationMapper")
	private RowMapper<Application> hostApplicationMapper;

    @Autowired
    @Qualifier("hostApplicationMapperVer2")
    private RowMapper<List<AcceptApplication>> hostApplicationMapperVer2;

    @Autowired
    private RangeFactory rangeFactory;


    @Override
    @Deprecated
    public Set<AcceptApplication> findAcceptApplicationName(String host, Range range) {
        if (host == null) {
            throw new NullPointerException("host must not be null");
        }
        final Scan scan = createScan(host, range);
        final List<Application> result = hbaseOperations2.find(HBaseTables.HOST_APPLICATION_MAP, scan, hostApplicationMapper);
        if (CollectionUtils.isNotEmpty(result)) {
            Set<AcceptApplication> resultSet = new HashSet<AcceptApplication>();
            for (Application application : result) {
                resultSet.add(new AcceptApplication(host, application));
            }
            return resultSet;
        } else {
            return Collections.emptySet();
        }
    }

    private Scan createScan(String host, Range range) {
        if (host == null) {
            throw new NullPointerException("host must not be null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("scan range:{}", range);
        }
        long startTime = TimeUtils.reverseTimeMillis(TimeSlot.getStatisticsRowSlot(range.getFrom()));
        long endTime = TimeUtils.reverseTimeMillis(TimeSlot.getStatisticsRowSlot(range.getTo()) + 1);

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

	@Override
	public Set<AcceptApplication> findAcceptApplicationName(Application fromApplication, Range range) {
        if (fromApplication == null) {
            throw new NullPointerException("fromApplication must not be null");
        }
        final Scan scan = createScan(fromApplication, range);
		final List<List<AcceptApplication>> result = hbaseOperations2.find(HBaseTables.HOST_APPLICATION_MAP_VER2, scan, hostApplicationMapperVer2);
		if (CollectionUtils.isNotEmpty(result)) {
            final Set<AcceptApplication> resultSet = new HashSet<AcceptApplication>();
            for (List<AcceptApplication> resultList : result) {
                resultSet.addAll(resultList);
            }
            logger.debug("findAcceptApplicationName result:{}", resultSet);
            return resultSet;
		} else {
			return Collections.emptySet();
		}
	}




    private Scan createScan(Application parentApplication, Range range) {
        if (parentApplication == null) {
            throw new NullPointerException("parentApplication must not be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("scan parentApplication:{}, range:{}", parentApplication, range);
        }
        // scanner crate로직의 공통화가 필요함.
        final long startTime = TimeUtils.reverseTimeMillis(TimeSlot.getStatisticsRowSlot(range.getFrom()));
        final long endTime = TimeUtils.reverseTimeMillis(TimeSlot.getStatisticsRowSlot(range.getTo()) + 1);
        // timestamp가 reverse되었기 때문에 start, end를 바꿔서 조회.
        final byte[] startKey = createKey(parentApplication, endTime);
        final byte[] endKey = createKey(parentApplication, startTime);

        Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);
        scan.setStartRow(startKey);
        scan.setStopRow(endKey);
        scan.setId("HostApplicationScan_Ver2");

        return scan;
    }

    private byte[] createKey(Application parentApplication, long time) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putPadString(parentApplication.getName(), HBaseTables.APPLICATION_NAME_MAX_LEN);
        buffer.put(parentApplication.getServiceTypeCode());
        buffer.put(time);
        return buffer.getBuffer();
    }



}
