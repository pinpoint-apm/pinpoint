package com.navercorp.pinpoint.profiler.context.storage;

import java.util.Objects;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Snapshot<T> {

    private T currentImage;
    private final LongFunction<T> instanceFactory;
    private final ToLongFunction<T> currentImageTimestamp;

    public Snapshot(LongFunction<T> instanceFactory, ToLongFunction<T> currentImageTimestamp) {
        this.instanceFactory = Objects.requireNonNull(instanceFactory, "instanceFactory");
        this.currentImageTimestamp = Objects.requireNonNull(currentImageTimestamp, "currentImageTimestamp");
    }

    public T takeSnapshot(long currentBaseTimestamp) {
        if (currentImage == null) {
            return null;
        }

        if (currentBaseTimestamp > currentImageTimestamp.applyAsLong(currentImage)) {
            T snapshot = this.currentImage;
            this.currentImage = null;
            return snapshot;
        }
        return null;
    }

    public T getCurrent(long currentBaseTimestamp) {
        if (currentImage == null) {
            currentImage = instanceFactory.apply(currentBaseTimestamp);
        }
        return currentImage;
    }
}
