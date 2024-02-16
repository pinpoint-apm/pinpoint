package com.navercorp.pinpoint.profiler.sender.grpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class ExponentialBackoffReconnectJobTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    void backoff() {
        ExponentialBackoffReconnectJob job = new ExponentialBackoffReconnectJob(
                () -> logger.info("run()"), 1000, 3000);

        for (int i = 0; i < 10; i++) {
            logger.debug("{}", job.nextInterval());
        }

        Assertions.assertEquals(3000, job.nextInterval());

    }
}