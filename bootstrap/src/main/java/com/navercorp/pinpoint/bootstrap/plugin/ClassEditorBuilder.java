package com.nhn.pinpoint.bootstrap.plugin;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.MethodFilter;
import com.nhn.pinpoint.bootstrap.plugin.MetadataInitializationStrategy.ByConstructor;

public class ClassEditorBuilder {
    private final ByteCodeInstrumentor instrumentor;
    private final TraceContext traceContext;
    
    private final List<InterceptorBuilder> interceptorBuilders = new ArrayList<InterceptorBuilder>();
    private final List<MetadataBuilder> metadataBuilders = new ArrayList<MetadataBuilder>();
    
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
    
    public InterceptorBuilder newInterceptorBuilder() {
        InterceptorBuilder interceptorBuilder = new InterceptorBuilder();
        interceptorBuilders.add(interceptorBuilder);
        return interceptorBuilder;
    }

    public MetadataBuilder newMetadataBuilder() {
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        metadataBuilders.add(metadataBuilder);
        return metadataBuilder;
    }
    
    public DedicatedClassEditor build() {
        List<MetadataInjector> metadataInjectors = new ArrayList<MetadataInjector>(metadataBuilders.size());
        
        for (MetadataBuilder builder : metadataBuilders) {
            metadataInjectors.add(builder.build());
        }
        
        List<InterceptorInjector> interceptorInjectors = new ArrayList<InterceptorInjector>(interceptorBuilders.size());
        
        for (InterceptorBuilder builder : interceptorBuilders) {
            interceptorInjectors.add(builder.build());
        }
        
        DedicatedClassEditor editor = new BasicClassEditor(targetClassName, metadataInjectors, interceptorInjectors);
        
        if (condition != null) {
            editor = new ConditionalClassEditor(condition, editor);
        }
        
        return editor;
    }
    
    public class InterceptorBuilder {
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
        
        private InterceptorInjector build() {
            InterceptorFactory interceptorFactory = new DefaultInterceptorFactory(instrumentor, traceContext, interceptorClassName, constructorArguments, scopeName);
            
            InterceptorInjector injector;
            
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
    
    public class MetadataBuilder {
        private String metadataAccessorTypeName;
        private MetadataInitializationStrategy initializationStrategy;
        
        public void inject(String metadataAccessorTypeName) {
            this.metadataAccessorTypeName = metadataAccessorTypeName;
        }
        
        public void initializeWithDefaultConstructorOf(String className) {
            this.initializationStrategy = new ByConstructor(className);
        }
        
        private MetadataInjector build() {
            return new DefaultMetadataInjector(metadataAccessorTypeName, initializationStrategy);
        }
    }
}
