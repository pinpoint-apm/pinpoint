package com.nhn.pinpoint.profiler.sampler;

import com.nhn.pinpoint.bootstrap.sampler.Sampler;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class SamplerFactoryTest {
    @Test
    public void createSamplerSamplingRate0() {
        SamplerFactory samplerFactory = new SamplerFactory();
        Sampler sampler = samplerFactory.createSampler(true, 0);
        boolean sampling = sampler.isSampling();
        Assert.assertFalse(sampling);
    }

    @Test
    public void createSamplerSamplingRate_Negative() {
        SamplerFactory samplerFactory = new SamplerFactory();
        Sampler sampler = samplerFactory.createSampler(true, -1);
        boolean sampling = sampler.isSampling();
        Assert.assertFalse(sampling);
    }
}
