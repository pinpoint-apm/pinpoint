package com.navercorp.pinpoint.common.util;

import java.util.function.Predicate;


public final class Predicates {
    private Predicates() {
    }

    @SuppressWarnings("rawtypes")
    private static final Predicate IS_TRUE = new TruePredicate();

    @SuppressWarnings("rawtypes")
    private static final Predicate IS_FALSE = new FalsePredicate();

    @SuppressWarnings("rawtypes")
    private static class TruePredicate implements Predicate {
        @Override
        public boolean test(Object o) {
            return true;
        }

        @Override
        public String toString() {
            return "TruePredicate";
        }
    };

    @SuppressWarnings("rawtypes")
    private static class FalsePredicate implements Predicate {
        @Override
        public boolean test(Object o) {
            return false;
        }

        @Override
        public String toString() {
            return "FalsePredicate";
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> isTrue() {
        return IS_TRUE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> isFalse() {
        return IS_FALSE;
    }

}
