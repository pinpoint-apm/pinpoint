package com.nhn.pinpoint.profiler.util;

/**
 * @author emeroad
 */
public final class DepthScope implements Scope {

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

    @Override
    public int push() {
        final Depth depth = scope.get();
        return depth.push();
    }

    @Override
    public int depth() {
        final Depth depth = scope.get();
        return depth.depth();
    }

    @Override
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

    @Override
    public String getName() {
        return scope.getName();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DepthScope{");
        sb.append("scope=").append(scope.getName());
        sb.append('}');
        return sb.toString();
    }
}