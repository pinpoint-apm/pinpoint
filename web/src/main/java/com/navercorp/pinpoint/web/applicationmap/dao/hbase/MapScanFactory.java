package com.navercorp.pinpoint.web.applicationmap.dao.hbase;

import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class MapScanFactory {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final int SCAN_CACHE_SIZE = 40;

    private final RangeFactory rangeFactory;

    private int scanCacheSize;

    public MapScanFactory(RangeFactory rangeFactory) {
        this.rangeFactory = Objects.requireNonNull(rangeFactory, "rangeFactory");
        this.scanCacheSize = SCAN_CACHE_SIZE;
    }

    public void setScanCacheSize(int scanCacheSize) {
        this.scanCacheSize = scanCacheSize;
    }

    public Scan createScan(String id, Application application, Range range, byte[] family) {
        range = rangeFactory.createStatisticsRange(range);
        if (logger.isDebugEnabled()) {
            logger.debug("scan time:{} ", range.prettyToString());
        }

        // start key is replaced by end key because timestamp has been reversed
        byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(application.name(), application.getServiceTypeCode(), range.getTo());
        byte[] endKey = ApplicationMapStatisticsUtils.makeRowKey(application.name(), application.getServiceTypeCode(), range.getFrom());

        final Scan scan = new Scan();
        scan.setCaching(this.scanCacheSize);
        scan.withStartRow(startKey);
        scan.withStopRow(endKey);
        scan.addFamily(family);
        scan.setId(id);

        return scan;
    }
}
