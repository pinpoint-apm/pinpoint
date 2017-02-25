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

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentEngine;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParser;
import com.navercorp.pinpoint.profiler.context.ApplicationContext;
import com.navercorp.pinpoint.profiler.context.scope.ConcurrentPool;
import com.navercorp.pinpoint.profiler.context.scope.InterceptorScopeFactory;
import com.navercorp.pinpoint.profiler.context.scope.Pool;
import com.navercorp.pinpoint.profiler.instrument.ClassInjector;
import com.navercorp.pinpoint.profiler.instrument.PluginClassInjector;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class DefaultProfilerPluginContext implements ProfilerPluginSetupContext, InstrumentContext {
    private final ApplicationContext applicationContext;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final ClassInjector classInjector;
    
    private final List<ApplicationTypeDetector> serverTypeDetectors = new ArrayList<ApplicationTypeDetector>();
    private final List<ClassFileTransformer> classTransformers = new ArrayList<ClassFileTransformer>();

    private final List<JdbcUrlParser> jdbcUrlParsers = new ArrayList<JdbcUrlParser>();

    private final Pool<String, InterceptorScope> interceptorScopePool = new ConcurrentPool<String, InterceptorScope>(new InterceptorScopeFactory());

    public DefaultProfilerPluginContext(ApplicationContext applicationContext, DynamicTransformTrigger dynamicTransformTrigger, ClassInjector classInjector) {
        if (applicationContext == null) {
            throw new NullPointerException("applicationContext must not be null");
        }
        if (dynamicTransformTrigger == null) {
            throw new NullPointerException("dynamicTransformTrigger must not be null");
        }
        if (classInjector == null) {
            throw new NullPointerException("classInjector must not be null");
        }
        this.applicationContext = applicationContext;
        this.dynamicTransformTrigger = dynamicTransformTrigger;
        this.classInjector = classInjector;
    }

    @Override
    public ProfilerConfig getConfig() {
        return applicationContext.getProfilerConfig();
    }

    public PluginConfig getPluginConfig() {
        if (classInjector instanceof PluginClassInjector) {
            return ((PluginClassInjector) classInjector).getPluginConfig();
        }
        return null;
    }

    @Override
    public TraceContext getTraceContext() {
        final TraceContext context = applicationContext.getTraceContext();
        if (context == null) {
            throw new IllegalStateException("TraceContext is not created yet");
        }

        return context;
    }
        
    @Override
    public void addApplicationTypeDetector(ApplicationTypeDetector... detectors) {
        if (detectors == null) {
            return;
        }
        for (ApplicationTypeDetector detector : detectors) {
            serverTypeDetectors.add(detector);
        }
    }
    
    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, byte[] classFileBuffer) {
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }
        try {
            final InstrumentEngine instrumentEngine = getInstrumentEngine();
            return instrumentEngine.getClass(this, classLoader, className, classFileBuffer);
        } catch (NotFoundInstrumentException e) {
            return null;
        }
    }
    
    @Override
    public boolean exist(ClassLoader classLoader, String className) {
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }
        final InstrumentEngine instrumentEngine = getInstrumentEngine();
        return instrumentEngine.hasClass(classLoader, className);
    }

    private InstrumentEngine getInstrumentEngine() {
        InstrumentEngine instrumentEngine = applicationContext.getInstrumentEngine();
        return instrumentEngine;
    }

    @Override
    public void addClassFileTransformer(final String targetClassName, final TransformCallback transformCallback) {
        if (targetClassName == null) {
            throw new NullPointerException("targetClassName must not be null");
        }
        if (transformCallback == null) {
            throw new NullPointerException("transformCallback must not be null");
        }

        final Matcher matcher = Matchers.newClassNameMatcher(JavaAssistUtils.javaNameToJvmName(targetClassName));
        final MatchableClassFileTransformerGuardDelegate guard = new MatchableClassFileTransformerGuardDelegate(this, matcher, transformCallback);
        classTransformers.add(guard);
    }
    
    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName, final TransformCallback transformCallback) {
        if (targetClassName == null) {
            throw new NullPointerException("targetClassName must not be null");
        }
        if (transformCallback == null) {
            throw new NullPointerException("transformCallback must not be null");
        }

        final ClassFileTransformerGuardDelegate classFileTransformerGuardDelegate = new ClassFileTransformerGuardDelegate(this, transformCallback);

        dynamicTransformTrigger.addClassFileTransformer(classLoader, targetClassName, classFileTransformerGuardDelegate);
    }


    @Override
    public void retransform(Class<?> target, final TransformCallback transformCallback) {
        if (target == null) {
            throw new NullPointerException("target must not be null");
        }
        if (transformCallback == null) {
            throw new NullPointerException("transformCallback must not be null");
        }

        final ClassFileTransformerGuardDelegate classFileTransformerGuardDelegate = new ClassFileTransformerGuardDelegate(this, transformCallback);

        dynamicTransformTrigger.retransform(target, classFileTransformerGuardDelegate);
    }


    @Override
    public <T> Class<? extends T> injectClass(ClassLoader targetClassLoader, String className) {
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }

        return classInjector.injectClass(targetClassLoader, className);
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String classPath) {
        if (classPath == null) {
            return null;
        }

        return classInjector.getResourceAsStream(targetClassLoader, classPath);
    }

    public List<ClassFileTransformer> getClassEditors() {
        return classTransformers;
    }

    public List<ApplicationTypeDetector> getApplicationTypeDetectors() {
        return serverTypeDetectors;
    }

    @Override
    public InterceptorScope getInterceptorScope(String name) {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }

        return interceptorScopePool.get(name);
    }

    @Override
    public void addJdbcUrlParser(JdbcUrlParser... jdbcUrlParsers) {
        if (jdbcUrlParsers == null) {
            return;
        }

        for (JdbcUrlParser jdbcUrlParser : jdbcUrlParsers) {
            this.jdbcUrlParsers.add(jdbcUrlParser);
        }
    }

}
