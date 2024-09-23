package com.navercorp.pinpoint.profiler.micrometer.config;

import com.navercorp.pinpoint.common.config.Value;

public class DefaultMicrometerConfig implements MicrometerConfig {
    @Value("${profiler.micrometer.otlp.enabled}")
    private boolean enable = false;

    @Value("${profiler.micrometer.otlp.url}")
    private String url;

    @Value("${profiler.micrometer.otlp.step}")
    private String step;

    @Value("${profiler.micrometer.otlp.batchSize}")
    private String batchSize;


    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getStep() {
        return step;
    }

    @Override
    public String getBatchSize() {
        return batchSize;
    }

    @Override
    public String toString() {
        return "DefaultMicrometerConfig{" +
                "enable=" + enable +
                ", url='" + url + '\'' +
                ", step='" + step + '\'' +
                ", batchSize='" + batchSize + '\'' +
                '}';
    }
}
