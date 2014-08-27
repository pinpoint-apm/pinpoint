package com.nhn.pinpoint.profiler.util;

/**
 * @author emeroad
 */
public interface Scope  {
    int push();

    int depth();

    int pop() ;

    String getName();

}
