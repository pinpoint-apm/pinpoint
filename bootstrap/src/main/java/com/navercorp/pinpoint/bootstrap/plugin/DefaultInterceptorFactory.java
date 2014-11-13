package com.nhn.pinpoint.bootstrap.plugin;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;

import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.instrument.Scope;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.ParameterExtractor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.nhn.pinpoint.exception.PinpointException;

public class DefaultInterceptorFactory implements InterceptorFactory {
    private static final Object[] NO_ARGS = new Object[0];
    
    private final ByteCodeInstrumentor instrumentor;
    private final TraceContext traceContext;
    
    private final String interceptorClassName;
    private final Object[] providedArguments;
    
    private final ParameterExtractorFactory parameterExtractorFactory;
    private final String scopeName;
    
    
    public DefaultInterceptorFactory(ByteCodeInstrumentor instrumentor, TraceContext traceContext, String interceptorClassName, Object[] providedArguments, ParameterExtractorFactory parameterExtractorFactory, String scopeName) {
        this.instrumentor = instrumentor;
        this.traceContext = traceContext;
        this.interceptorClassName = interceptorClassName;
        this.providedArguments = providedArguments == null ? NO_ARGS : providedArguments;
        this.parameterExtractorFactory = parameterExtractorFactory;
        this.scopeName = scopeName;
    }

    @Override
    public Interceptor getInterceptor(ClassLoader classLoader, InstrumentClass target, MethodInfo targetMethod) {
        Interceptor interceptor = createInstance(traceContext, classLoader, target, targetMethod);
        
        if (scopeName != null) {
            interceptor = wrapWithScope(interceptor);
        }
        
        return interceptor;
    }

    private Interceptor createInstance(TraceContext traceContext, ClassLoader classLoader, InstrumentClass target, MethodInfo targetMethod) {
        Class<?> interceptorClass;
        try {
            interceptorClass = classLoader.loadClass(interceptorClassName);
        } catch (ClassNotFoundException e) {
            throw new PinpointException("Cannot find interceptor class: " + interceptorClassName, e);
        }
        
        if (!Interceptor.class.isAssignableFrom(interceptorClass)) {
            throw new PinpointException("Given class " + interceptorClassName + " is not implementing Interceptor");
        }
        
        Constructor<?>[] constructors = interceptorClass.getConstructors();
        Arrays.sort(constructors, CONSTRUCTOR_COMPARATOR);
        
        for (Constructor<?> constructor : constructors) {
            ConstructorResolver resolver = new ConstructorResolver(target, targetMethod, constructor);
            Object[] resolvedArguments = resolver.resolve();
            
            if (resolvedArguments != null) {
                return (Interceptor)invokeConstructor(constructor, resolvedArguments);
            }
        }
        
        throw new PinpointException("Cannot find suitable constructor for " + interceptorClassName);
    }
    
    private Object invokeConstructor(Constructor<?> constructor, Object[] arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new PinpointException("Fail to invoke constructor: " + constructor + ", arguments: " + Arrays.toString(arguments), e);
        }
    }
    
    private Interceptor wrapWithScope(Interceptor interceptor) {
        Scope scope = instrumentor.getScope(scopeName);
        
        if (interceptor instanceof SimpleAroundInterceptor) {
            return new ScopedSimpleInterceptor((SimpleAroundInterceptor)interceptor, scope);
        } else if (interceptor instanceof StaticAroundInterceptor) {
            return new ScopedStaticInterceptor((StaticAroundInterceptor)interceptor, scope);
        }
        
        throw new IllegalArgumentException("Unexpected interceptor type: " + interceptor.getClass());
    }

    
    private static final Comparator<Constructor<?>> CONSTRUCTOR_COMPARATOR = new Comparator<Constructor<?>>() {

        @Override
        public int compare(Constructor<?> o1, Constructor<?> o2) {
            return Integer.compare(o2.getParameterCount(), o1.getParameterCount());
        }
        
    };
    
    private class ConstructorResolver {
        private final InstrumentClass target;
        private final MethodInfo targetMethod;
        
        private final Constructor<?> constructor;
        
        private int argumentIndex = 0;
        
        
        public ConstructorResolver(InstrumentClass target, MethodInfo targetMethod, Constructor<?> constructor) {
            this.target = target;
            this.targetMethod = targetMethod;
            this.constructor = constructor;
        }

        public Object[] resolve() {
            Class<?>[] types = constructor.getParameterTypes();
            int length = types.length;
            Object[] arguments = new Object[length];
            
            for (int i = 0; i < length; i++) {
                Class<?> type = types[i];
                Option<Object> resolved = resolveArgument(type);
                
                if (!resolved.hasValue()) {
                    return null;
                }
                
                arguments[i] = resolved.getValue();
            }
            
            if (argumentIndex != providedArguments.length) {
                return null;
            }
            
            return arguments;
        }
        
        private Option<Object> resolveArgument(Class<?> type) {
            Object result = resolvePinpointObject(type);
            
            if (result != null) {
                return Option.withValue(result);
            }
            
            if (providedArguments.length >= argumentIndex + 1) {
                Object candidate = providedArguments[argumentIndex];
                argumentIndex++;
                
                if (type.isPrimitive()) {
                    if (candidate == null) {
                        return Option.<Object>empty();
                    }
                    
                    if (TypeUtils.getWrapperOf(type) == candidate.getClass()) {
                        return Option.withValue(candidate); 
                    }
                } else {
                    if (type.isAssignableFrom(candidate.getClass())) {
                        return Option.withValue(candidate);
                    }
                }
            }
            
            return Option.<Object>empty();
        }
        
        private Object resolvePinpointObject(Class<?> type) {
            if (type == TraceContext.class) {
                return traceContext;
            } else if (type == MethodDescriptor.class) {
                return targetMethod.getDescriptor();
            } else if (type == ParameterExtractor.class) {
                if (parameterExtractorFactory == null) {
                    return null;
                }
                
                return parameterExtractorFactory.get(target, targetMethod);
            } else if (type == ByteCodeInstrumentor.class) {
                return instrumentor;
            }
            
            return null;
        }
    }
}
