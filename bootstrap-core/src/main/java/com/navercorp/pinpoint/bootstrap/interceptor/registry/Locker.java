package com.navercorp.pinpoint.bootstrap.interceptor;


/**
 * @author emeroad
 */
public interface Locker {

    boolean lock(Object lock);

    boolean unlock(Object lock);
}
