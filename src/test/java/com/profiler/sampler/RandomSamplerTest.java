package com.profiler.sampler;


import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class RandomSamplerTest {
    @Test
    public void test() {
        RandomSampler randomSampler = new RandomSampler(10);
        assertChoice(randomSampler, 10);
        assertChoice(randomSampler, 5);
        assertChoice(randomSampler, 1);

        assertDrop(randomSampler, 0);
        assertDrop(randomSampler, 11);
        assertDrop(randomSampler, 210);
    }

    @Test
    public void mod() {
        int i = 0 % 101;
        System.out.println("" + i);

        int j = Math.abs(-102) % 101;
        System.out.println("" + j);
    }

    private void assertDrop(RandomSampler randomSampler, int seed) {
        boolean sample = randomSampler.sample(seed);
        Assert.assertFalse(sample);
    }

    private void assertChoice(RandomSampler randomSampler, int seed) {
        boolean sample = randomSampler.sample(seed);
        Assert.assertTrue(sample);
    }
}
