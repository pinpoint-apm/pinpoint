package com.navercorp.pinpoint.collector.service;

import jakarta.validation.Valid;

public interface OtlpMetricService {
    void save(@Valid Object data);
}
