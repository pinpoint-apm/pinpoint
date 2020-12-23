package com.navercorp.pinpoint.test.plugin;

import org.tinylog.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectPluginTestVerifier {

    private static final Class<?> PluginTestVerifierHolderClass = forName("com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder");
    private static final Method getInstance = findMethod(PluginTestVerifierHolderClass, "getInstance");

    private static final Class<?> PluginTestVerifierClass = forName("com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier");
    private static final Method initialize = findMethod(PluginTestVerifierClass, "initialize", boolean.class);
    private static final Method cleanUp = findMethod(PluginTestVerifierClass, "cleanUp", boolean.class);


    private static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static Method findMethod(Class<?> clazz, String method, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(method, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    public static ReflectPluginTestVerifier getInstance() {
        try {
            Logger.info("getInstance");
            final Object holder = getInstance.invoke(PluginTestVerifierClass);
            if (holder == null) {
                return null;
            }
            return new ReflectPluginTestVerifier(holder);
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private final Object instance;

    public ReflectPluginTestVerifier(Object instance) {
        this.instance = instance;
    }

    public void initialize(boolean initializeTraceObject) {
        try {
            initialize.invoke(instance, initializeTraceObject);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void cleanUp(boolean detachTraceObject) {
        try {
            cleanUp.invoke(instance, detachTraceObject);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "ReflectPluginTestVerifierHolder{" +
                "instance=" + instance +
                '}';
    }
}
