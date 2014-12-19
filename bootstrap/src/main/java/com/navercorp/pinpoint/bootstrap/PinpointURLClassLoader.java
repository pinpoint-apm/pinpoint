package com.navercorp.pinpoint.bootstrap;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * PinpointURLClassLoader loads a class in the profiler lib directory and delegates to load the other classes to parent classloader
 * Dead lock could happen in case of standalone java application.
 * Don't delegate to parents classlaoder if classes are in the profiler lib directory
 *
 * @author emeroad
 */
public class PinpointURLClassLoader extends URLClassLoader {


    private final ClassLoader parent;

    private final ProfilerLibClass profilerLibClass = new ProfilerLibClass();

    public PinpointURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        if (parent == null) {
            throw new NullPointerException("parent must not be null");
        }
        this.parent = parent;
    }


    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class clazz = findLoadedClass(name);
        if (clazz == null) {
            if (onLoadClass(name)) {
                // load a class used for Pinpoint itself by this PinpointURLClassLoader
                clazz = findClass(name);
            } else {
                try {
                    // load a class by parent ClassLoader
                    clazz = parent.loadClass(name);
                } catch (ClassNotFoundException e) {
                }
                if (clazz == null) {
                    // if not found, try to load a class by this PinpointURLClassLoader
                    clazz = findClass(name);
                }
            }
        }
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    // for test
    boolean onLoadClass(String name) {
        return profilerLibClass.onLoadClass(name);
    }

}
