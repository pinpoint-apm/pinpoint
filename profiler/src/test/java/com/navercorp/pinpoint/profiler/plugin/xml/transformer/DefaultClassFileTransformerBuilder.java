
package com.navercorp.pinpoint.profiler.plugin.xml.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import com.navercorp.pinpoint.profiler.plugin.xml.FieldInjector;
import com.navercorp.pinpoint.profiler.plugin.xml.GetterInjector;
import com.navercorp.pinpoint.profiler.plugin.xml.OverrideMethodInjector;
import com.navercorp.pinpoint.profiler.plugin.xml.interceptor.AnnotatedInterceptorInjector;
import com.navercorp.pinpoint.profiler.plugin.xml.interceptor.TargetAnnotatedInterceptorInjector;

public class DefaultClassFileTransformerBuilder implements ClassFileTransformerBuilder, ConditionalClassFileTransformerBuilder, RecipeBuilder<ClassRecipe> {

    private final ProfilerConfig profilerConfig;
    private final TraceContext traceContext;

    private final InstrumentContext pluginContext;
    private final List<ClassRecipe> recipes = new ArrayList<ClassRecipe>();

    private final List<RecipeBuilder<ClassRecipe>> recipeBuilders = new ArrayList<RecipeBuilder<ClassRecipe>>();
    private final ClassCondition condition;
    private final String targetClassName;
    private final DataSourceMonitorRegistry dataSourceMonitorRegistry;
    private final ApiMetaDataService apiMetaDataService;

    public DefaultClassFileTransformerBuilder(ProfilerConfig profilerConfig, TraceContext traceContext, DataSourceMonitorRegistry dataSourceMonitorRegistry, ApiMetaDataService apiMetaDataService, InstrumentContext pluginContext, String targetClassName) {
        this(profilerConfig, traceContext, dataSourceMonitorRegistry, apiMetaDataService, pluginContext, targetClassName, null);
    }
    
