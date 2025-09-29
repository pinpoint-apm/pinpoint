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

package com.navercorp.pinpoint.collector.applicationmap.statistics.config;

import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkIncrementer;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkUpdater;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.statistics.DefaultBulkIncrementer;
import com.navercorp.pinpoint.collector.applicationmap.statistics.DefaultBulkUpdater;
import com.navercorp.pinpoint.collector.applicationmap.statistics.DefaultBulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.statistics.SyncWriter;
import com.navercorp.pinpoint.collector.monitor.dao.hbase.BulkOperationReporter;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTemplate;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;

import java.util.Objects;

/**
 * @author emeroad
 */
public class BulkFactory {

    private final boolean bulkWriter;
    private final HbaseAsyncTemplate asyncTemplate;
    private final BulkIncrementerFactory bulkIncrementerFactory;
    private final BulkOperationReporterFactory bulkOperationReporterFactory;

    public BulkFactory(boolean bulkWriter,
                       HbaseAsyncTemplate asyncTemplate,
                       BulkIncrementerFactory bulkIncrementerFactory,
                       BulkOperationReporterFactory bulkOperationReporterFactory) {
        this.bulkWriter = bulkWriter;
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
        this.bulkIncrementerFactory = Objects.requireNonNull(bulkIncrementerFactory, "bulkIncrementerFactory");
        this.bulkOperationReporterFactory = Objects.requireNonNull(bulkOperationReporterFactory, "bulkOperationReporterFactory");
    }


    public BulkIncrementer newBulkIncrementer(String reporterName, int limitSize) {
        BulkOperationReporter reporter = bulkOperationReporterFactory.getBulkOperationReporter(reporterName);

        BulkIncrementer bulkIncrementer = new DefaultBulkIncrementer();
        return bulkIncrementerFactory.wrap(bulkIncrementer, limitSize, reporter);
    }


    public BulkUpdater getBulkUpdater(String reporterName, int limitSize) {
        BulkOperationReporter reporter = bulkOperationReporterFactory.getBulkOperationReporter(reporterName);
        BulkUpdater bulkUpdater = new DefaultBulkUpdater();
        return bulkIncrementerFactory.wrap(bulkUpdater, limitSize, reporter);
    }

    public BulkWriter newBulkWriter(String loggerName,
                                    RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                    byte[] family,
                                    BulkIncrementer bulkIncrementer,
                                    BulkUpdater bulkUpdater) {
        if (bulkWriter) {
            return new DefaultBulkWriter(loggerName, asyncTemplate, family, rowKeyDistributorByHashPrefix.getByteHasher(),
                    bulkIncrementer, bulkUpdater);
        } else {
            return new SyncWriter(loggerName, asyncTemplate, family, rowKeyDistributorByHashPrefix.getByteHasher());
        }
    }

    public Builder newBuilder(String loggerName, byte[] family, RowKeyDistributorByHashPrefix distributor) {
        return new Builder(this, loggerName, family, distributor);
    }

    public static class Builder {
        private final String loggerName;
        private final BulkFactory factory;
        private final byte[] family;
        private final RowKeyDistributorByHashPrefix distributor;

        private BulkIncrementer increment;
        private BulkUpdater maxUpdater;

        public Builder(BulkFactory factory,
                       String loggerName,
                       byte[] family,
                       RowKeyDistributorByHashPrefix distributor) {
            this.loggerName = Objects.requireNonNull(loggerName, "loggerName");
            this.factory = Objects.requireNonNull(factory, "factory");
            this.family = Objects.requireNonNull(family, "family");
            this.distributor = Objects.requireNonNull(distributor, "distributor");
        }

        public void setIncrementer(String reporterName, int limitSize) {
            this.increment = factory.newBulkIncrementer(reporterName, limitSize);
        }

        public void setMaxUpdater(String reporterName, int limitSize) {
            this.maxUpdater = factory.getBulkUpdater(reporterName, limitSize);
        }

        public BulkWriter build() {
            return factory.newBulkWriter(loggerName, this.distributor, family, increment, maxUpdater);
        }
    }
}
