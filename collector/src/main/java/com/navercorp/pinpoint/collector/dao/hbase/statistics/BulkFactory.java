package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.collector.dao.hbase.BulkOperationReporter;
import com.navercorp.pinpoint.collector.dao.hbase.HbaseMapResponseTimeDao;
import com.navercorp.pinpoint.collector.dao.hbase.HbaseMapStatisticsCalleeDao;
import com.navercorp.pinpoint.collector.dao.hbase.HbaseMapStatisticsCallerDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * @author emeroad
 */
@Configuration
public class BulkFactory {

    private final BulkConfiguration bulkConfiguration;
    private final BulkIncrementerFactory bulkIncrementerFactory;
    private final BulkOperationReporterFactory bulkOperationReporterFactory;

    public BulkFactory(BulkConfiguration bulkConfiguration,
                       BulkIncrementerFactory bulkIncrementerFactory,
                       BulkOperationReporterFactory bulkOperationReporterFactory) {
        this.bulkConfiguration = Objects.requireNonNull(bulkConfiguration, "bulkConfiguration");
        this.bulkIncrementerFactory = Objects.requireNonNull(bulkIncrementerFactory, "bulkIncrementerFactory");
        this.bulkOperationReporterFactory = Objects.requireNonNull(bulkOperationReporterFactory, "bulkOperationReporterFactory");
    }


    private BulkIncrementer newBulkIncrementer(String reporterName, HbaseColumnFamily hbaseColumnFamily, int limitSize) {
        BulkOperationReporter reporter = bulkOperationReporterFactory.getBulkOperationReporter(reporterName);
        RowKeyMerge merge = new RowKeyMerge(hbaseColumnFamily);
        BulkIncrementer bulkIncrementer = new DefaultBulkIncrementer(merge);

        return bulkIncrementerFactory.wrap(bulkIncrementer, limitSize, reporter);
    }


    private BulkUpdater getBulkUpdater(String reporterName) {
        BulkOperationReporter reporter = bulkOperationReporterFactory.getBulkOperationReporter(reporterName);
        BulkUpdater bulkUpdater = new DefaultBulkUpdater();
        return bulkIncrementerFactory.wrap(bulkUpdater, bulkConfiguration.getCalleeLimitSize(), reporter);
    }

    private BulkWriter newBulkWriter(String loggerName,
                                     HbaseOperations2 hbaseTemplate,
                                     HbaseColumnFamily descriptor,
                                     TableNameProvider tableNameProvider,
                                     RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                     BulkIncrementer bulkIncrementer,
                                     BulkUpdater bulkUpdater) {
        if (bulkConfiguration.enableBulk()) {
            return new DefaultBulkWriter(loggerName, hbaseTemplate, rowKeyDistributorByHashPrefix,
                    bulkIncrementer, bulkUpdater, descriptor, tableNameProvider);
        } else {
            return new SyncWriter(loggerName, hbaseTemplate, rowKeyDistributorByHashPrefix, descriptor, tableNameProvider);
        }
    }

