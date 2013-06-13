package com.nhn.pinpoint.profiler.interceptor.util;

import com.nhn.pinpoint.profiler.util.DepthScope;

/**
 *
 */
public class BindValueScope {
    private static DepthScope scope = new DepthScope("DepthScope");

    public static final Integer ZERO = DepthScope.ZERO;
    public static final Integer NULL = DepthScope.NULL;


    public static int push() {
        return scope.push();
    }

    public static int depth() {
        return scope.depth();
    }

    public static int pop() {
        return scope.pop();
    }
}
