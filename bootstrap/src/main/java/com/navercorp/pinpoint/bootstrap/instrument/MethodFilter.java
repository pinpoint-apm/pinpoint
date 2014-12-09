package com.navercorp.pinpoint.bootstrap.instrument;


/**
 * @author emeroad
 */
public interface MethodFilter {
    boolean filter(MethodInfo method);

}
