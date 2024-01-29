package com.navercorp.pinpoint.collector.sampler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

public class SamplerTest {

    Function<Long, Long> identityFunction;

    @BeforeEach
    public void beforeEach() {
        this.identityFunction = (num -> num);
    }

    @Test
    public void falseSamplerTest() {
        Sampler<Long> sampler = FalseSampler.INSTANCE;
        Sampler<Long> percentageSampler = new PercentRateSampler<>("0", identityFunction);

        for (long i = 0L; i < 10L; i++) {
            Assertions.assertFalse(sampler.isSampling(i));
            Assertions.assertFalse(percentageSampler.isSampling(i));
        }
    }

    @Test
    public void trueSamplerTest() {
        Sampler<Long> sampler = TrueSampler.INSTANCE;
        Sampler<Long> modSampler = new ModSampler<>(1, identityFunction);
        Sampler<Long> percentageSampler = new PercentRateSampler<>("100", identityFunction);

        for (long i = 0L; i < 10L; i++) {
            Assertions.assertTrue(sampler.isSampling(i));
            Assertions.assertTrue(modSampler.isSampling(i));
            Assertions.assertTrue(percentageSampler.isSampling(i));
        }
    }

    @Test
    public void modSamplerTest() {
        long samplingRate = 5;

        Sampler<Long> sampler = new ModSampler<>(samplingRate, identityFunction);
        for (long i = 0L; i < 10L; i++) {
            Assertions.assertEquals((i % samplingRate) == 0, (sampler.isSampling(i)));
        }
    }

    @Test
    public void modSamplerFunctionTest() {
        long samplingRate = 5;

        Sampler<Long> sampler = new ModSampler<>(samplingRate, identityFunction);
        for (long i = 0L; i < 10L; i++) {
            Assertions.assertEquals((i % samplingRate) == 0, (sampler.isSampling(i)));
        }
    }

    @Test
    public void modSamplerConstructorCheckTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ModSampler<>(0, identityFunction));
    }

    @Test
    public void percentageSamplerTest() {
        long samplingRate = 500; // 5%

        Sampler<Long> sampler = new PercentRateSampler<>(samplingRate, identityFunction);
        for (long i = 0L; i < 10L; i++) {
            long target = i * PercentRateSampler.MULTIPLIER;
            Assertions.assertEquals((target % PercentRateSampler.MAX) < samplingRate, (sampler.isSampling(target)));
        }
    }

    @Test
    public void percentageSamplerConstructorCheckTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PercentRateSampler<>("-1", identityFunction));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PercentRateSampler<>("101", identityFunction));
    }
}
