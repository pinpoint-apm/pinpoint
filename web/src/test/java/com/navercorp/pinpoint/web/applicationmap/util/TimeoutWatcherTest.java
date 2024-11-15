package com.navercorp.pinpoint.web.applicationmap.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TimeoutWatcherTest {

    @Test
    void remainingTimeMillis() {
        TimeoutWatcher timeoutWatcher = new TimeoutWatcher(1000);
        long remainingTimeMillis = timeoutWatcher.remainingTimeMillis();
        Assertions.assertThat(remainingTimeMillis).isGreaterThan(0);
    }

    @Test
    void remainingTimeMillis_timeout() {
        TimeoutWatcher timeoutWatcher = new TimeoutWatcher(1000) {
            int count;
            @Override
            long currentTimeMillis() {
                if (0 == count++) {
                    return System.currentTimeMillis();
                }
                return System.currentTimeMillis() + 5000;
            }
        };
        long remainingTimeMillis = timeoutWatcher.remainingTimeMillis();
        org.junit.jupiter.api.Assertions.assertEquals(0, remainingTimeMillis);
    }

    @Test
    void remainingTimeMillis_infinity() {
        TimeoutWatcher timeoutWatcher = new TimeoutWatcher(-1);

        long remainingTimeMillis = timeoutWatcher.remainingTimeMillis();

        Assertions.assertThat(remainingTimeMillis).isLessThanOrEqualTo(TimeoutWatcher.INFINITY_TIME);
    }
}