package com.profiler.util;

import com.profiler.StopWatch;
import com.profiler.context.Annotation;
import com.profiler.context.Trace;
import com.profiler.interceptor.*;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.profiler.modifier.Modifier;
import com.profiler.modifier.db.ConnectionTrace;
import javassist.CannotCompileException;
import javassist.Loader;
import javassist.NotFoundException;

import java.util.logging.Logger;

public class TestClassLoader extends Loader {
    private final Logger logger = Logger.getLogger(TestClassLoader.class.getName());

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

    public Modifier addModifier(Modifier modifier){
        return this.instrumentTranslator.addModifier(modifier);
    }

    private void addDefaultDelegateLoadingOf() {
        this.delegateLoadingOf(Interceptor.class.getName());
        this.delegateLoadingOf(StaticAroundInterceptor.class.getName());
        this.delegateLoadingOf(StaticBeforeInterceptor.class.getName());
        this.delegateLoadingOf(StaticAfterInterceptor.class.getName());
        this.delegateLoadingOf(InterceptorRegistry.class.getName());
        this.delegateLoadingOf(ConnectionTrace.class.getName());
        this.delegateLoadingOf(Trace.class.getName());
        this.delegateLoadingOf(Annotation.class.getName());
        this.delegateLoadingOf(StopWatch.class.getName());
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
            c.getDeclaredMethod(methodName, null).invoke(o, null);
        }
        catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
