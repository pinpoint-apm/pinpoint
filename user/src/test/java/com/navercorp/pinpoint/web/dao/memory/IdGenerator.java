package com.navercorp.pinpoint.web.dao.memory;

import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
    private final AtomicLong idGenerator = new AtomicLong();

    public String getId() {
        return String.valueOf(this.idGenerator.getAndIncrement());
    }

    public void reset(long id) {
        this.idGenerator.set(id);
    }
}
