/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.modifier.arcus;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;

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
