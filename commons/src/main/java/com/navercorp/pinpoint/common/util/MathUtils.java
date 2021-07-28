/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;

/**
 * @author emeroad
 */
public final class MathUtils {
    private MathUtils() {
    }

    public static int fastAbs(final int value) {
        return value & Integer.MAX_VALUE;
    }

    public static long roundToNearestMultipleOf(final long num, final long multipleOf) {
        if (num < 0) {
            throw new IllegalArgumentException("num cannot be negative");
        }
        if (multipleOf < 1) {
            throw new IllegalArgumentException("cannot round to nearest multiple of values less than 1");
        }
        if (num < multipleOf) {
            return multipleOf;
        }
        if ((num % multipleOf) >= (multipleOf / 2.0)) {
            return (num + multipleOf) - (num % multipleOf);
        } else {
            return num - (num % multipleOf);
        }
    }


    // copy Apache commons-math 3.6.1 FastMath.floorMod(long, long)
    /** Finds q such that a = q b + r with 0 <= r < b if b > 0 and b < r <= 0 if b < 0.
     * <p>
     * This methods returns the same value as integer division when
     * a and b are same signs, but returns a different value when
     * they are opposite (i.e. q is negative).
     * </p>
     * @param a dividend
     * @param b divisor
     * @return q such that a = q b + r with 0 <= r < b if b > 0 and b < r <= 0 if b < 0
     * @exception IllegalArgumentException if b == 0
     * @see #floorMod(long, long)
     * @since 3.4
     */
    public static long floorMod(final long a, final long b) {

        if (b == 0l) {
            throw new IllegalArgumentException("denominator must be different from 0");
        }

        final long m = a % b;
        if ((a ^ b) >= 0l || m == 0l) {
            // a an b have same sign, or division is exact
            return m;
        } else {
            // a and b have opposite signs and division is not exact
            return b + m;
        }

    }

    // copy Apache commons-math 3.6.1 FastMath.floorMod(int, int)
    /** Finds r such that a = q b + r with 0 <= r < b if b > 0 and b < r <= 0 if b < 0.
     * <p>
     * This methods returns the same value as integer modulo when
     * a and b are same signs, but returns a different value when
     * they are opposite (i.e. q is negative).
     * </p>
     * @param a dividend
     * @param b divisor
     * @return r such that a = q b + r with 0 <= r < b if b > 0 and b < r <= 0 if b < 0
     * @exception IllegalArgumentException if b == 0
     * @since 3.4
     */
    public static int floorMod(final int a, final int b) {

        if (b == 0) {
            throw new IllegalArgumentException("denominator must be different from 0");
        }

        final int m = a % b;
        if ((a ^ b) >= 0 || m == 0) {
            // a an b have same sign, or division is exact
            return m;
        } else {
            // a and b have opposite signs and division is not exact
            return b + m;
        }

    }
}
