package com.navercorp.pinpoint.profiler.micrometer;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.ApplicationName;
import com.navercorp.pinpoint.profiler.micrometer.config.MicrometerConfig;

import java.util.Objects;

public class MicrometerMonitorProvider implements Provider<MicrometerMonitor> {

    private final String applicationName;
    private final String agentId;
    private final MicrometerConfig micrometerConfig;

    @Inject
    public MicrometerMonitorProvider(@ApplicationName String applicationName,
                                     @AgentId String agentId,
                                     MicrometerConfig micrometerConfig) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.micrometerConfig = Objects.requireNonNull(micrometerConfig, "micrometerConfig");
    }

    @Override
    public MicrometerMonitor get() {
        if (micrometerConfig.isEnable()) {
            return new DefaultMicrometerMonitor(applicationName, agentId, micrometerConfig);
        } else {
            return new DisableMicrometerMonitor();
        }
    }
}
