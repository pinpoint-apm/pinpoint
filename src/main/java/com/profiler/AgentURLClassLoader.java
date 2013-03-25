package com.profiler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 */
public class AgentURLClassLoader {

    private ClassPathResolver classPathResolver = new ClassPathResolver(null);
    private URLClassLoader classLoader;
    private String bootClass = "com.profiler.AgentBootStrap";
    private String bootMethod = "boot";

    public AgentURLClassLoader(URL[] urls) {
        ClassLoader classLoader = AgentURLClassLoader.class.getClassLoader();
        this.classLoader = new URLClassLoader(urls, classLoader);
    }

    public void setBootClass(String bootClass) {
        this.bootClass = bootClass;
    }

    public void setBootMethod(String bootMethod) {
        this.bootMethod = bootMethod;
    }


    public void boot() {
        try{
            Class<?> bootStrap = this.classLoader.loadClass(bootClass);
            Object agentBootStrap = bootStrap.newInstance();

            Method bootMethod =  bootStrap.getDeclaredMethod(this.bootMethod);

            Thread currentThread = Thread.currentThread();
            ClassLoader before = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(this.classLoader);
            try {
                bootMethod.invoke(agentBootStrap);
            } finally {
                currentThread.setContextClassLoader(before);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("boot class not found. Caused:" + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("boot method not found. Caused:" + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(this.bootMethod + "() fail.  Caused:" + e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new RuntimeException("boot create fail. Caused:" + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("boot method invoke fail. Caused:" + e.getMessage(), e);
        }

    }
}