    @Bean("callerBulkIncrementer")
    public BulkIncrementer getCallerBulkIncrementer() {
        String reporterName = "callerBulkIncrementerReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_CALLER_VER2_COUNTER;
        int limitSize = bulkConfiguration.getCallerLimitSize();

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean("callerBulkUpdater")
    public BulkUpdater getCallerBulkUpdater() {
        String reporterName = "callerBulkUpdaterReporter";
        return getBulkUpdater(reporterName);
    }


    @Bean("callerBulkWriter")
    public BulkWriter newCallerBulkWriter(HbaseOperations2 hbaseTemplate,
                                          TableNameProvider tableNameProvider,
                                          @Qualifier("statisticsCallerRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                          @Qualifier("callerBulkIncrementer") BulkIncrementer bulkIncrementer,
                                          @Qualifier("callerBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapStatisticsCallerDao.class.getName());
        return newBulkWriter(loggerName, hbaseTemplate, HbaseColumnFamily.MAP_STATISTICS_CALLEE_VER2_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }


    @Bean("calleeBulkIncrementer")
    public BulkIncrementer getCalleeBulkIncrementer() {
        String reporterName = "calleeBulkIncrementerReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_CALLEE_VER2_COUNTER;
        int limitSize = bulkConfiguration.getCalleeLimitSize();

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }


    @Bean("calleeBulkUpdater")
    public BulkUpdater getCalleeBulkUpdater() {
        String reporterName = "calleeBulkUpdaterReporter";
        return getBulkUpdater(reporterName);
    }

    @Bean("calleeBulkWriter")
    public BulkWriter newCalleeBulkWriter(HbaseOperations2 hbaseTemplate,
                                          TableNameProvider tableNameProvider,
                                          @Qualifier("statisticsCalleeRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                          @Qualifier("calleeBulkIncrementer") BulkIncrementer bulkIncrementer,
                                          @Qualifier("calleeBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapStatisticsCalleeDao.class.getName());
        return newBulkWriter(loggerName, hbaseTemplate, HbaseColumnFamily.MAP_STATISTICS_CALLER_VER2_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }

    @Bean("selfBulkIncrementer")
    public BulkIncrementer getSelfBulkIncrementer() {
        String reporterName = "selfBulkIncrementerReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_SELF_VER2_COUNTER;
        int limitSize = bulkConfiguration.getSelfLimitSize();

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean("selfBulkUpdater")
    public BulkUpdater getSelfBulkUpdater() {
        String reporterName = "selfBulkUpdaterReporter";
        return getBulkUpdater(reporterName);
    }

    @Bean("selfBulkWriter")
    public BulkWriter newSelfBulkWriter(HbaseOperations2 hbaseTemplate,
                                        TableNameProvider tableNameProvider,
                                        @Qualifier("statisticsSelfRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                        @Qualifier("selfBulkIncrementer") BulkIncrementer bulkIncrementer,
                                        @Qualifier("selfBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapResponseTimeDao.class.getName());
        return newBulkWriter(loggerName, hbaseTemplate, HbaseColumnFamily.MAP_STATISTICS_SELF_VER2_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }

    @Bean("callerCompactBulkIncrementer")
    public BulkIncrementer getCallerCompactBulkIncrementer() {
        String reporterName = "callerCompactBulkIncrementerReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_CALLER_COMPACT_COUNTER;
        int limitSize = bulkConfiguration.getCallerLimitSize();

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean("callerCompactBulkUpdater")
    public BulkUpdater getCallerCompactBulkUpdater() {
        String reporterName = "callerCompactBulkUpdaterReporter";
        return getBulkUpdater(reporterName);
    }


    @Bean("callerCompactBulkWriter")
    public BulkWriter newCallerCompactBulkWriter(HbaseOperations2 hbaseTemplate,
                                          TableNameProvider tableNameProvider,
                                          @Qualifier("statisticsCallerCompactRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                          @Qualifier("callerCompactBulkIncrementer") BulkIncrementer bulkIncrementer,
                                          @Qualifier("callerCompactBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapStatisticsCallerDao.class.getName());
        return newBulkWriter(loggerName, hbaseTemplate, HbaseColumnFamily.MAP_STATISTICS_CALLEE_COMPACT_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }


    @Bean("calleeCompactBulkIncrementer")
    public BulkIncrementer getCalleeCompactBulkIncrementer() {
        String reporterName = "calleeCompactBulkIncrementerReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_CALLEE_COMPACT_COUNTER;
        int limitSize = bulkConfiguration.getCalleeLimitSize();

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }


    @Bean("calleeCompactBulkUpdater")
    public BulkUpdater getCalleeCompactBulkUpdater() {
        String reporterName = "calleeCompactBulkUpdaterReporter";
        return getBulkUpdater(reporterName);
    }

    @Bean("calleeCompactBulkWriter")
    public BulkWriter newCalleeCompactBulkWriter(HbaseOperations2 hbaseTemplate,
                                          TableNameProvider tableNameProvider,
                                          @Qualifier("statisticsCalleeCompactRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                          @Qualifier("calleeCompactBulkIncrementer") BulkIncrementer bulkIncrementer,
                                          @Qualifier("calleeCompactBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapStatisticsCalleeDao.class.getName());
        return newBulkWriter(loggerName, hbaseTemplate, HbaseColumnFamily.MAP_STATISTICS_CALLER_COMPACT_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }

    private String newBulkWriterName(String className) {
        return className + "-writer";
    }
}
