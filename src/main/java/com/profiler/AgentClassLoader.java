package com.profiler;

import com.profiler.logging.LoggerBinder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 *
 */
public class AgentClassLoader {

    private URLClassLoader classLoader;
    private String bootClass = "com.profiler.AgentBootStrap";
    private String bootMethod = "boot";
    private Object agentBootStrap;

    public AgentClassLoader(URL[] urls) {
        ClassLoader classLoader = AgentClassLoader.class.getClassLoader();
        this.classLoader = new URLClassLoader(urls, classLoader);
    }

    public void setBootClass(String bootClass) {
        this.bootClass = bootClass;
    }

    public void setBootMethod(String bootMethod) {
        this.bootMethod = bootMethod;
    }
    public void test() {
        URL resource = this.classLoader.getResource("/log4j.xml");
        System.out.println("log4j.xml=" + resource);
        URL resource1 = this.classLoader.getResource("/nhn_source/hippo_project/deploy/agent/agentlib/lib/log4j.xml");
        System.out.println("log4j.xml=" + resource1);

        URL[] aaa = this.classLoader.getURLs();
        for (URL url : aaa) {
            System.out.println("" + url);
        }

        URL resource2 = classLoader.getResource(".");
        System.out.println("resource2:" + resource2);
        try {
            Enumeration<URL> resources = this.classLoader.getResources(".");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                System.out.println("" + url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void boot() {
        try{
            Class<?> bootStrap = this.classLoader.loadClass(bootClass);
            agentBootStrap = bootStrap.newInstance();
            invoke(bootStrap, this.bootMethod);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("boot class not found. Caused:" + e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new RuntimeException("boot create fail. Caused:" + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("boot method invoke fail. Caused:" + e.getMessage(), e);
        }
    }

    private Object invoke(Class<?> clazz, String method) {
        Method bootMethod = null;
        try {
            bootMethod = clazz.getDeclaredMethod(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("boot method not found. Caused:" + e.getMessage(), e);
        }

        final Thread currentThread = Thread.currentThread();
        ClassLoader before = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(this.classLoader);
        try {
            return bootMethod.invoke(agentBootStrap);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(this.bootMethod + "() fail.  Caused:" + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("boot method invoke fail. Caused:" + e.getMessage(), e);
        }
        finally {
            currentThread.setContextClassLoader(before);
        }
    }

    public LoggerBinder initializeLoggerBinder() {
        return (LoggerBinder) invoke(agentBootStrap.getClass(), "initializeLoggerBinder");
    }
}
