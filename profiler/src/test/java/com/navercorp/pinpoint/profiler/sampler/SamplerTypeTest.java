package com.navercorp.pinpoint.profiler.sampler;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class SamplerTypeTest {

    @Test
    public void of() {
        Assert.assertEquals(SamplerType.COUNTING, SamplerType.of("counting"));
        Assert.assertEquals(SamplerType.PERCENT, SamplerType.of("percent"));
    }
}