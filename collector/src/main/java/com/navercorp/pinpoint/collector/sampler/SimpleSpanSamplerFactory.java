package com.navercorp.pinpoint.collector.sampler;

import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Function;

public class SimpleSpanSamplerFactory implements SpanSamplerFactory {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final boolean spanSamplerEnable;
    private final SamplerType spanSamplerType;
    private final long spanModSamplerRate;
    private final String spanPercentageSamplerRate;

    public SimpleSpanSamplerFactory(CollectorProperties collectorProperties) {
        Objects.requireNonNull(collectorProperties, "collectorProperties");
        this.spanSamplerEnable = collectorProperties.isSpanSamplingEnable();
        this.spanSamplerType = SamplerType.of(collectorProperties.getSpanSamplingType());
        this.spanModSamplerRate = collectorProperties.getSpanSamplingRate();
        this.spanPercentageSamplerRate = collectorProperties.getSpanSamplingPercent();
    }

    @Override
    public Sampler<BasicSpan> createBasicSpanSampler() {
        if (spanSamplerEnable) {
            try {
                switch (spanSamplerType) {
                    case PERCENT:
                        return createPercentageSampler(spanPercentageSamplerRate, createBasicFunction());
                    case MOD:
                        return createModSampler(spanModSamplerRate, createBasicFunction());
                    default:
                        break;
                }
            } catch (IllegalArgumentException e) {
                logger.warn("sampling disabled, {}", e.getMessage());
            }
        }

        return TrueSampler.instance();
    }

    private Function<BasicSpan, Number> createBasicFunction() {
        return (span -> span.getTransactionId().getTransactionSequence());
    }

    private Sampler<BasicSpan> createPercentageSampler(String spanSamplerPercentageStr,
                                                       Function<BasicSpan, Number> function) {
        long spanSamplerPercentage = PercentRateSampler.parseSamplingRateString(spanSamplerPercentageStr);
        if (spanSamplerPercentage == 0) {
            return FalseSampler.instance();
        } else if (spanSamplerPercentage == PercentRateSampler.MAX) {
            return TrueSampler.instance();
        }
        return new PercentRateSampler<>(spanSamplerPercentage, function);
    }

    private Sampler<BasicSpan> createModSampler(long spanModSamplerRate,
                                                Function<BasicSpan, Number> function) {
        if (spanModSamplerRate == 1) {
            return TrueSampler.instance();
        }
        return new ModSampler<>(spanModSamplerRate, function);
    }
}
