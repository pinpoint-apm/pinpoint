package com.nhn.pinpoint.profiler.interceptor.util;

import com.nhn.pinpoint.profiler.util.DepthScope;

/**
 *
 */
public class BindValueScope {
    private static final DepthScope SCOPE = new DepthScope("DepthScope");

    public static int push() {
        return SCOPE.push();
    }

    public static int depth() {
        return SCOPE.depth();
    }

    public static int pop() {
        return SCOPE.pop();
    }
}
