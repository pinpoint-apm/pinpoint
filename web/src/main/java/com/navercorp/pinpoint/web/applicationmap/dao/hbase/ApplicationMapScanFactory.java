/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.applicationmap.dao.hbase;

import com.navercorp.pinpoint.common.server.applicationmap.ApplicationMapUtils;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static com.navercorp.pinpoint.common.server.applicationmap.ServiceId.DEFAULT_SERVICE_ID;

/**
 * @author intr3p1d
 */
public class ApplicationMapScanFactory {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final int SCAN_CACHE_SIZE = 40;

    private final RangeFactory rangeFactory;

    private int scanCacheSize;

    public ApplicationMapScanFactory(RangeFactory rangeFactory) {
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


        byte[] startKey = ApplicationMapUtils.makeRowKey(
                DEFAULT_SERVICE_ID, application.getName(), application.getServiceTypeCode(), range.getTo()
        );
        byte[] endKey = ApplicationMapUtils.makeRowKey(
                DEFAULT_SERVICE_ID, application.getName(), application.getServiceTypeCode(), range.getFrom()
        );

        final Scan scan = new Scan();
        scan.setCaching(scanCacheSize);
        scan.withStartRow(startKey);
        scan.withStopRow(endKey);
        scan.addFamily(family);
        scan.setId(id);

        return scan;
    }

}
