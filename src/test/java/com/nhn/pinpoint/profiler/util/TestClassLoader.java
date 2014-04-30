package com.nhn.pinpoint.profiler.util;

import java.io.IOException;
import java.lang.reflect.Constructor;

import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.util.MetaObject;
import com.nhn.pinpoint.bootstrap.util.StringUtils;
import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.logging.LoggingUtils;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.context.DefaultTrace;
import com.nhn.pinpoint.profiler.context.DefaultTraceContext;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.modifier.Modifier;
import com.nhn.pinpoint.profiler.util.bindvalue.BindValueConverter;
import javassist.CannotCompileException;
import javassist.Loader;
import javassist.NotFoundException;

import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class TestClassLoader extends Loader {
    private final Logger logger = LoggerFactory.getLogger(TestClassLoader.class.getName());

    private final String agentClassName;
    
    private Agent agent;
    private ByteCodeInstrumentor instrumentor;
    private InstrumentTranslator instrumentTranslator;


    public TestClassLoader(String agentClassName) {
    	this.agentClassName = agentClassName;
    }
    
    public TestClassLoader(DefaultAgent agent) {
        if (agent == null) {
            throw new NullPointerException("agent must not be null");
        }

        this.agentClassName = agent.getClass().getName();
        this.agent = agent;
        this.instrumentor = agent.getByteCodeInstrumentor();
        this.instrumentTranslator = new InstrumentTranslator(this, agent);
    }
    
    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    public void initialize() throws InitializationError {
        addDefaultDelegateLoadingOf();
//        loadAgent();
        addTranslator();
    }
    
    private void loadAgent() throws InitializationError {
		try {
			Thread.currentThread().setContextClassLoader(this);
	    	Class<?> agentClass = this.loadClass(this.agentClassName);
	    	@SuppressWarnings("unchecked")
			Constructor<? extends DefaultAgent> agentConstructor = (Constructor<? extends DefaultAgent>) agentClass.getConstructor(String.class, ProfilerConfig.class);
	    	ProfilerConfig profilerConfig = getProfilerConfig();
	    	this.agent = agentConstructor.newInstance("", profilerConfig);
	        this.instrumentor = ((DefaultAgent)agent).getByteCodeInstrumentor();
	        this.instrumentTranslator = new InstrumentTranslator(this, ((DefaultAgent)agent));
		} catch (Exception e) {
			// 퉁 치자..결국은 InitializationError 이므로...
			throw new InitializationError(e);
		}
    }
    
    private ProfilerConfig getProfilerConfig() throws InitializationError {
    	ProfilerConfig profilerConfig = new ProfilerConfig();
		
		String path = MockAgent.class.getClassLoader().getResource("pinpoint.config").getPath();
		try {
			profilerConfig.readConfigFile(path);
		} catch (IOException e) {
			throw new InitializationError("Unable to read pinpoint.config");
		}
		
		profilerConfig.setApplicationServerType(ServiceType.TEST_STAND_ALONE);
		return profilerConfig;
    }

    public Agent getAgent() {
    	if (this.agent == null) {
    		throw new IllegalStateException("TestClassLoader is not initialized.");
    	}
        return agent;
    }

    public ByteCodeInstrumentor getInstrumentor() {
    	if (this.instrumentor == null) {
    		throw new IllegalStateException("TestClassLoader is not initialized.");
    	}
        return instrumentor;
    }

    public Modifier addModifier(Modifier modifier) {
    	if (this.instrumentTranslator == null) {
    		throw new IllegalStateException("TestClassLoader is not initialized.");
    	}
        return this.instrumentTranslator.addModifier(modifier);
    }

    private void addDefaultDelegateLoadingOf() {
        this.delegateLoadingOf("com.nhn.pinpoint.bootstrap.");
        this.delegateLoadingOf("com.nhn.pinpoint.common.");
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
