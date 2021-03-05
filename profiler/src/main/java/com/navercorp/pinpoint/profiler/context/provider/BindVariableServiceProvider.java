package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindVariableService;
import com.navercorp.pinpoint.profiler.jdbc.BindValueConverter;
import com.navercorp.pinpoint.profiler.jdbc.DefaultBindVariableService;
import com.navercorp.pinpoint.profiler.jdbc.JdbcContextConfig;

import java.util.Objects;


public class BindVariableServiceProvider implements Provider<BindVariableService> {
    private final JdbcContextConfig jdbcContextConfig;

    @Inject
    public BindVariableServiceProvider(ProfilerConfig profilerConfig) {
        Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.jdbcContextConfig = new JdbcContextConfig(profilerConfig);

    }

    @Override
    public BindVariableService get() {
        BindValueConverter bindVariable = BindValueConverter.defaultBindValueConverter();

        if (jdbcContextConfig.getByteFormat() == JdbcContextConfig.ByteFormat.raw) {
            bindVariable.setRawBytesConverter();
        }

        BindVariableService bindVariableService = new DefaultBindVariableService(bindVariable);

        oldVersionCompatibility(bindVariableService);

        return bindVariableService;
    }

    private void oldVersionCompatibility(BindVariableService bindVariableService) {
        com.navercorp.pinpoint.bootstrap.plugin.jdbc.bindvalue.BindValueConverter.setBindVariableService(bindVariableService);
        com.navercorp.pinpoint.bootstrap.plugin.jdbc.bindvalue.BindValueUtils.setBindVariableService(bindVariableService);
    }
}
