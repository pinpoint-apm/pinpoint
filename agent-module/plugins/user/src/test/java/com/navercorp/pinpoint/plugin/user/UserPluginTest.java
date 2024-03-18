package com.navercorp.pinpoint.plugin.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserPluginTest {

    @Test
    public void test() {
        UserPlugin plugin = new UserPlugin();
        assertEquals("org.apache.commons.pool.impl.GenericKeyedObjectPool", plugin.toClassName("org.apache.commons.pool.impl.GenericKeyedObjectPool.borrowObject"));
        assertEquals("borrowObject", plugin.toMethodName("org.apache.commons.pool.impl.GenericKeyedObjectPool.borrowObject"));
    }
}
