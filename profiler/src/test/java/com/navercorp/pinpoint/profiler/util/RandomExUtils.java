package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.common.util.Assert;
import org.apache.commons.lang.math.RandomUtils;

public final class RandomExUtils {
    private RandomExUtils() {
    }

    public static int nextInt(int startInclusive, int endExclusive) {
        Assert.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Assert.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        return startInclusive == endExclusive ? startInclusive : startInclusive + RandomUtils.nextInt(endExclusive - startInclusive);
    }
}
