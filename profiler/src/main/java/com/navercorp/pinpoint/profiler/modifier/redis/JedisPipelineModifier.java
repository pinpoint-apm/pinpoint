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
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;

/**
 * jedis(redis client) pipeline modifier
 * 
 * @author jaehong.kim
 *
 */
public class JedisPipelineModifier extends JedisPipelineBaseModifier {

    public JedisPipelineModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "redis/clients/jedis/Pipeline";
    }

    @Override
    protected void beforeAddInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws NotFoundInstrumentException, InstrumentException {
        // for trace endPoint
        instrumentClass.addTraceValue(MapTraceValue.class);

        // add constructor interceptor
        addConstructorInterceptor(classLoader, protectedDomain, instrumentClass);
        
        // add method interceptor
        addSetClientMethodInterceptor(classLoader, protectedDomain, instrumentClass);
    }

    private void addSetClientMethodInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws NotFoundInstrumentException, InstrumentException {
        for (MethodInfo method : instrumentClass.getDeclaredMethods()) {
            if (method.getName().equals("setClient")) {
                // jedis 2.x
                final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.redis.interceptor.JedisPipelineSetClientMethodInterceptor");
                instrumentClass.addInterceptor("setClient", new String[] { "redis.clients.jedis.Client" }, methodInterceptor);
            }
        }
    }

    private void addConstructorInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws InstrumentException {
        final Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.redis.interceptor.JedisPipelineConstructorInterceptor");
        try {
            // jedis 1.x
            instrumentClass.addConstructorInterceptor(new String[] { "redis.clients.jedis.Client" }, constructorInterceptor);
        } catch (Exception e) {
            // backward compatibility error
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to add constructor interceptor(only jedis 1.x). caused={}", e.getMessage(), e);
            }
        }
    }
}