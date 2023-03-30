package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.hbase.distributor.RangeOneByteSimpleHash;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DistributorConfiguration {

    private final Logger logger = LogManager.getLogger(DistributorConfiguration.class);

    public DistributorConfiguration() {
        logger.info("Install {}", DistributorConfiguration.class.getSimpleName());
    }

    @Bean
    public RowKeyDistributorByHashPrefix applicationTraceIndexDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newOneByteSimpleHash(32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    private RowKeyDistributorByHashPrefix.Hasher newOneByteSimpleHash(int maxBuckets) {
        return new RowKeyDistributorByHashPrefix.OneByteSimpleHash(maxBuckets);
    }

    @Bean
    public RowKeyDistributorByHashPrefix traceV2Distributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(32, 40, 256);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix applicationStatRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 33, 64);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix agentStatV2RowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 33, 64);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix metadataRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix metadataRowKeyDistributor2() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix acceptApplicationRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 24, 4);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix statisticsCalleeRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix statisticsCallerRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix statisticsSelfRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    private RowKeyDistributorByHashPrefix.Hasher newRangeOneByteSimpleHash(int start, int end, int maxBuckets) {
        return new RangeOneByteSimpleHash(start, end, maxBuckets);
    }

}
