package com.navercorp.pinpoint.bootstrap.interceptor.registry;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author emeroad
 */
public class DefaultLocker implements Locker {

    private final AtomicReference<Object> lockRef = new AtomicReference<Object>();

    public final boolean lock(Object lock) {
        if (lock == null) {
            return lockRef.compareAndSet(null, null);
        } else {
            return lockRef.compareAndSet(null, lock);
        }
    }

    public final boolean unlock(Object lock) {
        final Object lockExist = lockRef.get();
        if (lockExist == null) {
            return lockRef.compareAndSet(null, null);
        } else {
            if (lockExist == lock) {
                return lockRef.compareAndSet(lockExist, null);
            } else {
                return false;
            }

        }
    }

    @Override
    public Object getLock() {
        return lockRef.get();
    }
}
