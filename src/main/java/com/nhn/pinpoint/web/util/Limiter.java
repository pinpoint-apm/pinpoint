package com.nhn.pinpoint.web.util;

/**
 * @author emeroad
 */
public interface Limiter {
    void limit(long from, long to);
}
