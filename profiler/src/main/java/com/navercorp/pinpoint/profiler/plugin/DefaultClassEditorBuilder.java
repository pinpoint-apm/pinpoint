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

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.plugin.FieldSnooper;
import com.navercorp.pinpoint.bootstrap.plugin.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassCondition;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassRecipe;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ConstructorEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.DedicatedClassEditor;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorBuilder;
import com.navercorp.pinpoint.profiler.plugin.MetadataInitializationStrategy.ByConstructor;

public class DefaultClassEditorBuilder implements ClassEditorBuilder {
    private final ProfilerPluginContext pluginContext;
    
    private final List<ClassRecipe> recipes = new ArrayList<ClassRecipe>();
    private final List<RecipeBuilder<ClassRecipe>> recipeBuilders = new ArrayList<RecipeBuilder<ClassRecipe>>();
    
    private String targetClassName;
    private ClassCondition condition;

    public DefaultClassEditorBuilder(ProfilerPluginContext pluginContext) {
        this.pluginContext = pluginContext;
    }

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder#target(java.lang.String)
     */
    @Override
    public void target(String targetClassName) {
        this.targetClassName = targetClassName;
    }
    
    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder#condition(com.navercorp.pinpoint.bootstrap.plugin.editor.ClassCondition)
     */
    @Override
    public void condition(ClassCondition condition) {
        this.condition = condition;
    }
    
    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder#injectFieldSnooper(java.lang.String)
     */
    @Override
    public void injectFieldSnooper(String fieldName) {
        FieldSnooper snooper = pluginContext.allocateFieldSnooper(fieldName);
        recipes.add(new FieldSnooperInjector(snooper, fieldName));
    }
    
    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder#injectMetadata(java.lang.String)
     */
    @Override
    public void injectMetadata(String name) {
        MetadataAccessor accessor = pluginContext.allocateMetadataAccessor(name);
        recipes.add(new MetadataInjector(accessor));
    }
    
    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder#injectMetadata(java.lang.String, java.lang.String)
     */
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

    public DedicatedClassEditor build(TraceContext context, ByteCodeInstrumentor instrumentor) {
        ClassRecipe recipe = buildClassRecipe(context, instrumentor); 
        DedicatedClassEditor editor = buildClassEditor(recipe);
        
        return editor;
    }

    private DedicatedClassEditor buildClassEditor(ClassRecipe recipe) {
        DedicatedClassEditor editor = new DefaultDedicatedClassEditor(targetClassName, recipe);
        
        if (condition != null) {
            editor = new ConditionalClassEditor(condition, editor);
        }
        return editor;
    }

    private ClassRecipe buildClassRecipe(TraceContext context, ByteCodeInstrumentor instrumentor) {
        List<ClassRecipe> recipes = new ArrayList<ClassRecipe>(this.recipes);
        
        for (RecipeBuilder<ClassRecipe> builder : recipeBuilders) {
            recipes.add(builder.build(context, instrumentor));
        }
        
        if (recipes.isEmpty()) {
            throw new IllegalStateException("No class editor registered"); 
        }
        
        ClassRecipe recipe = recipes.size() == 1 ? recipes.get(0) : new ClassCookBook(recipes);
        return recipe;
    }
    
    private interface RecipeBuilder<T> {
        public T build(TraceContext context, ByteCodeInstrumentor instrumentor);
    }

    private class TargetAnnotatedInterceptorInjectorBuilder implements RecipeBuilder<ClassRecipe> {
        private final String interceptorClassName;
        private final Object[] constructorArguments;
        
        public TargetAnnotatedInterceptorInjectorBuilder(String interceptorClassName, Object[] constructorArguments) {
            this.interceptorClassName = interceptorClassName;
            this.constructorArguments = constructorArguments;
        }

        @Override
        public ClassRecipe build(TraceContext context, ByteCodeInstrumentor instrumentor) {
            return new TargetAnnotatedInterceptorInjector(context, pluginContext, instrumentor, interceptorClassName, constructorArguments);
        }
    }

    private class AnnotatedInterceptorInjectorBuilder implements RecipeBuilder<MethodRecipe> {
        private final String interceptorClassName;
        private final Object[] constructorArguments;
        
        public AnnotatedInterceptorInjectorBuilder(String interceptorClassName, Object[] constructorArguments) {
            this.interceptorClassName = interceptorClassName;
            this.constructorArguments = constructorArguments;
        }

        @Override
        public MethodRecipe build(TraceContext context, ByteCodeInstrumentor instrumentor) {
            return new AnnotatedInterceptorInjector(context, pluginContext, instrumentor, interceptorClassName, constructorArguments);
        }
    }
    
    public class DefaultMethodEditorBuilder implements MethodEditorBuilder, ConstructorEditorBuilder, RecipeBuilder<ClassRecipe> {
        private final String methodName;
        private final String[] parameterTypeNames;
        private final MethodFilter filter;
        private ClassCondition condition;
        private List<RecipeBuilder<MethodRecipe>> recipeBuilders = new ArrayList<RecipeBuilder<MethodRecipe>>();

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
        public void condition(ClassCondition condition) {
            this.condition = condition;
        }
        
        @Override
        public void injectInterceptor(String interceptorClassName, Object... constructorArguments) {
            recipeBuilders.add(new AnnotatedInterceptorInjectorBuilder(interceptorClassName, constructorArguments));
        }
        
        public MethodEditor build(TraceContext context, ByteCodeInstrumentor instrumentor) {
            MethodRecipe recipe = buildMethodRecipe(context, instrumentor);
            MethodEditor editor = buildMethodEditor(recipe);
            
            return editor;
        }

        private MethodEditor buildMethodEditor(MethodRecipe recipe) {
            MethodEditor editor;
            if (filter != null) {
                editor = new FilteringMethodEditor(filter, recipe);
            } else if (methodName != null) {
                editor = new DedicatedMethodEditor(methodName, parameterTypeNames, recipe);
            } else {
                editor = new ConstructorEditor(parameterTypeNames, recipe);
            }
            
            if (condition != null) {
                editor = new ConditionalMethodEditor(condition, editor);
            }
            return editor;
        }

        private MethodRecipe buildMethodRecipe(TraceContext context, ByteCodeInstrumentor instrumentor) {
            if (recipeBuilders.isEmpty()) {
                // For now, a method editor without any interceptor is meaningless. 
                throw new IllegalStateException("No interceptors are defiend");
            }

            if (recipeBuilders.size() == 1) {
                return recipeBuilders.get(0).build(context, instrumentor);
            }

            List<MethodRecipe> recipes = new ArrayList<MethodRecipe>(recipeBuilders.size());
            
            for (RecipeBuilder<MethodRecipe> builder : recipeBuilders) {
                recipes.add(builder.build(context, instrumentor));
            }
            
            return new MethodCookBook(recipes);
        }
    }
}
