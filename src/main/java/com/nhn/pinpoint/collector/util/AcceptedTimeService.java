package com.nhn.pinpoint.collector.util;

/**
 * @author emeroad
 */
public interface AcceptedTimeService {

    public void accept();

    public void accept(long time);

    public long getAcceptedTime();
}