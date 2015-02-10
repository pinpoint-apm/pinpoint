package com.navercorp.pinpoint.bootstrap.interceptor;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author emeroad
 */
public interface Locker {

    boolean lock(Object lock);

    boolean unlock(Object lock);
}
