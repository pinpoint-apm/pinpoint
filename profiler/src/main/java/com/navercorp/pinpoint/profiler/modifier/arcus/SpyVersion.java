package com.nhn.pinpoint.profiler.modifier.arcus;

import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;

public enum SpyVersion {

    SPYMEMCACHED_2_11,
    ARCUSCLIENT_1_6,
    ERROR
    ;

    public SpyVersion identifyWithMemcachedConnection(InstrumentClass aClass) {
        String className = aClass.getName();
        if (! "net/spy/memcached/MemcachedConnection".equals(className)) {
            return ERROR;
        }

        String[] createConnectionArgs = {"java.util.Collection"};
        boolean isSpy = aClass.hasDeclaredMethod("createConnection", createConnectionArgs);

        if (isSpy) {
            return SPYMEMCACHED_2_11;
        } else {
            return ARCUSCLIENT_1_6;
        }
    }

    public SpyVersion identifyWithMemcachedClient(InstrumentClass aClass) {
        String className = aClass.getName();
        if (! "net/spy/memcached/MemcachedClient".equals(className)) {
            return ERROR;
        }

        String[] addOpArgs = {"java.lang.String", "net.spy.memcached.ops.Operation"};
        boolean isArcus = aClass.hasDeclaredMethod("addOp", addOpArgs);

        return SPYMEMCACHED_2_11;
    }

}
