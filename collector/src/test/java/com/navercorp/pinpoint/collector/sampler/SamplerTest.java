package com.navercorp.pinpoint.collector.sampler;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class SamplerTest {

    Function<Long, Long> identityFunction = (num -> num);

    @Test
    public void falseSamplerTest() {
        Sampler<Long> sampler = FalseSampler.instance();
        Sampler<Long> percentageSampler = new PercentRateSampler<>("0", identityFunction);

        for (long i = 0L; i < 10L; i++) {
            assertThat(sampler.isSampling(i)).isFalse();
            assertThat(percentageSampler.isSampling(i)).isFalse();
        }
    }

    @Test
    public void trueSamplerTest() {
        Sampler<Long> sampler = TrueSampler.instance();
        Sampler<Long> modSampler = new ModSampler<>(1, identityFunction);
        Sampler<Long> percentageSampler = new PercentRateSampler<>("100", identityFunction);

        for (long i = 0L; i < 10L; i++) {
            assertThat(sampler.isSampling(i)).isTrue();
            assertThat(modSampler.isSampling(i)).isTrue();
            assertThat(percentageSampler.isSampling(i)).isTrue();
        }
    }

    @Test
    public void modSamplerTest() {
        long samplingRate = 5;

        Sampler<Long> sampler = new ModSampler<>(samplingRate, identityFunction);
        for (long i = 0L; i < 10L; i++) {
            assertThat(sampler.isSampling(i)).isEqualTo((i % samplingRate) == 0);
        }
    }

    @Test
    public void modSamplerFunctionTest() {
        long samplingRate = 5;

        Sampler<Long> sampler = new ModSampler<>(samplingRate, identityFunction);
        for (long i = 0L; i < 10L; i++) {
            assertThat(sampler.isSampling(i)).isEqualTo((i % samplingRate) == 0);
        }
    }

    @Test
    public void modSamplerConstructorCheckTest() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ModSampler<>(0, identityFunction));
    }

    @Test
    public void percentageSamplerTest() {
        long samplingRate = 500; // 5%

        Sampler<Long> sampler = new PercentRateSampler<>(samplingRate, identityFunction);
        for (long i = 0L; i < 10L; i++) {
            long target = i * PercentRateSampler.MULTIPLIER;
            assertThat(sampler.isSampling(target)).isEqualTo((target % PercentRateSampler.MAX) < samplingRate);
        }
    }

    @Test
    public void percentageSamplerConstructorCheckTest() {
        assertThatIllegalArgumentException().isThrownBy(() -> new PercentRateSampler<>("-1", identityFunction));
        assertThatIllegalArgumentException().isThrownBy(() -> new PercentRateSampler<>("101", identityFunction));
    }
}
