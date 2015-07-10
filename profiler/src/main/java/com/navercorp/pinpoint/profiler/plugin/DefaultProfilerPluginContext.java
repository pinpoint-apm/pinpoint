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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.navercorp.pinpoint.profiler.plugin.transformer.DefaultClassFileTransformerBuilder;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import com.navercorp.pinpoint.profiler.util.NameValueList;

public class DefaultProfilerPluginContext implements ProfilerPluginContext {
    private final DefaultAgent agent;
    private final ProfilerPluginClassLoader classInjector;
    
    private final List<ApplicationTypeDetector> serverTypeDetectors = new ArrayList<ApplicationTypeDetector>();
    private final List<ClassFileTransformer> classTransformers = new ArrayList<ClassFileTransformer>();
    
    private final ConcurrentMap<String, Object> attributeMap = new ConcurrentHashMap<String, Object>();
    private final NameValueList<MetadataAccessor> metadataAccessors = new NameValueList<MetadataAccessor>();
    private final NameValueList<FieldAccessor> fieldSnoopers = new NameValueList<FieldAccessor>();
    private final NameValueList<InterceptorGroup> interceptorGroups = new NameValueList<InterceptorGroup>();
    
    private int metadataAccessorIndex = 0;
    private int fieldSnooperIndex = 0;
    
    private boolean initialized = false;
    
    
    public DefaultProfilerPluginContext(DefaultAgent agent, ProfilerPluginClassLoader classInjector) {
        this.agent = agent;
        this.classInjector = classInjector;
    }

    @Override
    public ClassFileTransformerBuilder getClassFileTransformerBuilder(String targetClassName) {
        return new DefaultClassFileTransformerBuilder(this, targetClassName);
    }
    
    @Override
    public void addClassFileTransformer(ClassFileTransformer transformer) {
        if (initialized) {
            throw new IllegalStateException("Context already initialized");
        }

        classTransformers.add(transformer);
    }

    @Override
    public ProfilerConfig getConfig() {
        return agent.getProfilerConfig();
    }

    @Override
    public TraceContext getTraceContext() {
        TraceContext context = agent.getTraceContext();
        
        if (context == null) {
            throw new IllegalStateException("TraceContext is not created yet");
        }
        
        return context;
    }

    @Override
    public ByteCodeInstrumentor getByteCodeInstrumentor() {
        return agent.getByteCodeInstrumentor();
    }

    @Override
    public MetadataAccessor getMetadataAccessor(String name) {
        MetadataAccessor accessor = metadataAccessors.get(name);
        
        if (accessor != null) {
            return accessor;
        }
        
        try {
            accessor = MetadataAccessor.get(metadataAccessorIndex);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("Cannot allocate MetadataAccessor. Exceeded max:" + metadataAccessorIndex);
        }

        metadataAccessors.add(name, accessor);
        metadataAccessorIndex++;
        
        return accessor;
    }

    @Override
    public FieldAccessor getFieldAccessor(String name) {
        FieldAccessor snooper = fieldSnoopers.get(name);
        
        if (snooper != null) {
            return snooper;
        }
        
        try {
            snooper = FieldAccessor.get(fieldSnooperIndex);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("Cannot allocate FieldAccessor. Exceeded max:" + fieldSnooperIndex);
        }
        
        fieldSnoopers.add(name, snooper);
        fieldSnooperIndex++;
        
        return snooper;
    }
    
    @Override
    public Object setAttribute(String key, Object value) {
        return attributeMap.put(key, value);
    }

    @Override
    public Object getAttribute(String key) {
        return attributeMap.get(key);
    }
    
    @Override
    public void addApplicationTypeDetector(ApplicationTypeDetector... detectors) {
        if (initialized) {
            throw new IllegalStateException("Context already initialized");
        }
        
        for (ApplicationTypeDetector detector : detectors) {
            serverTypeDetectors.add(detector);
        }
    }
    
    @Override
    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, byte[] classFileBuffer) {
        try {
            return ((JavaAssistByteCodeInstrumentor)agent.getByteCodeInstrumentor()).getClass(this, classLoader, className, classFileBuffer);
        } catch (NotFoundInstrumentException e) {
            return null;
        }
    }

    @Override
    public void addClassFileTransformer(final String targetClassName, final ClassFileTransformer transformer) {
        if (initialized) {
            throw new IllegalStateException("Context already initialized");
        }

        classTransformers.add(new PinpointClassFileTransformer() {
            private final Matcher matcher = Matchers.newClassNameMatcher(JavaAssistUtils.javaNameToJvmName(targetClassName));
            
            @Override
            public Matcher getMatcher() {
                return matcher;
            }
            
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                return transformer.transform(loader, targetClassName, classBeingRedefined, protectionDomain, classfileBuffer);
            }
        });
    }

    @Override
    public void retransform(Class<?> target, ClassFileTransformer classEditor) {
        agent.getByteCodeInstrumentor().retransform(target, classEditor);
    }

    public ProfilerPluginClassLoader getClassInjector() {
        return classInjector;
    }

    public List<ClassFileTransformer> getClassEditors() {
        return classTransformers;
    }

    public List<ApplicationTypeDetector> getApplicationTypeDetectors() {
        return serverTypeDetectors;
    }

    @Override
    public InterceptorGroup getInterceptorGroup(String name) {
        InterceptorGroup group = interceptorGroups.get(name);
        
        if (group == null) {
            group = new DefaultInterceptorGroup(name);
            interceptorGroups.add(name, group);
        }
        
        return group;
    }
    
    public void markInitialized() {
        this.initialized = true;
    }
}
