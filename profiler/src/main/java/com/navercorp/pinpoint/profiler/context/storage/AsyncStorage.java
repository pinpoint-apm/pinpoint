package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;

public class AsyncStorage implements Storage {

    private Storage storage;
    
    public AsyncStorage(final Storage storage) {
        this.storage = storage;
    }
    
    @Override
    public void store(SpanEvent spanEvent) {
        storage.store(spanEvent);
    }

    @Override
    public void store(Span span) {
        storage.flush();
    }

    @Override
    public void flush() {
        storage.flush();
    }
}
