package com.navercorp.pinpoint.plugin.arcus;

import org.junit.Ignore;
import org.junit.Test;

//@RunWith(ForkRunner.class)
//@PinpointConfig("pinpoint-test.config")
//@PinpointAgent("build/pinpoint-agent")
//@OnChildClassLoader
public class ArcusPluginTest {

    // TODO how to test intercpetor?
    
    @Test
    @Ignore // until arcus modifier/interceptors are removed from pinpoint-profiler
    public void test() throws Exception {
//
//        Class<?> arcusClient = Class.forName("net.spy.memcached.ArcusClient");
//        
//        Class<?> cacheManager = Class.forName("net.spy.memcached.CacheManager");
//        assertTrue(ObjectAccessor.class.isAssignableFrom(cacheManager));
//
//        Class<?> collectionFuture = Class.forName("net.spy.memcached.internal.CollectionFuture");
//        assertTrue(ObjectAccessor.class.isAssignableFrom(collectionFuture));
//
//        Class<?> baseOperationImpl = Class.forName("net.spy.memcached.protocol.BaseOperationImpl");
//        assertTrue(ObjectAccessor.class.isAssignableFrom(baseOperationImpl));
//        
//        
//        Class<?> getFuture = Class.forName("net.spy.memcached.internal.GetFuture");
//        assertTrue(ObjectAccessor.class.isAssignableFrom(getFuture));
//        
//        Class<?> immediateFuture = Class.forName("net.spy.memcached.internal.ImmediateFuture");
////        assertTrue(OperationAccessor.class.isAssignableFrom(immediateFuture));
//        
//        Class<?> operationFuture = Class.forName("net.spy.memcached.internal.OperationFuture");
//        assertTrue(ObjectAccessor.class.isAssignableFrom(operationFuture));
//        
//        Class<?> frontCacheGetFuture = Class.forName("net.spy.memcached.plugin.FrontCacheGetFuture");
//        assertTrue(ObjectAccessor.class.isAssignableFrom(frontCacheGetFuture));
//        assertTrue(ObjectAccessor2.class.isAssignableFrom(frontCacheGetFuture));
//        
//        Class<?> frontCacheMemcachedClient = Class.forName("net.spy.memcached.plugin.FrontCacheMemcachedClient");
//        
//        Class<?> memcachedClient = Class.forName("net.spy.memcached.MemcachedClient");
//        assertTrue(ObjectAccessor.class.isAssignableFrom(memcachedClient));
    }
}