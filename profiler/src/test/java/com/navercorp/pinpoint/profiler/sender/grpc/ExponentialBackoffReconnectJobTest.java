package com.navercorp.pinpoint.profiler.sender.grpc;

import io.github.resilience4j.core.IntervalFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ExponentialBackoffReconnectJobTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    void exponentialBackoff() {
        ExponentialBackoffReconnectJob job = new ExponentialBackoffReconnectJob(
                () -> logger.info("run()"), 1000, 3000);

        for (int i = 0; i < 10; i++) {
            logger.debug("{}", job.nextInterval());
        }

        Assertions.assertEquals(3000, job.nextInterval());
    }


    @Test
    void random() {
        IntervalFunction interval = IntervalFunction.ofRandomized(3000, 0.1);
        for (int i = 0; i < 10; i++) {
            logger.debug("{}", interval.apply(i +1 ));
        }

    }


    @Test
    void default_policy() {
        IntervalFunction interval = IntervalFunction.of(3000);
        for (int i = 0; i < 10; i++) {
            logger.debug("{}", interval.apply(i +1 ));
        }

    }
}