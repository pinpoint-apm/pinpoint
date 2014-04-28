package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.util.MetaObject;
import com.nhn.pinpoint.bootstrap.util.StringUtils;
import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.logging.LoggingUtils;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.context.DefaultTrace;
import com.nhn.pinpoint.profiler.context.DefaultTraceContext;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.modifier.Modifier;
import com.nhn.pinpoint.profiler.util.bindvalue.BindValueConverter;
import javassist.CannotCompileException;
import javassist.Loader;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class TestClassLoader extends Loader {
    private final Logger logger = LoggerFactory.getLogger(TestClassLoader.class.getName());

    private ByteCodeInstrumentor instrumentor;
    private InstrumentTranslator instrumentTranslator;
    private Agent agent;


    public TestClassLoader(DefaultAgent agent) {
        if (agent == null) {
            throw new NullPointerException("agent must not be null");
        }

        this.agent = agent;
        this.instrumentor = agent.getByteCodeInstrumentor();
        this.instrumentTranslator = new InstrumentTranslator(this, agent);
    }

    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    public void initialize() {
        addDefaultDelegateLoadingOf();
        addTranslator();
    }

    public Agent getAgent() {
        return agent;
    }

    public ByteCodeInstrumentor getInstrumentor() {
        return instrumentor;
    }

    public Modifier addModifier(Modifier modifier) {
        return this.instrumentTranslator.addModifier(modifier);
    }

    private void addDefaultDelegateLoadingOf() {
        this.delegateLoadingOf("com.nhn.pinpoint.bootstrap.");
        this.delegateLoadingOf("com.nhn.pinpoint.common.");
        this.delegateLoadingOf("com.nhn.pinpoint.profiler.context.");

    }

    @Override
    protected Class loadClassByDelegation(String name) throws ClassNotFoundException {
        return super.loadClassByDelegation(name);
    }

    private void addTranslator() {
        try {
            addTranslator(((JavaAssistByteCodeInstrumentor)instrumentor).getClassPool(), instrumentTranslator);
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
