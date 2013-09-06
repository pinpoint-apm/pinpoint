package com.nhn.pinpoint.profiler.util;

/**
 *
 */
public class DepthScope {

    private final NamedThreadLocal<Integer> scope;

    // ZERO일 경우의 객체 생성을 줄이기 위해 상수화
    public static final Integer ZERO = 0;
    public static final Integer NULL = -1;

    public DepthScope(final String scopeName) {
        this.scope = new NamedThreadLocal<Integer>(scopeName) {
            @Override
            protected Integer initialValue() {
                return null;
            }
        };
    }

    public int push() {
        Integer depth = scope.get();
        if (depth == null) {
            scope.set(ZERO);
            return 0;
        } else {
            depth++;
            scope.set(depth);
            return depth;
        }
    }

    public int depth() {
        return scope.get();
    }

    public int pop() {
        Integer depth = scope.get();
        if (depth == null) {
            return NULL;
        } else if (ZERO.equals(depth)) {
            scope.set(null);
            return ZERO;
        } else {
            scope.set(depth - 1);
            return depth;
        }
    }
}