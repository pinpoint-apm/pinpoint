package com.nhn.pinpoint.bootstrap;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;

/**
 * @author emeroad
 */
public class AgentClassLoader {

    private URLClassLoader classLoader;

    private String bootClass;

    private Agent agentBootStrap;
    private ContextClassLoaderExecuteTemplate executeTemplate;

    public AgentClassLoader(URL[] urls) {
        if (urls == null) {
            throw new NullPointerException("urls");
        }

        ClassLoader bootStrapClassLoader = AgentClassLoader.class.getClassLoader();
        this.classLoader = new PinpointURLClassLoader(urls, bootStrapClassLoader);

        this.executeTemplate = new ContextClassLoaderExecuteTemplate(classLoader);
    }

    public void setBootClass(String bootClass) {
        this.bootClass = bootClass;
    }

    public void boot(final String agentArgs, final Instrumentation instrumentation, final ProfilerConfig profilerConfig) {

        final Class<?> bootStrapClazz = getBootStrapClass();

        agentBootStrap = (Agent) executeTemplate.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    Constructor<?> constructor = bootStrapClazz.getConstructor(String.class, Instrumentation.class, ProfilerConfig.class);
                    return constructor.newInstance(agentArgs, instrumentation, profilerConfig);
                } catch (InstantiationException e) {
                    throw new BootStrapException("boot create fail. Caused:" + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new BootStrapException("boot method invoke fail. Caused:" + e.getMessage(), e);
                }
            }
        });
    }


    private Class<?> getBootStrapClass() {
        try {
            return this.classLoader.loadClass(bootClass);
        } catch (ClassNotFoundException e) {
            throw new BootStrapException("boot class not found. Caused:" + e.getMessage(), e);
        }
    }

    @Deprecated
    public Object initializeLoggerBinder() {
        if (agentBootStrap != null) {
            return reflectionInvoke(this.agentBootStrap, "initializeLogger", null, null);
        }
        return null;
    }

    private Object reflectionInvoke(Object target, String method, Class[] type, final Object[] args) {
        final Method findMethod = findMethod(target.getClass(), method, type);
        return executeTemplate.execute(new Callable() {
            @Override
            public Object call() {
                try {
                    return findMethod.invoke(agentBootStrap, args);
                } catch (InvocationTargetException e) {
                    throw new BootStrapException(findMethod.getName() + "() fail.  Caused:" + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new BootStrapException("boot method invoke fail. Caused:" + e.getMessage(), e);
                }
            }
        });

    }

    private Method findMethod(Class<?> clazz, String method, Class[] type) {
        try {
            return clazz.getDeclaredMethod(method, type);
        } catch (NoSuchMethodException e) {
            throw new BootStrapException("(" + method + ") boot method not found. Caused:" + e.getMessage(), e);
        }
    }

}
