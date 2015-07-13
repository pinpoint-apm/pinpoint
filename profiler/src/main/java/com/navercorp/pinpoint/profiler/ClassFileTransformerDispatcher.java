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

package com.navercorp.pinpoint.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.RetransformEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.DefaultModifierRegistry;
import com.navercorp.pinpoint.profiler.modifier.ModifierRegistry;
import com.navercorp.pinpoint.profiler.plugin.ClassFileTransformerAdaptor;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author emeroad
 * @author netspider
 */
public class ClassFileTransformerDispatcher implements ClassFileTransformer, RetransformEventListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ClassLoader agentClassLoader = this.getClass().getClassLoader();

    private final ModifierRegistry modifierRegistry;

    private final DefaultAgent agent;
    private final ByteCodeInstrumentor byteCodeInstrumentor;
    private final ClassFileRetransformer retransformer;

    private final ProfilerConfig profilerConfig;

    private final ClassFileFilter skipFilter;
    
    public ClassFileTransformerDispatcher(DefaultAgent agent, ByteCodeInstrumentor byteCodeInstrumentor, List<DefaultProfilerPluginContext> pluginContexts) {
        if (agent == null) {
            throw new NullPointerException("agent must not be null");
        }
        if (byteCodeInstrumentor == null) {
            throw new NullPointerException("byteCodeInstrumentor must not be null");
        }

        
        this.agent = agent;
        this.byteCodeInstrumentor = byteCodeInstrumentor;
        this.retransformer = new DefaultClassFileRetransformer();
        this.profilerConfig = agent.getProfilerConfig();
        this.modifierRegistry = createModifierRegistry(pluginContexts);
        this.skipFilter = new DefaultClassFileFilter(agentClassLoader);
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String jvmClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        if (classBeingRedefined != null) {
            return this.retransform(classLoader, jvmClassName, classBeingRedefined, protectionDomain, classFileBuffer);
        }

        if (skipFilter.doFilter(classLoader, jvmClassName, classBeingRedefined, protectionDomain, classFileBuffer)) {
            return null;
        }

        AbstractModifier findModifier = this.modifierRegistry.findModifier(jvmClassName);
        if (findModifier == null) {
            // TODO For debug
            // TODO What if a modifier is duplicated?
            if (this.profilerConfig.getProfilableClassFilter().filter(jvmClassName)) {
                // Added to see if call stack view is OK on a test machine.
                findModifier = this.modifierRegistry.findModifier("*");
            } else {
                return null;
            }
        }

        if (isDebug) {
            logger.debug("[transform] cl:{} className:{} Modifier:{}", classLoader, jvmClassName, findModifier.getClass().getName());
        }
        final String javaClassName = JavaAssistUtils.jvmNameToJavaName(jvmClassName);

        try {
            final Thread thread = Thread.currentThread();
            final ClassLoader before = getContextClassLoader(thread);
            thread.setContextClassLoader(this.agentClassLoader);
            try {
                return findModifier.modify(classLoader, javaClassName, protectionDomain, classFileBuffer);
            } finally {
                // The context class loader have to be recovered even if it was null.
                thread.setContextClassLoader(before);
            }
        }
        catch (Throwable e) {
            logger.error("Modifier:{} modify fail. cl:{} ctxCl:{} agentCl:{} Cause:{}",
                    findModifier.getMatcher(), classLoader, Thread.currentThread().getContextClassLoader(), agentClassLoader, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void addRetransformEvent(Class<?> target, final ClassFileTransformer transformer) {
        this.retransformer.addRetransformEvent(target, transformer);
    }

    private byte[] retransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return retransformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    private ClassLoader getContextClassLoader(Thread thread) throws Throwable {
        try {
            return thread.getContextClassLoader();
        } catch (SecurityException se) {
            throw se;
        } catch (Throwable th) {
            if (isDebug) {
                logger.debug("getContextClassLoader(). Caused:{}", th.getMessage(), th);
            }
            throw th;
        }
    }

    private ModifierRegistry createModifierRegistry(List<DefaultProfilerPluginContext> pluginContexts) {
        DefaultModifierRegistry modifierRepository = new DefaultModifierRegistry(agent, byteCodeInstrumentor);

        modifierRepository.addMethodModifier();

        modifierRepository.addTomcatModifier();

        // jdbc
        modifierRepository.addJdbcModifier();

        // rpc
        modifierRepository.addConnectorModifier();

        // orm
        modifierRepository.addOrmModifier();

        // spring beans
        modifierRepository.addSpringBeansModifier();

       
        // log4j
        modifierRepository.addLog4jModifier();
        
        // logback
        modifierRepository.addLogbackModifier();
        
        loadEditorsFromPlugins(modifierRepository, pluginContexts);
        
        return modifierRepository;
    }

    private void loadEditorsFromPlugins(DefaultModifierRegistry modifierRepository, List<DefaultProfilerPluginContext> pluginContexts) {
        for (DefaultProfilerPluginContext pluginContext : pluginContexts) {
            for (ClassFileTransformer transformer : pluginContext.getClassEditors()) {
                if (transformer instanceof PinpointClassFileTransformer) {
                    PinpointClassFileTransformer t = (PinpointClassFileTransformer)transformer;
                    logger.info("Registering class file transformer {} for {} ", t, t.getMatcher());
                    modifierRepository.addModifier(new ClassFileTransformerAdaptor(byteCodeInstrumentor, t));
                } else {
                    logger.warn("Ignore class file transformer {}", transformer);
                }
            }
        }
    }

}
