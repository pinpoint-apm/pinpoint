package com.navercorp.pinpoint.common.server.bo.codec.stat;

import com.navercorp.pinpoint.common.hbase.distributor.RangeOneByteSimpleHash;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "com.navercorp.pinpoint.common.server.bo.codec.stat",
        "com.navercorp.pinpoint.common.server.bo.serializer.stat"
})
public class CodecTestConfig {

    @Bean
    public RowKeyDistributorByHashPrefix agentStatV2RowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = agentStatV2RangeHasher();
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    private RowKeyDistributorByHashPrefix.Hasher agentStatV2RangeHasher() {
        return new RangeOneByteSimpleHash(0, 33, 64);
    }

    @Bean
    public RowKeyDistributorByHashPrefix applicationStatRowKeyDistributor() {
        RowKeyDistributorByHashPrefix.Hasher hasher = applicationStatRangeHasher();
        return new RowKeyDistributorByHashPrefix(hasher);
    }


    private RowKeyDistributorByHashPrefix.Hasher applicationStatRangeHasher() {
        return new RangeOneByteSimpleHash(0, 33, 64);
    }
}
