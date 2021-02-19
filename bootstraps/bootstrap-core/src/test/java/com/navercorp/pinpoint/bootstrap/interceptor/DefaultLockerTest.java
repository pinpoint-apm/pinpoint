package com.navercorp.pinpoint.bootstrap.interceptor;

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.interceptor.registry.DefaultLocker;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.Locker;

public class DefaultLockerTest {

    @Test
    public void testLock_Null0() throws Exception {
        Locker locker = new DefaultLocker();
        Assert.assertTrue(locker.lock(null));
        Assert.assertTrue(locker.lock(null));
    }

    @Test
    public void testLock_Null1() throws Exception {
        Locker locker = new DefaultLocker();

        Assert.assertTrue(locker.lock(null));
        String one = "1";
        Assert.assertTrue(locker.lock(one));
        Assert.assertFalse(locker.lock("2"));

        Assert.assertFalse(locker.lock(null));

        Assert.assertFalse(locker.unlock(null));
        @SuppressWarnings("all")
        String s = new String("1");
        Assert.assertFalse(locker.unlock(s));

        Assert.assertTrue(locker.unlock(one));

    }

    @Test
    public void testUnlock() throws Exception {

    }
}