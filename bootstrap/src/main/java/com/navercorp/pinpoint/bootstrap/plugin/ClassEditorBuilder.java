package com.nhn.pinpoint.bootstrap.plugin;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.MethodFilter;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;
import com.nhn.pinpoint.bootstrap.plugin.MetadataInitializationStrategy.ByConstructor;

public class ClassEditorBuilder {
    private final ByteCodeInstrumentor instrumentor;
    private final TraceContext traceContext;
    
    private final String targetClassName;

    private final List<InterceptorBuilder> interceptorBuilders = new ArrayList<InterceptorBuilder>();
    private final List<MetadataBuilder> metadataBuilders = new ArrayList<MetadataBuilder>();
    
    public ClassEditorBuilder(ByteCodeInstrumentor instrumentor, TraceContext traceContext, String targetClassName) {
        this.instrumentor = instrumentor;
        this.traceContext = traceContext;
        this.targetClassName = targetClassName;
    }

    public InterceptorBuilder intercept(String methodName, String... parameterTypes) {
        InterceptorBuilder interceptorBuilder = new InterceptorBuilder(methodName, parameterTypes, null);
        interceptorBuilders.add(interceptorBuilder);
        return interceptorBuilder;
    }
    
    public InterceptorBuilder interceptMethodsFilteredBy(MethodFilter filter) {
        InterceptorBuilder interceptorBuilder = new InterceptorBuilder(null, null, filter);
        interceptorBuilders.add(interceptorBuilder);
        return interceptorBuilder;
    }
    
    public InterceptorBuilder interceptConstructor(String... parameterTypes) {
        InterceptorBuilder interceptorBuilder = new InterceptorBuilder(null, parameterTypes, null);
        interceptorBuilders.add(interceptorBuilder);
        return interceptorBuilder;
    }
    
    public MetadataBuilder inject(Class<? extends TraceValue> metadataAccessor) {
        MetadataBuilder metadataBuilder = new MetadataBuilder(metadataAccessor);
        metadataBuilders.add(metadataBuilder);
        return metadataBuilder;
    }
    
    public ClassEditor build() {
        List<MetadataInjector> metadataInjectors = new ArrayList<MetadataInjector>(metadataBuilders.size());
        
        for (MetadataBuilder builder : metadataBuilders) {
            metadataInjectors.add(builder.build());
        }
        
        List<InterceptorInjector> interceptorInjectors = new ArrayList<InterceptorInjector>(interceptorBuilders.size());
        
        for (InterceptorBuilder builder : interceptorBuilders) {
            interceptorInjectors.add(builder.build());
        }
        
        return new BasicClassEditor(instrumentor, targetClassName, metadataInjectors, interceptorInjectors);
    }
    
    public class InterceptorBuilder {
        private final String methodName;
        private final String[] parameterTypes;
        private final MethodFilter filter;
        
        private String interceptorClassName;
        private String scopeName;
        private Object[] constructorArguments;
        private ParameterExtractorFactory parameterExtractorFactory;
        private boolean singleton;
        
        public InterceptorBuilder(String methodName, String[] parameterTypes, MethodFilter filter) {
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
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
        
        private InterceptorInjector build() {
            InterceptorFactory interceptorFactory = new DefaultInterceptorFactory(instrumentor, traceContext, interceptorClassName, constructorArguments, parameterExtractorFactory, scopeName);
            
            if (filter != null) {
                return new FilteringInterceptorInjector(filter, interceptorFactory, singleton);
            } else if (methodName != null) {
                return new DedicatedInterceptorInjector(methodName, parameterTypes, interceptorFactory);
            } else {
                return new ConstructorInterceptorInjector(parameterTypes, interceptorFactory);
            }
        }
    }
    
    public class MetadataBuilder {
        private final Class<? extends TraceValue> metadataAccessor;
        private MetadataInitializationStrategy initializationStrategy;
        
        public MetadataBuilder(Class<? extends TraceValue> metadataAccessor) {
            this.metadataAccessor = metadataAccessor;
        }
        
        public MetadataBuilder initializeWithDefaultConstructorOf(String className) {
            this.initializationStrategy = new ByConstructor(className);
            return this;
        }
        
        private MetadataInjector build() {
            return new DefaultMetadataInjector(metadataAccessor, initializationStrategy);
        }
    }
}
