package com.navercorp.pinpoint.collector.util;

/**
 * @author emeroad
 */
public interface AcceptedTimeService {

    void accept();

    void accept(long time);

    long getAcceptedTime();
}