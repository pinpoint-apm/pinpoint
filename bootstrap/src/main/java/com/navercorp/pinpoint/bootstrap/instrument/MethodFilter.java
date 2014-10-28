package com.nhn.pinpoint.bootstrap.instrument;


/**
 * @author emeroad
 */
public interface MethodFilter {
    boolean filter(Method method);

}
