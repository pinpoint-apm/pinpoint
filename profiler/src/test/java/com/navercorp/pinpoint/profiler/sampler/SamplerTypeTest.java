package com.navercorp.pinpoint.profiler.sampler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SamplerTypeTest {

    @Test
    public void of() {
        Assertions.assertEquals(SamplerType.COUNTING, SamplerType.of("counting"));
        Assertions.assertEquals(SamplerType.PERCENT, SamplerType.of("percent"));
    }
}