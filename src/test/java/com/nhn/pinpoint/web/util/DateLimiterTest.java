package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.web.vo.Range;
import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class DateLimiterTest {

    @Test
    public void check() {
        Limiter limiter = new DateLimiter(2);

        limiter.limit(0, TimeUnit.DAYS.toMillis(2));

        long time = 1000;
        limiter.limit(time, time + TimeUnit.DAYS.toMillis(2));

        limiter.limit(TimeUnit.DAYS.toMillis(2), TimeUnit.DAYS.toMillis(2));
    }

    @Test
    public void checkRange() {
        Limiter limiter = new DateLimiter(2);

        limiter.limit(new Range(0, TimeUnit.DAYS.toMillis(2)));

        long time = 1000;
        limiter.limit(new Range(time, time + TimeUnit.DAYS.toMillis(2)));

        limiter.limit(new Range(TimeUnit.DAYS.toMillis(2), TimeUnit.DAYS.toMillis(2)));
    }

    @Test
    public void checkFail() {
        Limiter limiter = new DateLimiter(2);
        try {
            limiter.limit(0, TimeUnit.DAYS.toMillis(2) + 1);
            Assert.fail();
        } catch (Exception e) {
        }

        try {
            limiter.limit(TimeUnit.DAYS.toMillis(2), 0);
            Assert.fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void checkRangeFail() {
        Limiter limiter = new DateLimiter(2);
        try {
            limiter.limit(new Range(0, TimeUnit.DAYS.toMillis(2) + 1));
            Assert.fail();
        } catch (Exception e) {
        }

        try {
            limiter.limit(new Range(TimeUnit.DAYS.toMillis(2), 0));
            Assert.fail();
        } catch (Exception e) {
        }
    }

}
