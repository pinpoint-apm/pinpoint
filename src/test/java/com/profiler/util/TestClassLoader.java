package com.profiler.util;

import com.profiler.Agent;
import com.profiler.context.DefaultTrace;
import com.profiler.context.DefaultTraceContext;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.*;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.profiler.logging.LoggingUtils;
import com.profiler.modifier.Modifier;
import com.profiler.util.bindvalue.BindValueConverter;
import javassist.CannotCompileException;
import javassist.Loader;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClassLoader extends Loader {
    private final Logger logger = LoggerFactory.getLogger(TestClassLoader.class.getName());

    private ByteCodeInstrumentor instrumentor;
    private InstrumentTranslator instrumentTranslator;


    public TestClassLoader() {
        this.instrumentor = new JavaAssistByteCodeInstrumentor();
        this.instrumentTranslator = new InstrumentTranslator(this);
    }

    public void initialize() {
        addDefaultDelegateLoadingOf();
        addTranslator();
    }

    public ByteCodeInstrumentor getInstrumentor() {
        return instrumentor;
    }

    public Modifier addModifier(Modifier modifier) {
        return this.instrumentTranslator.addModifier(modifier);
    }

    private void addDefaultDelegateLoadingOf() {
        // 패키지명 필터로 바꾸던지 개선해야 될것으로 보임.
        this.delegateLoadingOf(Interceptor.class.getName());
        this.delegateLoadingOf(StaticAroundInterceptor.class.getName());
        this.delegateLoadingOf(StaticBeforeInterceptor.class.getName());
        this.delegateLoadingOf(StaticAfterInterceptor.class.getName());
        this.delegateLoadingOf(InterceptorRegistry.class.getName());
        this.delegateLoadingOf(Trace.class.getName());
        this.delegateLoadingOf(DefaultTrace.class.getName());
        this.delegateLoadingOf(MetaObject.class.getName());
        this.delegateLoadingOf(StringUtils.class.getName());
        this.delegateLoadingOf(MethodDescriptor.class.getName());
        this.delegateLoadingOf(ByteCodeMethodDescriptorSupport.class.getName());
        this.delegateLoadingOf(LoggingUtils.class.getName());
        this.delegateLoadingOf(Agent.class.getName());
        this.delegateLoadingOf(TraceContext.class.getName());
        this.delegateLoadingOf(DefaultTraceContext.class.getName());



        this.delegateLoadingOf(BindValueConverter.class.getPackage() + ".");
    }

    @Override
    protected Class loadClassByDelegation(String name) throws ClassNotFoundException {
        return super.loadClassByDelegation(name);
    }

    private void addTranslator() {
        try {
            addTranslator(instrumentor.getClassPool(), instrumentTranslator);
        } catch (NotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void runTest(String className, String methodName) throws Throwable {
        Class c = loadClass(className);
        Object o = c.newInstance();
        try {
            c.getDeclaredMethod(methodName).invoke(o);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
