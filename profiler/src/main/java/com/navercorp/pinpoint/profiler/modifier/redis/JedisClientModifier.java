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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

/**
 * jedis(redis client) Client modifier
 * - trace endPoint
 * 
 * @author jaehong.kim
 *
 */
public class JedisClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public JedisClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "redis/clients/jedis/Client";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }

        try {
            final InstrumentClass instrumentClass = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);

            // for trace endPoint
            instrumentClass.addTraceValue(MapTraceValue.class);
            
            final Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.redis.interceptor.JedisClientConstructorInterceptor");
            instrumentClass.addConstructorInterceptor(new String[] { "java.lang.String" }, constructorInterceptor);
            instrumentClass.addConstructorInterceptor(new String[] { "java.lang.String", "int" }, constructorInterceptor);

            return instrumentClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to modifier. caused={}", e.getMessage(), e);
            }
        }

        return null;
    }
}