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

package com.navercorp.pinpoint.bootstrap.plugin;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.plugin.MetadataInitializationStrategy.ByConstructor;

public class ClassEditorBuilder {
    private final ByteCodeInstrumentor instrumentor;
    private final TraceContext traceContext;
    
    private final List<Injector> injectors = new ArrayList<Injector>();
    private final List<InjectorBuilder> injectorBuilders = new ArrayList<InjectorBuilder>();
    
    private String targetClassName;
    private Condition condition;
    
    public ClassEditorBuilder(ByteCodeInstrumentor instrumentor, TraceContext traceContext) {
        this.instrumentor = instrumentor;
        this.traceContext = traceContext;
    }
    
    public void edit(String targetClassName) {
        this.targetClassName = targetClassName;
    }
    
    public void when(Condition condition) {
        this.condition = condition;
    }
    
    public void inject(FieldSnooper snooper, String fieldName) {
        injectors.add(new FieldSnooperInjector(snooper, fieldName));
    }
    
    public void inject(MetadataHolder holder) {
        injectors.add(new MetadataInjector(holder));
    }
    
    public void inject(MetadataHolder holder, String initialValueType) {
        injectors.add(new MetadataInjector(holder, new ByConstructor(initialValueType)));
    }
    
    public InterceptorBuilder newInterceptorBuilder() {
        InterceptorBuilder interceptorBuilder = new InterceptorBuilder();
        injectorBuilders.add(interceptorBuilder);
        return interceptorBuilder;
    }
    
    public DedicatedClassEditor build() {
        List<Injector> injectors = new ArrayList<Injector>(this.injectors);
        
        for (InjectorBuilder builder : injectorBuilders) {
            injectors.add(builder.build());
        }
        
        DedicatedClassEditor editor = new BasicClassEditor(targetClassName, injectors);
        
        if (condition != null) {
            editor = new ConditionalClassEditor(condition, editor);
        }
        
        return editor;
    }
    
    private static abstract class InjectorBuilder {
        abstract Injector build();
    }
    
    public class InterceptorBuilder extends InjectorBuilder {
        private String methodName;
        private String[] parameterNames;
        private MethodFilter filter;
        
        private String interceptorClassName;
        private Condition condition;
        private String scopeName;
        private Object[] constructorArguments;
        private boolean singleton;
        
        public void intercept(String methodName, String... parameterTypeNames) {
            this.methodName = methodName;
            this.parameterNames = parameterTypeNames;
        }
        
        public void interceptMethodsFilteredBy(MethodFilter filter) {
            this.filter = filter;
        }
        
        public void interceptConstructor(String... parameterTypeNames) {
            this.parameterNames = parameterTypeNames;
        }

        public void in(String scopeName) {
            this.scopeName = scopeName;
        }

        public void with(String interceptorClassName) {
            this.interceptorClassName = interceptorClassName;
        }
        
        public void constructedWith(Object... args) {
            this.constructorArguments = args;
        }
        
        public void singleton(boolean singleton) {
            this.singleton = singleton;
        }
        
        public void when(Condition condition) {
            this.condition = condition;
        }
        
        @Override
        Injector build() {
            InterceptorFactory interceptorFactory = new DefaultInterceptorFactory(instrumentor, traceContext, interceptorClassName, constructorArguments, scopeName);
            
            Injector injector;
            
            if (filter != null) {
                injector = new FilteringInterceptorInjector(filter, interceptorFactory, singleton);
            } else if (methodName != null) {
                injector = new DedicatedInterceptorInjector(methodName, parameterNames, interceptorFactory);
            } else {
                injector = new ConstructorInterceptorInjector(parameterNames, interceptorFactory);
            }
            
            if (condition != null) {
                injector = new ConditionalInterceptorInjector(condition, injector);
            }
            
            return injector;
        }
    }
}
