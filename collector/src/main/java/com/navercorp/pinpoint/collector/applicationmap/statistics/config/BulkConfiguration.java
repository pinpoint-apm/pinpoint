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

import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapInLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapOutLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapResponseTimeDao;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTemplate;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        BulkProperties.class,

})
public class BulkConfiguration {

    @Bean
    public BulkIncrementerFactory bulkIncrementerFactory() {
        return new BulkIncrementerFactory();
    }

    @Bean
    public BulkOperationReporterFactory bulkOperationReporterFactory() {
        return new BulkOperationReporterFactory();
    }

    @Bean
    public BulkFactory bulkFactory(BulkProperties bulkProperties,
                                   HbaseAsyncTemplate asyncTemplate,
                                   BulkIncrementerFactory bulkIncrementerFactory,
                                   BulkOperationReporterFactory bulkOperationReporterFactory) {
        return new BulkFactory(bulkProperties.enableBulk(), asyncTemplate, bulkIncrementerFactory, bulkOperationReporterFactory);
    }

    @Bean
    public BulkWriter outLinkBulkWriter(BulkFactory factory,
                                        BulkProperties bulkProperties,
                                        @Qualifier("mapLinkRowKeyDistributor")
                                        RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {

        int limitSize = bulkProperties.getCallerLimitSize();
        String loggerName = newBulkWriterName(HbaseMapOutLinkDao.class.getName());

        BulkFactory.Builder builder = factory.newBuilder(loggerName, rowKeyDistributorByHashPrefix);
        builder.setIncrementer("outLinkIncrementReporter", limitSize);
        builder.setMaxUpdater("outLinkUpdateReporter", limitSize);
        return builder.build();
    }

    @Bean
    public BulkWriter inLinkBulkWriter(BulkFactory factory,
                                       BulkProperties bulkProperties,
                                       @Qualifier("mapLinkRowKeyDistributor")
                                       RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {

        int limitSize = bulkProperties.getCalleeLimitSize();
        String loggerName = newBulkWriterName(HbaseMapInLinkDao.class.getName());

        BulkFactory.Builder builder = factory.newBuilder(loggerName, rowKeyDistributorByHashPrefix);
        builder.setIncrementer("inLinkIncrementReporter", limitSize);
        builder.setMaxUpdater("inLinkUpdateReporter", limitSize);
        return builder.build();
    }


    @Bean
    public BulkWriter selfBulkWriter(BulkFactory factory,
                                     BulkProperties bulkProperties,
                                     @Qualifier("mapSelfRowKeyDistributor")
                                     RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {

        int limitSize = bulkProperties.getSelfLimitSize();
        String loggerName = newBulkWriterName(HbaseMapResponseTimeDao.class.getName());

        BulkFactory.Builder builder = factory.newBuilder(loggerName, rowKeyDistributorByHashPrefix);
        builder.setIncrementer("selfIncrementReporter", limitSize);
        builder.setMaxUpdater("selfUpdateReporter", limitSize);
        return builder.build();
    }

    private String newBulkWriterName(String className) {
        return className + "-writer";
    }
}
