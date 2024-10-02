package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;


class PredicatesTest {

    @Test
    void isTrue() {
        Predicate<String> predicate = Predicates.isTrue();
        Assertions.assertTrue(predicate.test("test"));

        Assertions.assertSame(predicate, Predicates.isTrue());
    }

    @Test
    void isFalse() {
        Predicate<String> predicate = Predicates.isFalse();
        Assertions.assertFalse(predicate.test("test"));

        Assertions.assertSame(predicate, Predicates.isFalse());
    }

}