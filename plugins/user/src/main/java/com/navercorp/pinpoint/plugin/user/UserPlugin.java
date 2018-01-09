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
import java.util.*;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * @author jaehong.kim
 *
 */
public class UserPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;
    
    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final UserPluginConfig config = new UserPluginConfig(context.getConfig());

        // merge
        final Map<String, Set<String>> methods = parseUserMethods(config.getIncludeList());
        if (logger.isInfoEnabled()) {
            logger.info("UserPlugin entry points={}", methods);
        }

        // add user include methods
        for(Map.Entry<String, Set<String>> entry : methods.entrySet()) {
            try {
                addUserIncludeClass(entry.getKey(), entry.getValue());
                if (logger.isDebugEnabled()) {
                    logger.debug("Add user include class interceptor {}.{}", entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                logger.warn("Failed to add user include class(" + entry.getKey() + "." + entry.getValue() + ").", e);
            }
        }

        // add message queue client handler methods
        List<String> clientHandlerMethods = config.getMqClientHandlerMethods();
        addMessageQueueClientHandlerMethods(clientHandlerMethods);
    }

    private void addUserIncludeClass(final String className, final Set<String> methodNames) {
       transformTemplate.transform(className, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                final String[] names = methodNames.toArray(new String[methodNames.size()]);
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(names))) {
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

    private void addMessageQueueClientHandlerMethods(List<String> clientHandlerMethods) {
        Map<String, Set<String>> clientHandlers = parseUserMethods(clientHandlerMethods);
        for (Map.Entry<String, Set<String>> clientHandler : clientHandlers.entrySet()) {
            final String className = clientHandler.getKey();
            final Set<String> methodNames = clientHandler.getValue();
            transformTemplate.transform(className, new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                    final String[] names = methodNames.toArray(new String[methodNames.size()]);
                    for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(names))) {
                        try {
                            method.addInterceptor("com.navercorp.pinpoint.plugin.user.interceptor.MQExternalClientHandlerInterceptor");
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
    }

    private Map<String, Set<String>> parseUserMethods(List<String> fullyQualifiedMethodNames) {
        Map<String, Set<String>> userMethods = new HashMap<String, Set<String>>();
        for (String fullyQualifiedMethodName : fullyQualifiedMethodNames) {
            try {
                final String className = toClassName(fullyQualifiedMethodName);
                final String methodName = toMethodName(fullyQualifiedMethodName);
                Set<String> methodNames = userMethods.get(className);
                if (methodNames == null) {
                    methodNames = new HashSet<String>();
                    userMethods.put(className, methodNames);
                }
                methodNames.add(methodName);
            } catch (Exception e) {
                logger.warn("Failed to parse user method(" + fullyQualifiedMethodName + ").", e);
            }
        }
        return userMethods;
    }

    String toClassName(String fullQualifiedMethodName) {
        final int classEndPosition = fullQualifiedMethodName.lastIndexOf('.');
        if (classEndPosition <= 0) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(0, classEndPosition);
    }

    String toMethodName(String fullQualifiedMethodName) {
        final int methodBeginPosition = fullQualifiedMethodName.lastIndexOf('.');
        if (methodBeginPosition <= 0 || methodBeginPosition + 1 >= fullQualifiedMethodName.length()) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(methodBeginPosition + 1);
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}