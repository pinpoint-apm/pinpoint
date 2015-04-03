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

package com.navercorp.pinpoint.profiler.plugin.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassCondition;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassInstrumentation;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ConditionalClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ConditionalClassEditorSetup;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ConstructorEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.DedicatedClassEditor;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorExceptionHandler;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorProperty;
import com.navercorp.pinpoint.bootstrap.plugin.interceptor.ExecutionPoint;
import com.navercorp.pinpoint.bootstrap.plugin.interceptor.InterceptorBuilder;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.FieldSnooperInjector;
import com.navercorp.pinpoint.profiler.plugin.MetadataInitializationStrategy.ByConstructor;
import com.navercorp.pinpoint.profiler.plugin.MetadataInjector;
import com.navercorp.pinpoint.profiler.plugin.interceptor.AnnotatedInterceptorInjector;
import com.navercorp.pinpoint.profiler.plugin.interceptor.TargetAnnotatedInterceptorInjector;

public class DefaultClassEditorBuilder implements ClassEditorBuilder, ConditionalClassEditorBuilder, RecipeBuilder<ClassRecipe> {

    private final DefaultProfilerPluginContext pluginContext;
    
    private final List<ClassRecipe> recipes = new ArrayList<ClassRecipe>();
    private final List<RecipeBuilder<ClassRecipe>> recipeBuilders = new ArrayList<RecipeBuilder<ClassRecipe>>();
    
    private final ClassCondition condition;
    private final String targetClassName;

    public DefaultClassEditorBuilder(DefaultProfilerPluginContext pluginContext, String targetClassName) {
        this(pluginContext, targetClassName, null);
    }
    
    private DefaultClassEditorBuilder(DefaultProfilerPluginContext pluginContext, String targetClassName, ClassCondition condition) {
        this.pluginContext = pluginContext;
        this.targetClassName = targetClassName;
        this.condition = condition;
    }

    @Override
    public void conditional(ClassCondition condition, ConditionalClassEditorSetup describer) {
        DefaultClassEditorBuilder conditional = new DefaultClassEditorBuilder(pluginContext, targetClassName, condition);
        describer.setup(conditional);
        recipeBuilders.add(conditional);
    }
    
    @Override
    public void apply(ClassInstrumentation instrumentation) {
        recipes.add(new ClassInstrumentationRecipe(pluginContext, instrumentation));
    }

    @Override
    public void injectFieldAccessor(String fieldName) {
        FieldAccessor snooper = pluginContext.allocateFieldSnooper(fieldName);
        recipes.add(new FieldSnooperInjector(snooper, fieldName));
    }
    
    @Override
    public void injectMetadata(String name) {
        MetadataAccessor accessor = pluginContext.allocateMetadataAccessor(name);
        recipes.add(new MetadataInjector(accessor));
    }
    
    @Override
    public void injectMetadata(String name, String initialValueType) {
        MetadataAccessor accessor = pluginContext.allocateMetadataAccessor(name);
        recipes.add(new MetadataInjector(accessor, new ByConstructor(initialValueType)));
    }
    
    @Override
    public void injectInterceptor(String className, Object... constructorArgs) {
        recipeBuilders.add(new TargetAnnotatedInterceptorInjectorBuilder(className, constructorArgs));
    }

    @Override
    public MethodEditorBuilder editMethods(MethodFilter filter) {
        DefaultMethodEditorBuilder builder = new DefaultMethodEditorBuilder(filter);
        recipeBuilders.add(builder);
        return builder;
    }

    @Override
    public MethodEditorBuilder editMethod(String name, String... parameterTypeNames) {
        DefaultMethodEditorBuilder builder = new DefaultMethodEditorBuilder(name, parameterTypeNames);
        recipeBuilders.add(builder);
        return builder;
    }

    @Override
    public ConstructorEditorBuilder editConstructor(String... parameterTypeNames) {
        DefaultMethodEditorBuilder builder = new DefaultMethodEditorBuilder(parameterTypeNames);
        recipeBuilders.add(builder);
        return builder;
    }
    
    @Override
    public void weave(String aspectClassName) {
        recipes.add(new ClassWeaver(aspectClassName));
    }
    
    @Override
    public DedicatedClassEditor build() {
        ClassRecipe recipe = buildClassRecipe(); 
        return new DefaultDedicatedClassEditor(targetClassName, recipe);
    }

    private ClassRecipe buildClassRecipe() {
        List<ClassRecipe> recipes = new ArrayList<ClassRecipe>(this.recipes);
        
        for (RecipeBuilder<ClassRecipe> builder : recipeBuilders) {
            recipes.add(builder.buildRecipe());
        }
        
        if (recipes.isEmpty()) {
            throw new IllegalStateException("No class editor registered"); 
        }
        
        ClassRecipe recipe = recipes.size() == 1 ? recipes.get(0) : new ClassCookBook(recipes);
        return recipe;
    }
    
