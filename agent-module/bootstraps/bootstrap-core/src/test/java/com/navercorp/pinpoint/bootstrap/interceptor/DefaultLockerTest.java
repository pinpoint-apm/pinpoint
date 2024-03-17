package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.registry.DefaultLocker;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.Locker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultLockerTest {

    @Test
    public void testLock_Null0() {
        Locker locker = new DefaultLocker();
        Assertions.assertTrue(locker.lock(null));
        Assertions.assertTrue(locker.lock(null));
    }

    @Test
    public void testLock_Null1() {
        Locker locker = new DefaultLocker();

        Assertions.assertTrue(locker.lock(null));
        String one = "1";
        Assertions.assertTrue(locker.lock(one));
        Assertions.assertFalse(locker.lock("2"));

        Assertions.assertFalse(locker.lock(null));

        Assertions.assertFalse(locker.unlock(null));
        @SuppressWarnings("all")
        String s = new String("1");
        Assertions.assertFalse(locker.unlock(s));

        Assertions.assertTrue(locker.unlock(one));

    }

    @Test
    public void testUnlock() {

    }
}