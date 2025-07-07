package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
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
        ByteHasher hasher = newOneByteSimpleHash(32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    private ByteHasher newOneByteSimpleHash(int maxBuckets) {
        return new OneByteSimpleHash(maxBuckets);
    }

    @Bean
    public RowKeyDistributorByHashPrefix traceV2Distributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(32, 40, 256);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix applicationStatRowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 33, 64);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix agentStatV2RowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 33, 64);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix metadataRowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix metadataRowKeyDistributor2() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix acceptApplicationRowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 24, 4);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapInLinkRowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapOutLinkRowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapSelfRowKeyDistributor() {
        ByteHasher hasher = newRangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    private ByteHasher newRangeOneByteSimpleHash(int start, int end, int maxBuckets) {
        return new RangeOneByteSimpleHash(start, end, maxBuckets);
    }

}
