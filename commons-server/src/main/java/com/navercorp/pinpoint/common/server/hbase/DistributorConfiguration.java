package com.navercorp.pinpoint.common.server.hbase;

import com.navercorp.pinpoint.common.hbase.distributor.RangeOneByteSimpleHash;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DistributorConfiguration {

    @Bean("applicationTraceIndexDistributor")
    public RowKeyDistributorByHashPrefix getApplicationTraceIndexDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newOneByteSimpleHash(32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    private RowKeyDistributorByHashPrefix.Hasher newOneByteSimpleHash(int maxBuckets) {
        return new RowKeyDistributorByHashPrefix.OneByteSimpleHash(maxBuckets);
    }

    @Bean("traceV2Distributor")
    public RowKeyDistributorByHashPrefix getTraceV2Distributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(32, 40, 256);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean("applicationStatRowKeyDistributor")
    public RowKeyDistributorByHashPrefix getApplicationStatRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 33, 64);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean("agentStatV2RowKeyDistributor")
    public RowKeyDistributorByHashPrefix getAgentStatV2RowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 33, 64);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean("metadataRowKeyDistributor")
    public RowKeyDistributorByHashPrefix getMetadataRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean("metadataRowKeyDistributor2")
    public RowKeyDistributorByHashPrefix getMetadataRowKeyDistributor2() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean("acceptApplicationRowKeyDistributor")
    public RowKeyDistributorByHashPrefix getAcceptApplicationRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 24, 4);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean("statisticsCalleeRowKeyDistributor")
    public RowKeyDistributorByHashPrefix getStatisticsCalleeRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean("statisticsCallerRowKeyDistributor")
    public RowKeyDistributorByHashPrefix getStatisticsCallerRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean("statisticsSelfRowKeyDistributor")
    public RowKeyDistributorByHashPrefix getStatisticsSelfRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = newRangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    private RowKeyDistributorByHashPrefix.Hasher newRangeOneByteSimpleHash(int start, int end, int maxBuckets) {
        return new RangeOneByteSimpleHash(start, end, maxBuckets);
    }

}
