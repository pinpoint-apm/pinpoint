package com.nhn.pinpoint.collector.util;

/**
 * @author emeroad
 */
public interface AcceptedTimeService {

    void accept();

    void accept(long time);

    long getAcceptedTime();
}