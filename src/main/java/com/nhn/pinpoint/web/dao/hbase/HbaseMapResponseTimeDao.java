package com.nhn.pinpoint.web.dao.hbase;

import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;
import com.nhn.pinpoint.web.dao.MapResponseDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.RawResponseTime;
import org.apache.hadoop.hbase.client.Scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapResponseTimeDao implements MapResponseDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String tableName = HBaseTables.APPLICATION_MAP_STATISTICS_SELF;

    private int scanCacheSize = 40;

    @Autowired
    private RowMapper<RawResponseTime> responseTimeMapper;

    @Autowired
    private HbaseOperations2 hbaseOperations2;


    @Override
    public List<RawResponseTime> selectResponseTime(Application application, long from, long to) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("selectResponseTime applicationName:{}, from:{}, to:{}", application, from, to);
        }
        Scan scan = createScan(application, from, to);
        List<RawResponseTime> rawResponseTimeList = hbaseOperations2.find(tableName, scan, responseTimeMapper);
        if (logger.isDebugEnabled()) {
            logger.debug("row:{}", rawResponseTimeList.size());
            for (RawResponseTime rawResponseTime : rawResponseTimeList) {
                logger.debug("rawResponseTime:{}", rawResponseTime);
            }
        }

        return rawResponseTimeList;
    }

    private Scan createScan(Application application, long from, long to) {
        long startTime = TimeSlot.getStatisticsRowSlot(from);
        // hbase의 scanner를 사용하여 검색시 endTime은 검색 대상에 포함되지 않기 때문에, +1을 해줘야 된다.
        long endTime = TimeSlot.getStatisticsRowSlot(to) + 1;

        if (logger.isDebugEnabled()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
            logger.debug("scan startTime:{} endTime:{}", simpleDateFormat.format(new Date(startTime)), simpleDateFormat.format(new Date(endTime)));
        }

        // timestamp가 reverse되었기 때문에 start, end를 바꿔서 조회.

        byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(application.getApplicationName(), application.getServiceTypeCode(), endTime);
        byte[] endKey = ApplicationMapStatisticsUtils.makeRowKey(application.getApplicationName(), application.getServiceTypeCode(), startTime);

        final Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);
        scan.setStartRow(startKey);
        scan.setStopRow(endKey);
        scan.addFamily(HBaseTables.APPLICATION_MAP_STATISTICS_SELF_CF_COUNTER);
        scan.setId("ApplicationSelfScan");

        return scan;
    }


}
