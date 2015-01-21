package com.navercorp.pinpoint.bootstrap.interceptor;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author emeroad
 */
public class AtomicMaxUpdater {

    private static final AtomicIntegerFieldUpdater<AtomicMaxUpdater> UPDATER = AtomicIntegerFieldUpdater.newUpdater(AtomicMaxUpdater.class, "maxIndex");

    private volatile int maxIndex = 0;

    public boolean update(int max) {
        while (true) {
            final int currentMax = getIndex();
            if (currentMax >= max) {
                return false;
            }
            final boolean update = UPDATER.compareAndSet(this, currentMax, max);
            if (update) {
                return true;
            }
        }
    }


    public int getIndex() {
        return UPDATER.get(this);
    }
}
