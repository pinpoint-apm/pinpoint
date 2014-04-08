package com.nhn.pinpoint.bootstrap;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * profiler lib 디렉토리의 jar의 경우 delegation하지 않고 자기 자신에게 로드하도록 함.
 * standalone java 일 경우 dead lock문제가 발생할수 있어, 자기자신이 load할 class일 경우 parent로 넘기지 않음.
 * @author emeroad
 */
public class PinpointURLClassLoader extends URLClassLoader {


    private final ClassLoader parent;

    public PinpointURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        if (parent == null) {
            throw new NullPointerException("parent must not be null");
        }
        // parent가 null인 케이스는 지원하지 않는다.
        this.parent = parent;
    }


    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class clazz = findLoadedClass(name);
        if (clazz == null) {
            if (ProfilerLibClass.onLoadClass(name)) {
                clazz = findClass(name);
            } else {
                try {
                    clazz = parent.loadClass(name);
                } catch (ClassNotFoundException e) {
                    clazz = findClass(name);
                }
            }
        }
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

}
