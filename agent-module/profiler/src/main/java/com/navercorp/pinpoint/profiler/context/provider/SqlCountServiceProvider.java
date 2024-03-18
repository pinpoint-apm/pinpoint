package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.DefaultSqlCountService;
import com.navercorp.pinpoint.profiler.context.DisableSqlCountService;
import com.navercorp.pinpoint.profiler.context.SqlCountService;
import com.navercorp.pinpoint.profiler.context.monitor.config.MonitorConfig;

import java.util.Objects;

public class SqlCountServiceProvider implements Provider<SqlCountService> {
    private final MonitorConfig monitorConfig;

    @Inject
    public SqlCountServiceProvider(MonitorConfig monitorConfig) {
        this.monitorConfig = Objects.requireNonNull(monitorConfig, "monitorConfig");
    }

    @Override
    public SqlCountService get() {
        if (monitorConfig.isSqlErrorEnable()) {
            return new DefaultSqlCountService(monitorConfig.getSqlErrorCount());
        } else {
            return new DisableSqlCountService();
        }
    }
}
