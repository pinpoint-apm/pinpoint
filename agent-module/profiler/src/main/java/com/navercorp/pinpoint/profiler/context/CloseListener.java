package com.navercorp.pinpoint.profiler.context;

public interface CloseListener {
    void close(long endTime);

    CloseListener EMPTY = new CloseListener() {
        @Override
        public void close(long endTime) {
        }
    };

}
