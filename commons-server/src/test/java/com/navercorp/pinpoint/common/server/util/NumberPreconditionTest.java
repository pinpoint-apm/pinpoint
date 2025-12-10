/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NumberPreconditionTest {
    @Test
    public void requirePositive_returnsNumber_whenPositive() {
        int result = NumberPrecondition.requirePositive(5, "Number must be positive");
        assertEquals(5, result);
    }

    @Test
    public void requirePositive_throwsException_whenZero() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositive(0, "Number must be positive")
        );
    }

    @Test
    public void requirePositive_throwsException_whenNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositive(-1, "Number must be positive")
        );
    }

    @Test
    public void requirePositiveOrZero_returnsNumber_whenPositive() {
        int result = NumberPrecondition.requirePositiveOrZero(5, "Number must be positive or zero");
        assertEquals(5, result);
    }

    @Test
    public void requirePositiveOrZero_returnsZero_whenZero() {
        int result = NumberPrecondition.requirePositiveOrZero(0, "Number must be positive or zero");
        assertEquals(0, result);
    }

    @Test
    public void requirePositiveOrZero_throwsException_whenNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositiveOrZero(-1, "Number must be positive or zero")
        );
    }

    @Test
    public void requireNegative_returnsNumber_whenNegative() {
        int result = NumberPrecondition.requireNegative(-5, "Number must be negative");
        assertEquals(-5, result);
    }

    @Test
    public void requireNegative_throwsException_whenZero() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requireNegative(0, "Number must be negative")
        );
    }

    @Test
    public void requireNegative_throwsException_whenPositive() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requireNegative(1, "Number must be negative")
        );
    }

    @Test
    public void requireNegativeOrZero_returnsNumber_whenNegative() {
        int result = NumberPrecondition.requireNegativeOrZero(-5, "Number must be negative or zero");
        assertEquals(-5, result);
    }

    @Test
    public void requireNegativeOrZero_returnsZero_whenZero() {
        int result = NumberPrecondition.requireNegativeOrZero(0, "Number must be negative or zero");
        assertEquals(0, result);
    }

    @Test
    public void requireNegativeOrZero_throwsException_whenPositive() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requireNegativeOrZero(1, "Number must be negative or zero")
        );
    }

    @Test
    public void requirePositive_long_returnsNumber_whenPositive() {
        long result = NumberPrecondition.requirePositive(5L, "Number must be positive");
        assertEquals(5L, result);
    }

    @Test
    public void requirePositive_long_throwsException_whenZero() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositive(0L, "Number must be positive")
        );
    }

    @Test
    public void requirePositive_long_throwsException_whenNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositive(-1L, "Number must be positive")
        );
    }

    @Test
    public void requirePositiveOrZero_long_returnsNumber_whenPositive() {
        long result = NumberPrecondition.requirePositiveOrZero(5L, "Number must be positive or zero");
        assertEquals(5L, result);
    }

    @Test
    public void requirePositiveOrZero_long_returnsZero_whenZero() {
        long result = NumberPrecondition.requirePositiveOrZero(0L, "Number must be positive or zero");
        assertEquals(0L, result);
    }

    @Test
    public void requirePositiveOrZero_long_throwsException_whenNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositiveOrZero(-1L, "Number must be positive or zero")
        );
    }

    @Test
    public void requireNegative_long_returnsNumber_whenNegative() {
        long result = NumberPrecondition.requireNegative(-5L, "Number must be negative");
        assertEquals(-5L, result);
    }

    @Test
    public void requireNegative_long_throwsException_whenZero() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requireNegative(0L, "Number must be negative")
        );
    }

    @Test
    public void requireNegative_long_throwsException_whenPositive() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requireNegative(1L, "Number must be negative")
        );
    }

    @Test
    public void requireNegativeOrZero_long_returnsNumber_whenNegative() {
        long result = NumberPrecondition.requireNegativeOrZero(-5L, "Number must be negative or zero");
        assertEquals(-5L, result);
    }

    @Test
    public void requireNegativeOrZero_long_returnsZero_whenZero() {
        long result = NumberPrecondition.requireNegativeOrZero(0L, "Number must be negative or zero");
        assertEquals(0L, result);
    }

    @Test
    public void requireNegativeOrZero_long_throwsException_whenPositive() {
        assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requireNegativeOrZero(1L, "Number must be negative or zero")
        );
    }

    // Tests for Integer type methods

    @Test
    public void requirePositive_Integer_returnsNumber_whenPositive() {
        Integer result = NumberPrecondition.requirePositive(5, "value");
        assertEquals(5, result);
    }

    @Test
    public void requirePositive_Integer_throwsException_whenNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositive((Integer) null, "value")
        );
        assertEquals("value must not be null", exception.getMessage());
    }

    @Test
    public void requirePositive_Integer_throwsException_whenZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositive(Integer.valueOf(0), "value")
        );
        assertEquals("value must be positive", exception.getMessage());
    }

    @Test
    public void requirePositive_Integer_throwsException_whenNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositive(Integer.valueOf(-1), "value")
        );
        assertEquals("value must be positive", exception.getMessage());
    }

    @Test
    public void requirePositiveOrZero_Integer_returnsNumber_whenPositive() {
        Integer result = NumberPrecondition.requirePositiveOrZero(5, "value");
        assertEquals(5, result);
    }

    @Test
    public void requirePositiveOrZero_Integer_returnsZero_whenZero() {
        Integer result = NumberPrecondition.requirePositiveOrZero(0, "value");
        assertEquals(0, result);
    }

    @Test
    public void requirePositiveOrZero_Integer_throwsException_whenNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositiveOrZero((Integer) null, "value")
        );
        assertEquals("value must not be null", exception.getMessage());
    }

    @Test
    public void requirePositiveOrZero_Integer_throwsException_whenNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositiveOrZero(Integer.valueOf(-1), "value")
        );
        assertEquals("value must be non-negative", exception.getMessage());
    }

    // Tests for Long type methods

    @Test
    public void requirePositive_Long_returnsNumber_whenPositive() {
        Long result = NumberPrecondition.requirePositive(5L, "value");
        assertEquals(5L, result);
    }

    @Test
    public void requirePositive_Long_throwsException_whenNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositive((Long) null, "value")
        );
        assertEquals("value must not be null", exception.getMessage());
    }

    @Test
    public void requirePositive_Long_throwsException_whenZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositive(Long.valueOf(0), "value")
        );
        assertEquals("value must be positive", exception.getMessage());
    }

    @Test
    public void requirePositive_Long_throwsException_whenNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositive(Long.valueOf(-1), "value")
        );
        assertEquals("value must be positive", exception.getMessage());
    }

    @Test
    public void requirePositiveOrZero_Long_returnsNumber_whenPositive() {
        Long result = NumberPrecondition.requirePositiveOrZero(5L, "value");
        assertEquals(5L, result);
    }

    @Test
    public void requirePositiveOrZero_Long_returnsZero_whenZero() {
        Long result = NumberPrecondition.requirePositiveOrZero(0L, "value");
        assertEquals(0L, result);
    }

    @Test
    public void requirePositiveOrZero_Long_throwsException_whenNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositiveOrZero((Long) null, "value")
        );
        assertEquals("value must not be null", exception.getMessage());
    }

    @Test
    public void requirePositiveOrZero_Long_throwsException_whenNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                NumberPrecondition.requirePositiveOrZero(Long.valueOf(-1), "value")
        );
        assertEquals("value must be non-negative", exception.getMessage());
    }
}