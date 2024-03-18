package com.navercorp.pinpoint.bootstrap.util.argument;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface Predicate {
    boolean test(Object[] args);

    int index();
}
