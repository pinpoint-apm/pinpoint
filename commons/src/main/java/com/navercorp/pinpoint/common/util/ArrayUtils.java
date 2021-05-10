/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.util;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class ArrayUtils {

    private static final int DEFAULT_ABBREVIATE_MAX_WIDTH = 32;
    private static char DELIMITER = ',';

    private ArrayUtils() {
    }

    public static String abbreviate(final byte[] bytes) {
        return abbreviate(bytes, DEFAULT_ABBREVIATE_MAX_WIDTH);
    }

    public static String abbreviate(final byte[] bytes, final int maxWidth) {
        if (bytes == null) {
            return "null";
        }
        if (maxWidth < 0) {
            throw new IllegalArgumentException("negative maxWidth");
        }
        if (bytes.length == 0) {
            return "[]";
        }
        if (maxWidth == 0) {
            return "[...(" + bytes.length + ")]";
        }

        final int bytesMaxLength = Math.min(bytes.length, maxWidth);

        final StringBuilder sb = new StringBuilder(abbreviateBufferSize(bytes, maxWidth));
        sb.append('[');

        final int lastIndex = bytesMaxLength - 1;
        for (int i = 0; i < bytesMaxLength; i++) {
            sb.append(bytes[i]);
            if (i < lastIndex) {
                sb.append(DELIMITER);
            }
        }

        if (bytes.length <= maxWidth) {
            sb.append(']');
        } else {
            sb.append(DELIMITER);
            sb.append("...(");
            sb.append(bytes.length);
            sb.append(")]");
        }
        return sb.toString();
    }


    static int stringLength(byte[] bytes, int toOffset) {
        int length = 0;
        for (int i = 0; i < toOffset; i++) {
            byte b = bytes[i];
            length += StringUtils.stringLength(b);
        }
        return length;
    }

    static int abbreviateBufferSize(byte[] bytes, int maxWidth) {
        final int min = Math.min(bytes.length, maxWidth);
        final int delimiter = Math.max(min - 1, 0);
        final int defaultBufferSize = stringLength(bytes, min) + delimiter;
        if (bytes.length <= maxWidth) {
            // "[]";
            return defaultBufferSize + "[]".length();
        } else {
            final int size = defaultBufferSize + StringUtils.stringLength(bytes.length);
            if (defaultBufferSize == 0) {
                // "[...()]";
                return size + "[...()]".length();
            } else {
                // "[,...()]";
                return size + "[,...()]".length();
            }
        }
    }

    // copy apache commons lang3 ----------------------------------------------------------------------

    /**
     * <p>Checks if an array of Objects is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 1.7.0
     */
    public static <T> boolean isEmpty(final T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <p>Checks if an array of primitive longs is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 1.7.0
     */
    public static boolean isEmpty(final long[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <p>Checks if an array of primitive ints is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 1.7.0
     */
    public static boolean isEmpty(final int[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <p>Checks if an array of primitive shorts is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 1.7.0
     */
    public static boolean isEmpty(final short[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <p>Checks if an array of primitive chars is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 1.7.0
     */
    public static boolean isEmpty(final char[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <p>Checks if an array of primitive bytes is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 1.7.0
     */
    public static boolean isEmpty(final byte[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <p>Checks if an array of primitive doubles is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 1.7.0
     */
    public static boolean isEmpty(final double[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <p>Checks if an array of primitive floats is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 1.7.0
     */
    public static boolean isEmpty(final float[] array) {
        return array == null || array.length == 0;
    }

    /**
     * <p>Checks if an array of primitive booleans is empty or {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is empty or {@code null}
     * @since 1.7.0
     */
    public static boolean isEmpty(final boolean[] array) {
        return array == null || array.length == 0;
    }


    // copy apache commons lang3 ----------------------------------------------------------------------

    /**
     * <p>Checks if an array of Objects is not empty and not {@code null}.
     *
     * @param <T>   the component type of the array
     * @param array the array to test
     * @return {@code true} if the array is not empty and not {@code null}
     * @since 1.7.0
     */
    public static <T> boolean hasLength(final T[] array) {
        return array != null && array.length != 0;
    }

    /**
     * <p>Checks if an array of primitive longs is not empty and not {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is not empty and not {@code null}
     * @since 1.7.0
     */
    public static boolean hasLength(final long[] array) {
        return array != null && array.length != 0;
    }

    /**
     * <p>Checks if an array of primitive ints is not empty and not {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is not empty and not {@code null}
     * @since 1.7.0
     */
    public static boolean hasLength(final int[] array) {
        return array != null && array.length != 0;
    }

    /**
     * <p>Checks if an array of primitive shorts is not empty and not {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is not empty and not {@code null}
     * @since 1.7.0
     */
    public static boolean hasLength(final short[] array) {
        return array != null && array.length != 0;
    }

    /**
     * <p>Checks if an array of primitive chars is not empty and not {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is not empty and not {@code null}
     * @since 1.7.0
     */
    public static boolean hasLength(final char[] array) {
        return array != null && array.length != 0;
    }

    /**
     * <p>Checks if an array of primitive bytes is not empty and not {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is not empty and not {@code null}
     * @since 1.7.0
     */
    public static boolean hasLength(final byte[] array) {
        return array != null && array.length != 0;
    }

    /**
     * <p>Checks if an array of primitive doubles is not empty and not {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is not empty and not {@code null}
     * @since 1.7.0
     */
    public static boolean hasLength(final double[] array) {
        return array != null && array.length != 0;
    }

    /**
     * <p>Checks if an array of primitive floats is not empty and not {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is not empty and not {@code null}
     * @since 1.7.0
     */
    public static boolean hasLength(final float[] array) {
        return array != null && array.length != 0;
    }

    /**
     * <p>Checks if an array of primitive booleans is not empty and not {@code null}.
     *
     * @param array the array to test
     * @return {@code true} if the array is not empty and not {@code null}
     * @since 1.7.0
     */
    public static boolean hasLength(final boolean[] array) {
        return array != null && array.length != 0;
    }

    //---------------------------------------------------------------------------


    // Enhancement type safety & Remove native call ---------------------------------------------------------------------------

    /**
     * <p>Returns the length of the specified array.
     * This method can deal with {@code Object} arrays and with primitive arrays.
     *
     * <p>If the input array is {@code null}, {@code 0} is returned.
     *
     * <pre>
     *
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     * @since 1.7.0
     */
    public static int getLength(final long[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /**
     * <p>Returns the length of the specified array.
     * This method can deal with {@code Object} arrays and with primitive arrays.
     *
     * <p>If the input array is {@code null}, {@code 0} is returned.
     *
     * <pre>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     * @since 1.7.0
     */
    public static int getLength(final int[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /**
     * <p>Returns the length of the specified array.
     * This method can deal with {@code Object} arrays and with primitive arrays.
     *
     * <p>If the input array is {@code null}, {@code 0} is returned.
     *
     * <pre>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     * @since 1.7.0
     */
    public static int getLength(final short[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /**
     * <p>Returns the length of the specified array.
     * This method can deal with {@code Object} arrays and with primitive arrays.
     *
     * <p>If the input array is {@code null}, {@code 0} is returned.
     *
     * <pre>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     * @since 1.7.0
     */
    public static int getLength(final char[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /**
     * <p>Returns the length of the specified array.
     * This method can deal with {@code Object} arrays and with primitive arrays.
     *
     * <p>If the input array is {@code null}, {@code 0} is returned.
     *
     * <pre>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     * @since 1.7.0
     */
    public static int getLength(final byte[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /**
     * <p>Returns the length of the specified array.
     * This method can deal with {@code Object} arrays and with primitive arrays.
     *
     * <p>If the input array is {@code null}, {@code 0} is returned.
     *
     * <pre>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     * @since 1.7.0
     */
    public static int getLength(final double[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /**
     * <p>Returns the length of the specified array.
     * This method can deal with {@code Object} arrays and with primitive arrays.
     *
     * <p>If the input array is {@code null}, {@code 0} is returned.
     *
     * <pre>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array  the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     * @since 1.7.0
     */
    public static int getLength(final float[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /**
     * <p>Returns the length of the specified array.
     * This method can deal with {@code Object} arrays and with primitive arrays.
     *
     * <p>If the input array is {@code null}, {@code 0} is returned.
     *
     * <pre>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     * @since 1.7.0
     */
    public static int getLength(final boolean[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /**
     * <p>Returns the length of the specified array.
     * This method can deal with {@code Object} arrays and with primitive arrays.
     *
     * <p>If the input array is {@code null}, {@code 0} is returned.
     *
     * <pre>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array  the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     * @since 1.7.0
     */
    public static <T> int getLength(final T[] array) {
        if (array == null) {
            return 0;
        }
        return array.length;
    }

    /**
     * Gets the nTh element of an array or null if the index is out of bounds or the array is null.
     *
     * @param <T> The type of array elements.
     * @param array The array to index.
     * @param index The index
     * @return the nTh element of an array or null if the index is out of bounds or the array is null.
     * @since 3.11
     */
    public static <T> T get(final T[] array, final int index) {
        return get(array, index, null);
    }

    /**
     * Gets the nTh element of an array or a default value if the index is out of bounds.
     *
     * @param <T> The type of array elements.
     * @param array The array to index.
     * @param index The index
     * @param defaultValue The return value of the given index is out of bounds.
     * @return the nTh element of an array or a default value if the index is out of bounds.
     * @since 3.11
     */
    public static <T> T get(final T[] array, final int index, final T defaultValue) {
        return isArrayIndexValid(array, index) ? array[index] : defaultValue;
    }

    /**
     * Returns whether a given array can safely be accessed at the given index.
     *
     * <pre>
     * ArrayUtils.isArrayIndexValid(null, 0)       = false
     * ArrayUtils.isArrayIndexValid([], 0)         = false
     * ArrayUtils.isArrayIndexValid(["a"], 0)      = true
     * </pre>
     *
     * @param <T> the component type of the array
     * @param array the array to inspect, may be null
     * @param index the index of the array to be inspected
     * @return Whether the given index is safely-accessible in the given array
     * @since 3.8
     */
    public static <T> boolean isArrayIndexValid(final T[] array, final int index) {
        return index >= 0 && getLength(array) > index;
    }
}
