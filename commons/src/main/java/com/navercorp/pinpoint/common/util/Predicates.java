package com.navercorp.pinpoint.common.util;

import java.util.function.Predicate;


public final class Predicates {
    private Predicates() {
    }

    @SuppressWarnings("rawtypes")
    private static final Predicate IS_TRUE = new Predicate() {
        @Override
        public boolean test(Object o) {
            return true;
        }

        @Override
        public String toString() {
            return "Predicates.isTrue()";
        }
    };

    @SuppressWarnings("rawtypes")
    private static final Predicate IS_FALSE = new Predicate() {
        @Override
        public boolean test(Object o) {
            return false;
        }

        @Override
        public String toString() {
            return "Predicates.isFalse()";
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> isTrue() {
        return (Predicate<T>) IS_TRUE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> isFalse() {
        return IS_FALSE;
    }

}
