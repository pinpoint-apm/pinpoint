package com.nhn.pinpoint.profiler.interceptor.bci;

import java.util.HashMap;

/**
 * @author emeroad
 */
public class ClassHierarchyObject extends HashMap implements Runnable, Comparable {
    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public void run() {

    }
}
