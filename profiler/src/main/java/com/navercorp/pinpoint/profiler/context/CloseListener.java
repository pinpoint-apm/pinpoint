package com.navercorp.pinpoint.profiler.context;

public interface CloseListener {
    void close(Span span);

    CloseListener EMPTY = new CloseListener() {
        @Override
        public void close(Span span) {
        }
    };

}
