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
package com.navercorp.pinpoint.plugin.user;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * @author jaehong.kim
 *
 */
public class UserPlugin implements ProfilerPlugin {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final UserPluginConfig config = new UserPluginConfig(context.getConfig());

        // add user include methods
        for (String fullQualifiedMethodName : config.getIncludeList()) {
            try {
                addUserIncludeClass(context, fullQualifiedMethodName);
                if (logger.isDebugEnabled()) {
                    logger.debug("Add user include class interceptor {}", fullQualifiedMethodName);
                }
            } catch (Exception e) {
                logger.warn("Failed to add user include class(" + fullQualifiedMethodName + ").", e);
            }
        }
    }

    private void addUserIncludeClass(ProfilerPluginSetupContext context, final String fullQualifiedMethodName) {
        final String className = toClassName(fullQualifiedMethodName);
        final String methodName = toMethodName(fullQualifiedMethodName);

        context.addClassFileTransformer(className, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(methodName))) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.user.interceptor.UserIncludeMethodInterceptor");
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }

                return target.toBytecode();
            }
        });
    }

    String toClassName(String fullQualifiedMethodName) {
        final int classEndPosition = fullQualifiedMethodName.lastIndexOf(".");
        if (classEndPosition <= 0) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(0, classEndPosition);
    }

    String toMethodName(String fullQualifiedMethodName) {
        final int methodBeginPosition = fullQualifiedMethodName.lastIndexOf(".");
        if (methodBeginPosition <= 0 || methodBeginPosition + 1 >= fullQualifiedMethodName.length()) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(methodBeginPosition + 1);
    }
}