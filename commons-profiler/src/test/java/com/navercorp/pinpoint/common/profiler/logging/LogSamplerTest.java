package com.navercorp.pinpoint.common.profiler.logging;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LogSamplerTest {
    LogSampler sampler = new LogSampler(2);
    @Test
    void log() {
        MutableInt counter = new MutableInt(0);

        sampler.log((c) -> counter.increment());
        Assertions.assertEquals(1, counter.getValue());

        sampler.log((c) -> counter.increment());
        Assertions.assertEquals(1, counter.getValue());

        sampler.log((c) -> counter.increment());
        Assertions.assertEquals(2, counter.getValue());
    }

    @Test
    void log_runnable() {
        MutableInt counter = new MutableInt(0);

        sampler.log(counter::increment);
        Assertions.assertEquals(1, counter.getValue());

        sampler.log(counter::increment);
        Assertions.assertEquals(1, counter.getValue());

        sampler.log(counter::increment);
        Assertions.assertEquals(2, counter.getValue());
    }
}