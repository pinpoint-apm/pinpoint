package com.nhn.pinpoint.web.dao.hbase;

import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.web.dao.MapResponseDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.RangeFactory;
import com.nhn.pinpoint.web.vo.ResponseTime;
import org.apache.hadoop.hbase.client.Scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapResponseTimeDao implements MapResponseDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String tableName = HBaseTables.MAP_STATISTICS_SELF;

    private int scanCacheSize = 40;

    @Autowired
    private RowMapper<ResponseTime> responseTimeMapper;

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    private RangeFactory rangeFactory;


    @Override
    public List<ResponseTime> selectResponseTime(Application application, Range range) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("selectResponseTime applicationName:{}, {}", application, range);
        }
        Scan scan = createScan(application, range);
        List<ResponseTime> responseTimeList = hbaseOperations2.find(tableName, scan, responseTimeMapper);
        if (logger.isDebugEnabled()) {
            logger.debug("row:{}", responseTimeList.size());
            for (ResponseTime responseTime : responseTimeList) {
                logger.trace("responseTime:{}", responseTime);
            }
        }

        return responseTimeList;
    }

    private Scan createScan(Application application, Range range) {
        range = rangeFactory.createReverseStatisticsRange(range);


        if (logger.isDebugEnabled()) {
            logger.debug("scan time:{} ", range.prettyToString());
        }

        // timestamp가 reverse되었기 때문에 start, end를 바꿔서 조회.

        byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), range.getFrom());
        byte[] endKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), range.getTo());

        final Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);
        scan.setStartRow(startKey);
        scan.setStopRow(endKey);
        scan.addFamily(HBaseTables.MAP_STATISTICS_SELF_CF_COUNTER);
        scan.setId("ApplicationSelfScan");

        return scan;
    }


}
