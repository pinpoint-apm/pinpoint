package com.nhn.pinpoint.profiler.sampler;


import com.nhn.pinpoint.profiler.sampler.SamplingRateSampler;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class SimpleSamplerTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void test() {
        SamplingRateSampler simpleSampler = new SamplingRateSampler(1);
        assertChoice(simpleSampler);
        assertChoice(simpleSampler);
        assertChoice(simpleSampler);
        assertChoice(simpleSampler);

         SamplingRateSampler simpleSampler2 = new SamplingRateSampler(2);
        assertChoice(simpleSampler2);
        assertDrop(simpleSampler2);
        assertChoice(simpleSampler2);
        assertDrop(simpleSampler2);
    }

    @Test
    public void mod() {
        int i = 0 % 101;
        logger.debug("{}", i);

        int j = Math.abs(-102) % 101;
        logger.debug("{}", j);
    }

    private void assertDrop(SamplingRateSampler simpleSampler) {
        boolean sample = simpleSampler.isSampling();
        Assert.assertFalse(sample);
    }

    private void assertChoice(SamplingRateSampler simpleSampler) {
        boolean sample = simpleSampler.isSampling();
        Assert.assertTrue(sample);
    }
}