    @Override
    public ClassRecipe buildRecipe() {
        if (condition == null) {
            throw new IllegalStateException();
        }
        
        ClassRecipe recipe = buildClassRecipe();
        return new ConditionalClassRecipe(pluginContext, condition, recipe);
    }




    private class TargetAnnotatedInterceptorInjectorBuilder implements RecipeBuilder<ClassRecipe> {
        private final String interceptorClassName;
        private final Object[] constructorArguments;
        
        public TargetAnnotatedInterceptorInjectorBuilder(String interceptorClassName, Object[] constructorArguments) {
            this.interceptorClassName = interceptorClassName;
            this.constructorArguments = constructorArguments;
        }

        @Override
        public ClassRecipe buildRecipe() {
            return new TargetAnnotatedInterceptorInjector(pluginContext, interceptorClassName, constructorArguments);
        }
    }

    private class AnnotatedInterceptorInjectorBuilder implements InterceptorBuilder, RecipeBuilder<MethodRecipe> {
        private final String interceptorClassName;
        private final Object[] constructorArguments;
        
        private String groupName;
        private ExecutionPoint executionPoint;
        
        public AnnotatedInterceptorInjectorBuilder(String interceptorClassName, Object[] constructorArguments) {
            this.interceptorClassName = interceptorClassName;
            this.constructorArguments = constructorArguments;
        }
        
        @Override
        public void group(String groupName) {
            group(groupName, ExecutionPoint.ALWAYS);            
        }
        
        @Override
        public void group(String groupName, ExecutionPoint point) {
            this.groupName = groupName;
            this.executionPoint = point;
        }

        @Override
        public MethodRecipe buildRecipe() {
            return new AnnotatedInterceptorInjector(pluginContext, interceptorClassName, constructorArguments, groupName, executionPoint);
        }
    }
    
    public class DefaultMethodEditorBuilder implements MethodEditorBuilder, ConstructorEditorBuilder, RecipeBuilder<ClassRecipe> {
        private final String methodName;
        private final String[] parameterTypeNames;
        private final MethodFilter filter;
        private final List<RecipeBuilder<MethodRecipe>> recipeBuilders = new ArrayList<RecipeBuilder<MethodRecipe>>();
        private final EnumSet<MethodEditorProperty> properties = EnumSet.noneOf(MethodEditorProperty.class);
        private MethodEditorExceptionHandler exceptionHandler;

        private DefaultMethodEditorBuilder(String... parameterTypeNames) {
            this.methodName = null;
            this.parameterTypeNames = parameterTypeNames;
            this.filter = null;
        }
        
        private DefaultMethodEditorBuilder(String methodName, String... parameterTypeNames) {
            this.methodName = methodName;
            this.parameterTypeNames = parameterTypeNames;
            this.filter = null;
        }

        private DefaultMethodEditorBuilder(MethodFilter filter) {
            this.methodName = null;
            this.parameterTypeNames = null;
            this.filter = filter;
        }
        
        @Override
        public void property(MethodEditorProperty... properties) {
            this.properties.addAll(Arrays.asList(properties));
        }

        @Override
        public InterceptorBuilder injectInterceptor(String interceptorClassName, Object... constructorArguments) {
            AnnotatedInterceptorInjectorBuilder builder = new AnnotatedInterceptorInjectorBuilder(interceptorClassName, constructorArguments);
            recipeBuilders.add(builder);
            return builder;
        }
        
        @Override
        public void exceptionHandler(MethodEditorExceptionHandler handler) {
            this.exceptionHandler = handler;
        }

        @Override
        public MethodEditor buildRecipe() {
            List<MethodRecipe> recipes = buildMethodRecipe();
            MethodEditor editor = buildMethodEditor(recipes);
            
            return editor;
        }

        private MethodEditor buildMethodEditor(List<MethodRecipe> recipes) {
            MethodEditor editor;
            if (filter != null) {
                editor = new FilteringMethodEditor(filter, recipes, exceptionHandler);
            } else if (methodName != null) {
                editor = new DedicatedMethodEditor(methodName, parameterTypeNames, recipes, exceptionHandler, properties.contains(MethodEditorProperty.IGNORE_IF_NOT_EXIST));
            } else {
                editor = new ConstructorEditor(parameterTypeNames, recipes, exceptionHandler, properties.contains(MethodEditorProperty.IGNORE_IF_NOT_EXIST));
            }
            
            return editor;
        }

        private List<MethodRecipe> buildMethodRecipe() {
            if (recipeBuilders.isEmpty()) {
                // For now, a method editor without any interceptor is meaningless. 
                throw new IllegalStateException("No interceptors are defiend");
            }

            List<MethodRecipe> recipes = new ArrayList<MethodRecipe>(recipeBuilders.size());
            
            for (RecipeBuilder<MethodRecipe> builder : recipeBuilders) {
                recipes.add(builder.buildRecipe());
            }
            
            return recipes;
        }
    }
}
