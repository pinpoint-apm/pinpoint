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

package com.navercorp.pinpoint.profiler.modifier.redis;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;

/**
 * jedis(redis client) modifier
 * 
 * @author jaehong.kim
 *
 */
public class BinaryJedisModifier extends JedisModifier {

    public BinaryJedisModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "redis/clients/jedis/BinaryJedis";
    }

    @Override
    protected void beforeAddInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws NotFoundInstrumentException, InstrumentException {
        // for trace endPoint. 
        instrumentClass.addTraceValue(MapTraceValue.class);
    }
}
