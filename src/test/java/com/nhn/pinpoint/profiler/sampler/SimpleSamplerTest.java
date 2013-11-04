package com.nhn.pinpoint.profiler.sampler;


import com.nhn.pinpoint.profiler.sampler.SamplingRateSampler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class SimpleSamplerTest {
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
        System.out.println("" + i);

        int j = Math.abs(-102) % 101;
        System.out.println("" + j);
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
