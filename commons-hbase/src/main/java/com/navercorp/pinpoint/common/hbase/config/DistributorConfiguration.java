package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.hbase.wd.Hasher;
import com.navercorp.pinpoint.common.hbase.wd.OneByteSimpleHash;
import com.navercorp.pinpoint.common.hbase.wd.RangeOneByteSimpleHash;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
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
        Hasher hasher = newOneByteSimpleHash(32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    private Hasher newOneByteSimpleHash(int maxBuckets) {
        return new OneByteSimpleHash(maxBuckets);
    }

    @Bean
    public RowKeyDistributorByHashPrefix traceV2Distributor() {
        Hasher hasher = newRangeOneByteSimpleHash(32, 40, 256);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix applicationStatRowKeyDistributor() {
        Hasher hasher = newRangeOneByteSimpleHash(0, 33, 64);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix agentStatV2RowKeyDistributor() {
        Hasher hasher = newRangeOneByteSimpleHash(0, 33, 64);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix metadataRowKeyDistributor() {
        Hasher hasher = newRangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix metadataRowKeyDistributor2() {
        Hasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix acceptApplicationRowKeyDistributor() {
        Hasher hasher = newRangeOneByteSimpleHash(0, 24, 4);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapInLinkRowKeyDistributor() {
        Hasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapOutLinkRowKeyDistributor() {
        Hasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapSelfRowKeyDistributor() {
        Hasher hasher = newRangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    private Hasher newRangeOneByteSimpleHash(int start, int end, int maxBuckets) {
        return new RangeOneByteSimpleHash(start, end, maxBuckets);
    }

}
