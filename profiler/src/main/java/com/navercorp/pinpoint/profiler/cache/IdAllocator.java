package com.navercorp.pinpoint.profiler.cache;

import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.concurrent.atomic.AtomicInteger;

public interface IdAllocator {
    int allocate();

    enum ID_TYPE {
        BYPASS, ZIGZAG;
    }

    class ZigZagAllocator implements IdAllocator {
        private final AtomicInteger idGen;

        public ZigZagAllocator() {
            this(1);
        }

        public ZigZagAllocator(int startValue) {
            this.idGen = new AtomicInteger(startValue);
        }

        @Override
        public int allocate() {
            int id = this.idGen.getAndIncrement();
            return BytesUtils.zigzagToInt(id);
        }
    }

    class BypassAllocator implements IdAllocator {
        private final AtomicInteger idGen;

        public BypassAllocator() {
            this(1);
        }

        public BypassAllocator(int startValue) {
            this.idGen = new AtomicInteger(startValue);
        }

        @Override
        public int allocate() {
            return this.idGen.getAndIncrement();
        }
    }

}
