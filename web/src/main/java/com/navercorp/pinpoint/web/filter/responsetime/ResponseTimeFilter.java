package com.navercorp.pinpoint.web.filter.responsetime;

/**
 * @author emeroad
 */
public interface ResponseTimeFilter {
    boolean ACCEPT = true;
    boolean REJECT = false;

    boolean accept(long elapsed);


}
