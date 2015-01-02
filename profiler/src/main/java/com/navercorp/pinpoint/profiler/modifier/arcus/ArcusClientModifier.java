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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.instrument.Type;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ParameterExtractorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.ArcusScope;
import com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.IndexParameterExtractor;

/**
 * @author netspider
 * @author emeroad
 */
public class ArcusClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ArcusClientModifier(ByteCodeInstrumentor byteCodeInstrumentor,
            Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/spy/memcached/ArcusClient";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName,
            ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }

        try {
            InstrumentClass arcusClient = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            if (!checkCompatibility(arcusClient)) {
                return null;
            }

            final Interceptor setCacheManagerInterceptor = byteCodeInstrumentor.newInterceptor(classLoader,protectedDomain,"com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.SetCacheManagerInterceptor");
            final String[] args = {"net.spy.memcached.CacheManager"};
            arcusClient.addInterceptor("setCacheManager", args, setCacheManagerInterceptor, Type.before);

            List<MethodInfo> declaredMethods = arcusClient.getDeclaredMethods(new ArcusMethodFilter());
            for (MethodInfo method : declaredMethods) {

                SimpleAroundInterceptor apiInterceptor = (SimpleAroundInterceptor) byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain,
                                "com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.ApiInterceptor");
                if (agent.getProfilerConfig().isArucsKeyTrace()) {
                    final int index = ParameterUtils.findFirstString(method, 3);
                    if (index != -1) {
                        ((ParameterExtractorSupport)apiInterceptor).setParameterExtractor(new IndexParameterExtractor(index));
                    }
                }
                arcusClient.addScopeInterceptor(method.getName(), method.getParameterTypes(), apiInterceptor, ArcusScope.SCOPE);
            }

            return arcusClient.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    private boolean checkCompatibility(InstrumentClass arcusClient) {
        // Check if the class has addOp method
        final boolean addOp = arcusClient.hasMethod("addOp", new String[]{"java.lang.String", "net.spy.memcached.ops.Operation"}, "net.spy.memcached.ops.Operation");
        if (!addOp) {
            logger.warn("addOp() not found. skip ArcusClientModifier");
        }
        return addOp;
    }


}