package com.nhn.pinpoint.profiler.util;

/**
 *
 */
public class Scope {

    private final NamedThreadLocal<Marker> scope;

    public Scope(final String scopeName) {
        this.scope = new NamedThreadLocal<Marker>(scopeName) {
            @Override
            protected Marker initialValue() {
                return new Marker();
            }
        };
    }

    public void push() {
        Marker marker = scope.get();
        marker.mark();
    }

    public boolean isInternal() {
        Marker marker = scope.get();
        return marker.isMark();
    }

    public void pop() {
        Marker marker = scope.get();
        marker.unMark();
    }

    private static class Marker {
        private boolean mark;

        public boolean isMark() {
            return mark;
        }

        public void mark() {
            this.mark = true;
        }

        public void unMark() {
            this.mark = false;
        }
    }
}
