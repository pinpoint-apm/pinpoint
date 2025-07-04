package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.hbase.distributor.RangeOneByteSimpleHash;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MapDistributorConfiguration.class)
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
        RowKeyDistributorByHashPrefix.Hasher hasher = new RangeOneByteSimpleHash(32, 40, 256);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix applicationStatRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = new RangeOneByteSimpleHash(0, 33, 64);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix agentStatV2RowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = new RangeOneByteSimpleHash(0, 33, 64);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix metadataRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = new RangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix metadataRowKeyDistributor2() {
        RowKeyDistributorByHashPrefix.Hasher hasher = new RangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }


}
