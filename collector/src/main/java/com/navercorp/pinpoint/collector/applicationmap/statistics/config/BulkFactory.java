package com.navercorp.pinpoint.collector.applicationmap.statistics.config;

import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapInLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapOutLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.dao.hbase.HbaseMapResponseTimeDao;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkIncrementer;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkUpdater;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.statistics.DefaultBulkIncrementer;
import com.navercorp.pinpoint.collector.applicationmap.statistics.DefaultBulkUpdater;
import com.navercorp.pinpoint.collector.applicationmap.statistics.DefaultBulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.statistics.RowKeyMerge;
import com.navercorp.pinpoint.collector.applicationmap.statistics.SyncWriter;
import com.navercorp.pinpoint.collector.monitor.dao.hbase.BulkOperationReporter;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTemplate;
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
                                     HbaseOperations hbaseTemplate,
                                     HbaseAsyncTemplate asyncTemplate,
                                     HbaseColumnFamily descriptor,
                                     TableNameProvider tableNameProvider,
                                     RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                     BulkIncrementer bulkIncrementer,
                                     BulkUpdater bulkUpdater) {
        if (bulkConfiguration.enableBulk()) {
            return new DefaultBulkWriter(loggerName, asyncTemplate, rowKeyDistributorByHashPrefix,
                    bulkIncrementer, bulkUpdater, descriptor, tableNameProvider);
        } else {
            return new SyncWriter(loggerName, hbaseTemplate, rowKeyDistributorByHashPrefix, descriptor, tableNameProvider);
        }
    }

    @Bean
    public BulkIncrementer outLinkBulkIncrementer() {
        String reporterName = "callerBulkIncrementerReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_CALLER_VER2_COUNTER;
        int limitSize = bulkConfiguration.getCallerLimitSize();

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean
    public BulkUpdater outLinkBulkUpdater() {
        String reporterName = "callerBulkUpdaterReporter";
        return getBulkUpdater(reporterName);
    }


    @Bean
    public BulkWriter outLinkBulkWriter(HbaseOperations hbaseTemplate,
                                        HbaseAsyncTemplate asyncTemplate,
                                        TableNameProvider tableNameProvider,
                                        @Qualifier("mapOutLinkRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                        @Qualifier("outLinkBulkIncrementer") BulkIncrementer bulkIncrementer,
                                        @Qualifier("outLinkBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapOutLinkDao.class.getName());
        return newBulkWriter(loggerName, hbaseTemplate, asyncTemplate, HbaseColumnFamily.MAP_STATISTICS_CALLEE_VER2_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }


    @Bean
    public BulkIncrementer inLinkBulkIncrementer() {
        String reporterName = "calleeBulkIncrementerReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_CALLEE_VER2_COUNTER;
        int limitSize = bulkConfiguration.getCalleeLimitSize();

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }


    @Bean
    public BulkUpdater inLinkBulkUpdater() {
        String reporterName = "calleeBulkUpdaterReporter";
        return getBulkUpdater(reporterName);
    }

    @Bean
    public BulkWriter inLinkBulkWriter(HbaseOperations hbaseTemplate,
                                       HbaseAsyncTemplate asyncTemplate,
                                       TableNameProvider tableNameProvider,
                                       @Qualifier("mapInLinkRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                       @Qualifier("inLinkBulkIncrementer") BulkIncrementer bulkIncrementer,
                                       @Qualifier("inLinkBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapInLinkDao.class.getName());
        return newBulkWriter(loggerName, hbaseTemplate, asyncTemplate, HbaseColumnFamily.MAP_STATISTICS_CALLER_VER2_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }

    @Bean
    public BulkIncrementer selfBulkIncrementer() {
        String reporterName = "selfBulkIncrementerReporter";
        HbaseColumnFamily hbaseColumnFamily = HbaseColumnFamily.MAP_STATISTICS_SELF_VER2_COUNTER;
        int limitSize = bulkConfiguration.getSelfLimitSize();

        return newBulkIncrementer(reporterName, hbaseColumnFamily, limitSize);
    }

    @Bean
    public BulkUpdater selfBulkUpdater() {
        String reporterName = "selfBulkUpdaterReporter";
        return getBulkUpdater(reporterName);
    }

    @Bean
    public BulkWriter selfBulkWriter(HbaseOperations hbaseTemplate,
                                     HbaseAsyncTemplate asyncTemplate,
                                     TableNameProvider tableNameProvider,
                                     @Qualifier("mapSelfRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix,
                                     @Qualifier("selfBulkIncrementer") BulkIncrementer bulkIncrementer,
                                     @Qualifier("selfBulkUpdater") BulkUpdater bulkUpdater) {
        String loggerName = newBulkWriterName(HbaseMapResponseTimeDao.class.getName());
        return newBulkWriter(loggerName, hbaseTemplate, asyncTemplate, HbaseColumnFamily.MAP_STATISTICS_SELF_VER2_COUNTER, tableNameProvider, rowKeyDistributorByHashPrefix, bulkIncrementer, bulkUpdater);
    }

    private String newBulkWriterName(String className) {
        return className + "-writer";
    }
}
