/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.collector.dao.hbase.BulkOperationReporter;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class BulkConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BulkIncrementerFactory bulkIncrementerFactory;

    private final int callerLimitSize;

    private final int calleeLimitSize;

    private final int selfLimitSize;
    private final BulkOperationReporterFactory bulkOperationReporterFactory;

    public BulkConfiguration(BulkIncrementerFactory bulkIncrementerFactory,
                             @Value("${collector.cachedStatDao.caller.limit:-1}") int callerLimitSize,
                             @Value("${collector.cachedStatDao.callee.limit:-1}") int calleeLimitSize,
                             @Value("${collector.cachedStatDao.self.limit:-1}") int selfLimitSize,
                             BulkOperationReporterFactory bulkOperationReporterFactory) {

        this.bulkIncrementerFactory = bulkIncrementerFactory;
        this.callerLimitSize = callerLimitSize;
        this.calleeLimitSize = calleeLimitSize;
        this.selfLimitSize = selfLimitSize;
        this.bulkOperationReporterFactory = bulkOperationReporterFactory;
    }

    @Bean("callerBulkIncrementer")
    public BulkIncrementer getCallerBulkIncrementer() {
        String reporterName = "callerBulkReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_CALLER_VER2_COUNTER;
        int limitSize = callerLimitSize;

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean("calleeBulkIncrementer")
    public BulkIncrementer getCalleeBulkIncrementer() {
        String reporterName = "calleeBulkReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_CALLEE_VER2_COUNTER;
        int limitSize = calleeLimitSize;

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean("selfBulkIncrementer")
    public BulkIncrementer getSelfBulkIncrementer() {
        String reporterName = "selfBulkReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_SELF_VER2_COUNTER;
        int limitSize = selfLimitSize;

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    private BulkIncrementer newBulkIncrementer(String reporterName, HbaseColumnFamily hbaseColumnFamily, int limitSize) {
        BulkOperationReporter reporter = bulkOperationReporterFactory.getBulkOperationReporter(reporterName);
        RowKeyMerge merge = new RowKeyMerge(hbaseColumnFamily);
        BulkIncrementer bulkIncrementer = new DefaultBulkIncrementer(merge);

        return bulkIncrementerFactory.wrap(bulkIncrementer, limitSize, reporter);
    }

    @Bean("callerBulkUpdater")
    public BulkUpdater getCallerBulkUpdater() {
        String reporterName = "callerBulkUpdater";
        return getBulkUpdater(reporterName);
    }

    private BulkUpdater getBulkUpdater(String reporterName) {
        BulkOperationReporter reporter = bulkOperationReporterFactory.getBulkOperationReporter(reporterName);
        BulkUpdater bulkUpdater = new DefaultBulkUpdater();
        return bulkIncrementerFactory.wrap(bulkUpdater, calleeLimitSize, reporter);
    }

    @Bean("calleeBulkUpdater")
    public BulkUpdater getCalleeBulkUpdater() {
        String reporterName = "calleeBulkUpdater";
        return getBulkUpdater(reporterName);
    }

    @Bean("selfBulkUpdater")
    public BulkUpdater getSelfBulkUpdater() {
        String reporterName = "selfBulkUpdater";
        return getBulkUpdater(reporterName);
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
    }

    @Override
    public String toString() {
        return "BulkConfiguration{" +
                "callerLimitSize=" + callerLimitSize +
                ", calleeLimitSize=" + calleeLimitSize +
                ", selfLimitSize=" + selfLimitSize +
                '}';
    }

}
