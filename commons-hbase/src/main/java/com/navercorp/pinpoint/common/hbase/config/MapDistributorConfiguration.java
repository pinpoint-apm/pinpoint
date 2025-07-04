package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.hbase.distributor.RangeOneByteSimpleHash;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapDistributorConfiguration {


    @Bean
    public RowKeyDistributorByHashPrefix acceptApplicationRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = new RangeOneByteSimpleHash(0, 24, 4);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapInLinkRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = new RangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapOutLinkRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = new RangeOneByteSimpleHash(0, 36, 32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Bean
    public RowKeyDistributorByHashPrefix mapSelfRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = new RangeOneByteSimpleHash(0, 32, 8);
        return new RowKeyDistributorByHashPrefix(hasher);
    }
}
