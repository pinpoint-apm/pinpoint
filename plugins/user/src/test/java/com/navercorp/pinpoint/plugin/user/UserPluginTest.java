package com.navercorp.pinpoint.plugin.user;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserPluginTest {

    @Test
    public void test() {
        UserPlugin plugin = new UserPlugin();
        System.out.println(plugin.toClassName("org.apache.commons.pool.impl.GenericKeyedObjectPool.borrowObject"));
        System.out.println(plugin.toMethodName("org.apache.commons.pool.impl.GenericKeyedObjectPool.borrowObject"));
        
        
    }

}
