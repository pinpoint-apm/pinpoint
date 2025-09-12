/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.dao.hbase;

import com.google.common.primitives.Ints;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.DefaultTimeSlot;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class MapScanFactory {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final int SCAN_CACHE_SIZE_MIN = 8;
    public static final int SCAN_CACHE_SIZE_MAX = 1024;

    private long slotSize = DefaultTimeSlot.ONE_MIN_RESOLUTION;

    private final RangeFactory rangeFactory;

    public MapScanFactory(RangeFactory rangeFactory) {
        this.rangeFactory = Objects.requireNonNull(rangeFactory, "rangeFactory");
    }

    public void setSlotSize(long slotSize) {
        this.slotSize = slotSize;
    }

    public Scan createScan(String id, Application application, Range range, byte[] family) {
        range = rangeFactory.createStatisticsRange(range);
        if (logger.isDebugEnabled()) {
            logger.debug("scan time:{} ", range.prettyToString());
        }
        // start key is replaced by end key because timestamp has been reversed
        byte[] startKey = rowKey(application, range.getTo());
        byte[] endKey = rowKey(application, range.getFrom());

        final Scan scan = new Scan();

        final int scannerCaching = computeScannerCaching(range);
        scan.setCaching(scannerCaching);
        scan.withStartRow(startKey);
        scan.withStopRow(endKey);
        scan.addFamily(family);
        scan.setId(id);

        return scan;
    }

    protected byte[] rowKey(Application application, long range) {
        return LinkRowKey.makeRowKey(ByteSaltKey.NONE.size(), application.getName(), (short) application.getServiceTypeCode(), range);
    }

    private int computeScannerCaching(Range range) {
        int windowCount = (int)(range.durationMillis() / slotSize) + 1;
        int scannerCaching = Ints.constrainToRange(windowCount, SCAN_CACHE_SIZE_MIN, SCAN_CACHE_SIZE_MAX);
        if (logger.isDebugEnabled()) {
            logger.debug("ScannerCaching:{} windowCount:{}", scannerCaching, windowCount);
        }
        return scannerCaching;
    }
}
