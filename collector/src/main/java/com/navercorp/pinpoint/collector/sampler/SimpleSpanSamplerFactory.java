package com.navercorp.pinpoint.collector.sampler;

import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.ToLongFunction;

public class SimpleSpanSamplerFactory implements SpanSamplerFactory {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final boolean spanSamplerEnable;
    private final String spanSamplerType;
    private final long spanModSamplingRate;
    private final String spanPercentSamplingRateStr;

    public SimpleSpanSamplerFactory(CollectorProperties collectorProperties) {
        Objects.requireNonNull(collectorProperties, "collectorProperties");
        this.spanSamplerEnable = collectorProperties.isSpanSamplingEnable();
        this.spanSamplerType = collectorProperties.getSpanSamplingType();
        this.spanModSamplingRate = collectorProperties.getSpanModSamplingRate();
        this.spanPercentSamplingRateStr = collectorProperties.getSpanPercentSamplingRate();
    }

    @Override
    public Sampler<BasicSpan> createBasicSpanSampler() {
        if (spanSamplerEnable) {
            try {
                switch (SamplerType.of(spanSamplerType)) {
                    case PERCENT:
                        return createPercentageSampler(spanPercentSamplingRateStr, createBasicSpanSamplingFunction());
                    case MOD:
                        return createModSampler(spanModSamplingRate, createBasicSpanSamplingFunction());
                    default:
                        break;
                }
            } catch (IllegalArgumentException e) {
                logger.warn("sampling disabled, {}", e.getMessage());
            }
        }

        return TrueSampler.instance();
    }

    private ToLongFunction<BasicSpan> createBasicSpanSamplingFunction() {
        return new BasicSpanSampler();
    }

    private Sampler<BasicSpan> createPercentageSampler(String percentSamplingRateStr,
                                                       ToLongFunction<BasicSpan> function) {
        long percentSamplingRate = PercentRateSampler.parseSamplingRateString(percentSamplingRateStr);
        if (percentSamplingRate >= PercentRateSampler.MAX) {
            return TrueSampler.instance();
        } else if (percentSamplingRate <= 0) {
            return FalseSampler.instance();
        }
        return new PercentRateSampler<>(percentSamplingRate, function);
    }

    private Sampler<BasicSpan> createModSampler(long modSamplingRate,
                                                ToLongFunction<BasicSpan> function) {
        if (modSamplingRate == 1) {
            return TrueSampler.instance();
        }
        return new ModSampler<>(modSamplingRate, function);
    }
}
