package com.navercorp.pinpoint.profiler.micrometer.config;

public interface MicrometerConfig {
    boolean isEnable();

    String getUrl();

    String getStep();

    String getBatchSize();
}
