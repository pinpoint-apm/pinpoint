package com.nhn.pinpoint.plugin.arcus;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.nhn.pinpoint.plugin.arcus.accessor.CacheKeyAccessor;
import com.nhn.pinpoint.plugin.arcus.accessor.CacheNameAccessor;
import com.nhn.pinpoint.plugin.arcus.accessor.OperationAccessor;
import com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor;
import com.nhn.pinpoint.test.fork.ForkRunner;
import com.nhn.pinpoint.test.fork.OnChildClassLoader;
import com.nhn.pinpoint.test.fork.PinpointAgent;
import com.nhn.pinpoint.test.fork.PinpointConfig;

@RunWith(ForkRunner.class)
@PinpointConfig("pinpoint-test.config")
@PinpointAgent("build/pinpoint-agent")
@OnChildClassLoader
public class ArcusPluginTest {

    // TODO how to test intercpetor?
    
    @Test
    public void test() throws Exception {

        Class<?> arcusClient = Class.forName("net.spy.memcached.ArcusClient");
        
        Class<?> cacheManager = Class.forName("net.spy.memcached.CacheManager");
        assertTrue(ServiceCodeAccessor.class.isAssignableFrom(cacheManager));

        Class<?> collectionFuture = Class.forName("net.spy.memcached.internal.CollectionFuture");
        assertTrue(OperationAccessor.class.isAssignableFrom(collectionFuture));

        Class<?> baseOperationImpl = Class.forName("net.spy.memcached.protocol.BaseOperationImpl");
        assertTrue(ServiceCodeAccessor.class.isAssignableFrom(baseOperationImpl));
        
        
        Class<?> getFuture = Class.forName("net.spy.memcached.internal.GetFuture");
        assertTrue(OperationAccessor.class.isAssignableFrom(getFuture));
        
        Class<?> immediateFuture = Class.forName("net.spy.memcached.internal.ImmediateFuture");
//        assertTrue(OperationAccessor.class.isAssignableFrom(immediateFuture));
        
        Class<?> operationFuture = Class.forName("net.spy.memcached.internal.OperationFuture");
        assertTrue(OperationAccessor.class.isAssignableFrom(operationFuture));
        
        Class<?> frontCacheGetFuture = Class.forName("net.spy.memcached.plugin.FrontCacheGetFuture");
        assertTrue(CacheNameAccessor.class.isAssignableFrom(frontCacheGetFuture));
        assertTrue(CacheKeyAccessor.class.isAssignableFrom(frontCacheGetFuture));
        
        Class<?> frontCacheMemcachedClient = Class.forName("net.spy.memcached.plugin.FrontCacheMemcachedClient");
        
        Class<?> memcachedClient = Class.forName("net.spy.memcached.MemcachedClient");
        assertTrue(ServiceCodeAccessor.class.isAssignableFrom(memcachedClient));
    }
}