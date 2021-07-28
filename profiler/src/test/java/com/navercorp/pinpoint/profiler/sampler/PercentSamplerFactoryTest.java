package com.navercorp.pinpoint.profiler.sampler;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import org.junit.Assert;
import org.junit.Test;

public class PercentSamplerFactoryTest {

    @Test
    public void parseSamplingRate() {
        assertDoubleParse(100_00, "100");
        assertDoubleParse(50_00, "50");
        assertDoubleParse(2_00, "2");

        assertDoubleParse(10, "0.1");
        assertDoubleParse(3, "0.03");
        assertDoubleParse(1, "0.01");

        assertDoubleParse(99_99, "99.99");
        assertDoubleParse(33_33, "33.33");
        assertDoubleParse(3_03, "3.03");
    }

    private void assertDoubleParse(long expected, String strValue) {
        long v = PercentSamplerFactory.parseSamplingRate(strValue);
        Assert.assertEquals(expected, v);
    }

    @Test
    public void isSampling() {
        SamplerFactory factory = new PercentSamplerFactory(50_00);
        Sampler sampler = factory.createSampler();

        Assert.assertEquals(true, sampler.isSampling());
        Assert.assertEquals(false, sampler.isSampling());
        Assert.assertEquals(true, sampler.isSampling());
        Assert.assertEquals(false, sampler.isSampling());
    }

}