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

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.ArcusScope;

import org.slf4j.Logger;


/**
 * @author emeroad
 */
public class FutureModifier extends AbstractModifier  {

    protected Logger logger;

    public FutureModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            aClass.addTraceVariable("__operation", "__setOperation", "__getOperation", "net.spy.memcached.ops.Operation");

            Interceptor futureSetOperationInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.FutureSetOperationInterceptor");
            aClass.addInterceptor("setOperation", new String[]{"net.spy.memcached.ops.Operation"}, futureSetOperationInterceptor);
            
            SimpleAroundInterceptor futureGetInterceptor = (SimpleAroundInterceptor) byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.FutureGetInterceptor");

            aClass.addGroupInterceptor("get", new String[]{Long.TYPE.toString(), "java.util.concurrent.TimeUnit"}, futureGetInterceptor, ArcusScope.SCOPE);
            
            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    @Override
    public Matcher getMatcher() {
        List<String> classNameList = Arrays.asList(
                "net/spy/memcached/internal/OperationFuture",
                "net/spy/memcached/internal/GetFuture",
                "net/spy/memcached/internal/ImmediateFuture",
                // arcus future
                "net/spy/memcached/internal/CollectionFuture");
        return Matchers.newMultiClassNameMatcher(classNameList);
    }
}