    private DefaultClassFileTransformerBuilder(ProfilerConfig profilerConfig, TraceContext traceContext, DataSourceMonitorRegistry dataSourceMonitorRegistry, ApiMetaDataService apiMetaDataService,
                                               InstrumentContext pluginContext, String targetClassName, ClassCondition condition) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (dataSourceMonitorRegistry == null) {
            throw new NullPointerException("dataSourceMonitorRegistry must not be null");
        }
        if (apiMetaDataService == null) {
            throw new NullPointerException("apiMetaDataService must not be null");
        }
        if (pluginContext == null) {
            throw new NullPointerException("pluginContext must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.traceContext = traceContext;
        this.dataSourceMonitorRegistry = dataSourceMonitorRegistry;
        this.apiMetaDataService = apiMetaDataService;

        this.pluginContext = pluginContext;
        this.targetClassName = targetClassName;
        this.condition = condition;
    }

    @Override
    public void conditional(ClassCondition condition, ConditionalClassFileTransformerSetup describer) {
        DefaultClassFileTransformerBuilder conditional = new DefaultClassFileTransformerBuilder(profilerConfig, traceContext, dataSourceMonitorRegistry, apiMetaDataService, pluginContext, targetClassName, condition);
        describer.setup(conditional);
        recipeBuilders.add(conditional);
    }
    
    @Override
    public void injectGetter(String getterTyepName, String fieldName) {
        recipes.add(new GetterInjector(getterTyepName, fieldName));
    }
    
    @Override
    public void injectField(String accessorTypeName) {
        recipes.add(new FieldInjector(accessorTypeName));
    }

    @Override
    public void overrideMethodToDelegate(String name, String... paramTypes) {
        recipes.add(new OverrideMethodInjector(name, paramTypes));
    }
    
    @Override
    public InterceptorBuilder injectInterceptor(String className, Object... constructorArgs) {
        TargetAnnotatedInterceptorInjectorBuilder builder = new TargetAnnotatedInterceptorInjectorBuilder(className, constructorArgs);
        recipeBuilders.add(builder);
        return builder;
    }

    @Override
    public MethodTransformerBuilder editMethods(MethodFilter... filters) {
        DefaultMethodEditorBuilder builder = new DefaultMethodEditorBuilder(filters);
        recipeBuilders.add(builder);
        return builder;
    }

    @Override
    public MethodTransformerBuilder editMethod(String name, String... parameterTypeNames) {
        DefaultMethodEditorBuilder builder = new DefaultMethodEditorBuilder(name, parameterTypeNames);
        recipeBuilders.add(builder);
        return builder;
    }

    @Override
    public ConstructorTransformerBuilder editConstructor(String... parameterTypeNames) {
        DefaultMethodEditorBuilder builder = new DefaultMethodEditorBuilder(parameterTypeNames);
        recipeBuilders.add(builder);
        return builder;
    }
    
    @Override
    public void weave(String aspectClassName) {
        recipes.add(new ClassWeaver(aspectClassName));
    }
    
    @Override
    public MatchableClassFileTransformer build() {
        ClassRecipe recipe = buildClassRecipe(); 
        return new DedicatedClassFileTransformer(pluginContext, targetClassName, recipe);
    }

    private ClassRecipe buildClassRecipe() {
        List<ClassRecipe> recipes = new ArrayList<ClassRecipe>(this.recipes);
        
        for (RecipeBuilder<ClassRecipe> builder : recipeBuilders) {
            recipes.add(builder.buildRecipe());
        }
        
        if (recipes.isEmpty()) {
            throw new IllegalStateException("No class transformation registered"); 
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




    private class TargetAnnotatedInterceptorInjectorBuilder implements InterceptorBuilder, RecipeBuilder<ClassRecipe> {
        private final String interceptorClassName;
        private final Object[] constructorArguments;
        
        private String scopeName;
        private ExecutionPolicy executionPoint;

        public TargetAnnotatedInterceptorInjectorBuilder(String interceptorClassName, Object[] constructorArguments) {
            this.interceptorClassName = interceptorClassName;
            this.constructorArguments = constructorArguments;
        }

        @Override
        public void setScope(String scopeName) {
            setScope(scopeName, ExecutionPolicy.BOUNDARY);
        }
        
        @Override
        public void setScope(String scopeName, ExecutionPolicy point) {
            this.scopeName = scopeName;
            this.executionPoint = point;
        }

        @Override
        public ClassRecipe buildRecipe() {
            return new TargetAnnotatedInterceptorInjector(profilerConfig, traceContext, dataSourceMonitorRegistry, apiMetaDataService, pluginContext, interceptorClassName, constructorArguments, scopeName, executionPoint);
        }
    }

    private class AnnotatedInterceptorInjectorBuilder implements InterceptorBuilder, RecipeBuilder<MethodRecipe> {
        private final String interceptorClassName;
        private final Object[] constructorArguments;
        
        private String scopeName;
        private ExecutionPolicy executionPoint;
        
        public AnnotatedInterceptorInjectorBuilder(String interceptorClassName, Object[] constructorArguments) {
            this.interceptorClassName = interceptorClassName;
            this.constructorArguments = constructorArguments;
        }
        
        @Override
        public void setScope(String scopeName) {
            setScope(scopeName, ExecutionPolicy.BOUNDARY);
        }
        
        @Override
        public void setScope(String scopeName, ExecutionPolicy point) {
            this.scopeName = scopeName;
            this.executionPoint = point;
        }

        @Override
        public MethodRecipe buildRecipe() {
            return new AnnotatedInterceptorInjector(pluginContext, interceptorClassName, constructorArguments, scopeName, executionPoint);
        }
    }
    
    public class DefaultMethodEditorBuilder implements MethodTransformerBuilder, ConstructorTransformerBuilder, RecipeBuilder<ClassRecipe> {
        private final String methodName;
        private final String[] parameterTypeNames;
        private final MethodFilter[] filters;
        private final List<RecipeBuilder<MethodRecipe>> recipeBuilders = new ArrayList<RecipeBuilder<MethodRecipe>>();
        private final EnumSet<MethodTransformerProperty> properties = EnumSet.noneOf(MethodTransformerProperty.class);
        private MethodTransformerExceptionHandler exceptionHandler;

        private DefaultMethodEditorBuilder(String... parameterTypeNames) {
            this.methodName = null;
            this.parameterTypeNames = parameterTypeNames;
            this.filters = null;
        }
        
        private DefaultMethodEditorBuilder(String methodName, String... parameterTypeNames) {
            this.methodName = methodName;
            this.parameterTypeNames = parameterTypeNames;
            this.filters = null;
        }

        private DefaultMethodEditorBuilder(MethodFilter[] filters) {
            this.methodName = null;
            this.parameterTypeNames = null;
            this.filters = filters;
        }
        
        @Override
        public void property(MethodTransformerProperty... properties) {
            this.properties.addAll(Arrays.asList(properties));
        }

        @Override
        public InterceptorBuilder injectInterceptor(String interceptorClassName, Object... constructorArguments) {
            AnnotatedInterceptorInjectorBuilder builder = new AnnotatedInterceptorInjectorBuilder(interceptorClassName, constructorArguments);
            recipeBuilders.add(builder);
            return builder;
        }
        
        @Override
        public void exceptionHandler(MethodTransformerExceptionHandler handler) {
            this.exceptionHandler = handler;
        }

        @Override
        public MethodTransformer buildRecipe() {
            List<MethodRecipe> recipes = buildMethodRecipe();
            MethodTransformer transformer = buildMethodEditor(recipes);
            
            return transformer;
        }

        private MethodTransformer buildMethodEditor(List<MethodRecipe> recipes) {
            MethodTransformer transformer;
            if (filters != null && filters.length > 0) {
                transformer = new FilteringMethodTransformer(filters, recipes, exceptionHandler);
            } else if (methodName != null) {
                transformer = new DedicatedMethodTransformer(methodName, parameterTypeNames, recipes, exceptionHandler, properties.contains(MethodTransformerProperty.IGNORE_IF_NOT_EXIST));
            } else {
                transformer = new ConstructorTransformer(parameterTypeNames, recipes, exceptionHandler, properties.contains(MethodTransformerProperty.IGNORE_IF_NOT_EXIST));
            }
            
            return transformer;
        }

        private List<MethodRecipe> buildMethodRecipe() {
            if (recipeBuilders.isEmpty()) {
                // For now, a method transformer without any interceptor is meaningless. 
                throw new IllegalStateException("No interceptors are defined");
            }

            List<MethodRecipe> recipes = new ArrayList<MethodRecipe>(recipeBuilders.size());
            
            for (RecipeBuilder<MethodRecipe> builder : recipeBuilders) {
                recipes.add(builder.buildRecipe());
            }
            
            return recipes;
        }
    }
}
