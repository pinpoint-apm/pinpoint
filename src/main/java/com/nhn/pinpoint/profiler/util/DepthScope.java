package com.nhn.pinpoint.profiler.util;

/**
 * @author emeroad
 */
public final class DepthScope {

    public static final int ZERO = 0;

    private final NamedThreadLocal<Depth> scope;


    public DepthScope(final String scopeName) {
        this.scope = new NamedThreadLocal<Depth>(scopeName) {
            @Override
            protected Depth initialValue() {
                return new Depth();
            }
        };
    }

    public int push() {
        final Depth depth = scope.get();
        return depth.push();
    }

    public int depth() {
        final Depth depth = scope.get();
        return depth.depth();
    }

    public int pop() {
        final Depth depth = scope.get();
        return depth.pop();
    }

    private static final class Depth {
        private int depth = 0;

        public int push() {
            return depth++;
        }

        public int pop() {
            return --depth;
        }

        public int depth() {
            return depth;
        }

    }

    public String getName() {
        return scope.getName();
    }
}