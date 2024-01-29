package com.navercorp.pinpoint.collector.sampler;

import com.navercorp.pinpoint.common.server.bo.BasicSpan;

public interface SpanSamplerFactory {
    Sampler<BasicSpan> createBasicSpanSampler();
}
