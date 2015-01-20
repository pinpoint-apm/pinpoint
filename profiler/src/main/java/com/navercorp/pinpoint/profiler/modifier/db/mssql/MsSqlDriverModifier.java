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
package com.navercorp.pinpoint.profiler.modifier.db.mssql;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.db.Scopeable;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.DriverConnectInterceptor;

/**
 *
 * @author Barney Kim
 */
public abstract class MsSqlDriverModifier extends AbstractModifier implements Scopeable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * @param byteCodeInstrumentor
     * @param agent
     */
    public MsSqlDriverModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.profiler.modifier.Modifier#modify(java.lang.ClassLoader, java.lang.String, java.security.ProtectionDomain, byte[])
     */
    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }
        try {
            InstrumentClass driver = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);

            final Scope scope = byteCodeInstrumentor.getScope(getScopeName());
            Interceptor createConnection = new DriverConnectInterceptor(scope);
            String[] params = new String[] { "java.lang.String", "java.util.Properties" };
            driver.addInterceptor("connect", params, createConnection);

            if (logger.isInfoEnabled()) {
                logger.info("{} class is converted.", className);
            }

            return driver.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(this.getClass().getSimpleName() + " modify fail. Cause:" + e.getMessage(), e);
            }
            return null;
        }
    }

}
