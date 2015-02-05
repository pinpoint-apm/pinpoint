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

package com.navercorp.pinpoint.bootstrap.plugin.editor;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.plugin.FieldSnooper;
import com.navercorp.pinpoint.bootstrap.plugin.MetadataHolder;
import com.navercorp.pinpoint.bootstrap.plugin.MetadataInitializationStrategy.ByConstructor;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;

public class ClassEditorBuilder {
    private final ProfilerPluginContext pluginContext;
    private final ByteCodeInstrumentor instrumentor;
    private final TraceContext traceContext;
    
    private final List<ClassRecipe> recipes = new ArrayList<ClassRecipe>();
    private final List<MethodEditorBuilder> methodEditorBuilders = new ArrayList<MethodEditorBuilder>();
    
    private String targetClassName;
    private ClassCondition condition;

    public ClassEditorBuilder(ProfilerPluginContext pluginContext, ByteCodeInstrumentor instrumentor, TraceContext traceContext) {
        this.pluginContext = pluginContext;
        this.instrumentor = instrumentor;
        this.traceContext = traceContext;
    }

    public void target(String targetClassName) {
        this.targetClassName = targetClassName;
    }
    
    public void condition(ClassCondition condition) {
        this.condition = condition;
    }
    
    public void inject(FieldSnooper snooper, String fieldName) {
        recipes.add(new FieldSnooperInjector(snooper, fieldName));
    }
    
    public void inject(MetadataHolder holder) {
        recipes.add(new MetadataInjector(holder));
    }
    
    public void inject(MetadataHolder holder, String initialValueType) {
        recipes.add(new MetadataInjector(holder, new ByConstructor(initialValueType)));
    }

    public MethodEditorBuilder editMethod() {
        MethodEditorBuilder methodEditorBuilder = new MethodEditorBuilder();
        methodEditorBuilders.add(methodEditorBuilder);
        return methodEditorBuilder;
    }
    
    public DedicatedClassEditor build() {
        ClassRecipe recipe = buildClassRecipe(); 
        DedicatedClassEditor editor = buildClassEditor(recipe);
        
        return editor;
    }

    private DedicatedClassEditor buildClassEditor(ClassRecipe recipe) {
        DedicatedClassEditor editor = new BasicClassEditor(targetClassName, recipe);
        
        if (condition != null) {
            editor = new ConditionalClassEditor(condition, editor);
        }
        return editor;
    }

    private ClassRecipe buildClassRecipe() {
        List<ClassRecipe> recipes = new ArrayList<ClassRecipe>(this.recipes);
        
        for (MethodEditorBuilder builder : methodEditorBuilders) {
            recipes.add(builder.build());
        }
        
        if (recipes.isEmpty()) {
            throw new IllegalStateException("No class edit registered"); 
        }
        
        ClassRecipe recipe = recipes.size() == 1 ? recipes.get(0) : new ClassCookBook(recipes);
        return recipe;
    }
    
    public class MethodEditorBuilder {
        private ClassCondition condition;
        private boolean cacheApi;
        
        private String methodName;
        private String[] parameterNames;
        private MethodFilter filter;
        
        private final List<InterceptorBuilder> interceptorBuilders = new ArrayList<InterceptorBuilder>();
        
        public void targetConstrucotor(String... parameterNames) {
            this.methodName = null;
            this.parameterNames = parameterNames;
        }

        
        public void targetMethod(String methodName, String... parameterNames) {
            this.methodName = methodName;
            this.parameterNames = parameterNames;
        }
        
        public void targetFilter(MethodFilter filter) {
            this.filter = filter;
        }
        
        public InterceptorBuilder injectInterceptor() {
            InterceptorBuilder builder = new InterceptorBuilder();
            interceptorBuilders.add(builder);
            return builder;
        }
        
        public void cacheApi() {
            cacheApi = true;
        }

        MethodEditor build() {
            if (interceptorBuilders.isEmpty()) {
                // For now, a method editor without any interceptor is meaningless. 
                throw new IllegalStateException("No interceptors are defiend");
            }
            
            MethodRecipe recipe = buildMethodRecipe();
            MethodEditor editor = buildMethodEditor(recipe);
            
            return editor;
        }

        private MethodEditor buildMethodEditor(MethodRecipe recipe) {
            MethodEditor editor;
            if (filter != null) {
                editor = new FilteringMethodEditor(filter, recipe);
            } else if (methodName != null) {
                editor = new DedicatedMethodEditor(methodName, parameterNames, recipe);
            } else {
                editor = new ConstructorEditor(parameterNames, recipe);
            }
            
            if (condition != null) {
                editor = new ConditionalMethodEditor(condition, editor);
            }
            return editor;
        }

        private MethodRecipe buildMethodRecipe() {
            List<MethodRecipe> recipes = new ArrayList<MethodRecipe>();
            
            // CacheApiMethodRecipe must preeceed InterceptorInjectors because InterceptorInjectors can use cached api id.
            if (cacheApi) {
                recipes.add(new CacheApiMethodRecipe(pluginContext));
            }

            for (InterceptorBuilder ib : interceptorBuilders) {
                recipes.add(ib.build());
            }
            
            MethodRecipe recipe = recipes.size() == 1 ? recipes.get(0) : new MethodCookBook(recipes);
            return recipe;
        }
    }
    
    public class InterceptorBuilder {
        private String interceptorClassName;
        private MethodCondition condition;
        private String scopeName;
        private Object[] constructorArguments;
        private boolean singleton;
        
        public void interceptorClass(String interceptorClassName) {
            this.interceptorClassName = interceptorClassName;
        }
        
        public void constructorArgs(Object... args) {
            this.constructorArguments = args;
        }
        
        public void scope(String scopeName) {
            this.scopeName = scopeName;
        }

        public void singleton(boolean singleton) {
            this.singleton = singleton;
        }
        
        public void condition(MethodCondition condition) {
            this.condition = condition;
        }
        
        InterceptorInjector build() {
            InterceptorFactory factory = buildInterceptorFactory();
            InterceptorInjector injector = buildInterceptorInjector(factory);
            
            return injector;
        }

        private InterceptorInjector buildInterceptorInjector(InterceptorFactory factory) {
            InterceptorInjector injector;
            
            if (singleton) {
                injector = new SingletonInterceptorInjector(factory);
            } else {
                injector = new DefaultInterceptorInjector(factory);
            }
            
            if (condition != null) {
                injector = new ConditionalInterceptorInjector(injector, condition);
            }
            
            return injector;
        }

        private InterceptorFactory buildInterceptorFactory() {
            InterceptorFactory factory = new DefaultInterceptorFactory(pluginContext, instrumentor, traceContext, interceptorClassName, constructorArguments);
            
            if (scopeName != null) {
                factory = new ScopedInterceptorFactory(factory, instrumentor, scopeName);
            }
            
            return factory;
        }
    }
}
