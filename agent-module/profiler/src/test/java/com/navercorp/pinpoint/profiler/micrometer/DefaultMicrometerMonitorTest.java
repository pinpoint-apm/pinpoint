package com.navercorp.pinpoint.profiler.micrometer;

import com.navercorp.pinpoint.profiler.micrometer.config.DefaultMicrometerConfig;
import com.navercorp.pinpoint.profiler.micrometer.config.MicrometerConfig;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class DefaultMicrometerMonitorTest {

    @Test
    void micrometerMonitor() {
        MicrometerConfig config = spy(DefaultMicrometerConfig.class);
        when(config.isEnable()).thenReturn(true);
        when(config.getUrl()).thenReturn("http://localhost:8080");
        when(config.getStep()).thenReturn("10s");
        when(config.getBatchSize()).thenReturn("100");

        DefaultMicrometerMonitor monitor = new DefaultMicrometerMonitor("applicationName", "agentId", config);

        monitor.stop();
    }

}