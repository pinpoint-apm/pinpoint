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
    
    public ClassEditorBuilder edit(String targetClassName) {
        this.targetClassName = targetClassName;
        return this;
    }
    
    public ClassEditorBuilder when(Condition condition) {
        this.condition = condition;
        return this;
    }

    public InterceptorBuilder intercept(String methodName, String... parameterTypeNames) {
        InterceptorBuilder interceptorBuilder = new InterceptorBuilder(methodName, parameterTypeNames, null);
        interceptorBuilders.add(interceptorBuilder);
        return interceptorBuilder;
    }
    
    public InterceptorBuilder interceptMethodsFilteredBy(MethodFilter filter) {
        InterceptorBuilder interceptorBuilder = new InterceptorBuilder(null, null, filter);
        interceptorBuilders.add(interceptorBuilder);
        return interceptorBuilder;
    }
    
    public InterceptorBuilder interceptConstructor(String... parameterTypeNames) {
        InterceptorBuilder interceptorBuilder = new InterceptorBuilder(null, parameterTypeNames, null);
        interceptorBuilders.add(interceptorBuilder);
        return interceptorBuilder;
    }
    
    public MetadataBuilder inject(String metadataAccessorName) {
        MetadataBuilder metadataBuilder = new MetadataBuilder(metadataAccessorName);
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
        private final String methodName;
        private final String[] parameterTypes;
        private final MethodFilter filter;
        
        private String interceptorClassName;
        private Condition condition;
        private String scopeName;
        private Object[] constructorArguments;
        private ParameterExtractorFactory parameterExtractorFactory;
        private boolean singleton;
        
        public InterceptorBuilder(String methodName, String[] parameterTypeNames, MethodFilter filter) {
            this.methodName = methodName;
            this.parameterTypes = parameterTypeNames;
            this.filter = filter;
        }

        public InterceptorBuilder in(String scopeName) {
            this.scopeName = scopeName;
            return this;
        }

        public InterceptorBuilder with(String interceptorClassName) {
            this.interceptorClassName = interceptorClassName;
            return this;
        }
        
        public InterceptorBuilder constructedWith(Object... args) {
            this.constructorArguments = args;
            return this;
        }
        
        public InterceptorBuilder using(ParameterExtractorFactory factory) {
            this.parameterExtractorFactory = factory;
            return this;
        }
        
        public InterceptorBuilder singleton(boolean singleton) {
            this.singleton = singleton;
            return this;
        }
        
        public InterceptorBuilder when(Condition condition) {
            this.condition = condition;
            return this;
        }
        
        private InterceptorInjector build() {
            InterceptorFactory interceptorFactory = new DefaultInterceptorFactory(instrumentor, traceContext, interceptorClassName, constructorArguments, parameterExtractorFactory, scopeName);
            
            InterceptorInjector injector;
            
            if (filter != null) {
                injector = new FilteringInterceptorInjector(filter, interceptorFactory, singleton);
            } else if (methodName != null) {
                injector = new DedicatedInterceptorInjector(methodName, parameterTypes, interceptorFactory);
            } else {
                injector = new ConstructorInterceptorInjector(parameterTypes, interceptorFactory);
            }
            
            if (condition != null) {
                injector = new ConditionalInterceptorInjector(condition, injector);
            }
            
            return injector;
        }
    }
    
    public class MetadataBuilder {
        private final String metadataAccessorTypeName;
        private MetadataInitializationStrategy initializationStrategy;
        
        public MetadataBuilder(String metadataAccessorTypeName) {
            this.metadataAccessorTypeName = metadataAccessorTypeName;
        }
        
        public MetadataBuilder initializeWithDefaultConstructorOf(String className) {
            this.initializationStrategy = new ByConstructor(className);
            return this;
        }
        
        private MetadataInjector build() {
            return new DefaultMetadataInjector(metadataAccessorTypeName, initializationStrategy);
        }
    }
}
