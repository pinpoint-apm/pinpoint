package com.navercorp.pinpoint.metric.collector;

/**
 * @author Woonduk Kang(emeroad)
 */
public enum CollectorType {
    ALL,
    BASIC,
    METRIC;

    public boolean hasType(CollectorType type) {
        if (this == ALL) {
            return true;
        }
        if (this == type) {
            return true;
        }
        return false;
    }

}
