package com.navercorp.pinpoint.profiler.util;


import com.navercorp.pinpoint.common.util.Assert;

import java.util.Random;

public final class RandomExUtils {
    private static final Random RANDOM = new Random();

    private RandomExUtils() {
    }

    public static int nextInt(int startInclusive, int endExclusive) {
        Assert.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Assert.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        return startInclusive == endExclusive ? startInclusive : startInclusive + RANDOM.nextInt(endExclusive - startInclusive);
    }
}
