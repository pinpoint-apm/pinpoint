package com.navercorp.pinpoint.profiler.transaction;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.transaction.IRequestMappingInfo;
import com.navercorp.pinpoint.profiler.transaction.config.TransactionTypeConfiguration;

public class CustomerDefinitionRegistry extends DefaultRegistryMapping {

    private TransactionTypeConfiguration transactionTypeConfiguration;

    public CustomerDefinitionRegistry(ProfilerConfig profilerConfig) {
        this.transactionTypeConfiguration = new TransactionTypeConfiguration(profilerConfig);
    }


    @Override
    public IRequestMappingInfo match(String uri, String method) {
        return super.match(transactionTypeConfiguration.rules(), uri, method);
    }
}
