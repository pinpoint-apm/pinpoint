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

package com.navercorp.pinpoint.common.hbase.parallel;

import com.navercorp.pinpoint.common.hbase.TableFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ScanTaskConfig {

    private final TableName tableName;
    private final TableFactory tableFactory;

    private final int saltKeySize;
    private final int scanTaskQueueSize;
    private final boolean scanMetricsEnabled;

    public static ScanTaskConfig of(TableName tableName, TableFactory tableFactory, int saltKeySize, int scanCaching, Configuration configuration, boolean scanMetricsEnabled) {
        int scanTaskQueueSize = scanTaskQueueSize(scanCaching, configuration);
        return new ScanTaskConfig(tableName, tableFactory, saltKeySize, scanTaskQueueSize, scanMetricsEnabled);
    }

    private static int scanTaskQueueSize(int scanCaching, Configuration configuration) {
        if (scanCaching > 0) {
            return scanCaching;
        } else {
            return configuration.getInt(
                    HConstants.HBASE_CLIENT_SCANNER_CACHING,
                    HConstants.DEFAULT_HBASE_CLIENT_SCANNER_CACHING);
        }
    }

    public ScanTaskConfig(TableName tableName, TableFactory tableFactory, int saltKeySize, int scanTaskQueueSize, boolean scanMetricsEnabled) {
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.tableFactory = Objects.requireNonNull(tableFactory, "tableFactory");
        this.saltKeySize = saltKeySize;
        this.scanTaskQueueSize = scanTaskQueueSize;
        this.scanMetricsEnabled = scanMetricsEnabled;
    }

    public TableName getTableName() {
        return tableName;
    }


    public TableFactory getTableFactory() {
        return tableFactory;
    }

    public int getSaltKeySize() {
        return saltKeySize;
    }

    public int getScanTaskQueueSize() {
        return scanTaskQueueSize;
    }

    public boolean isScanMetricsEnabled() {
        return scanMetricsEnabled;
    }
}
