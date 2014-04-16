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

    private final ProfilerLibClass profilerLibClass = new ProfilerLibClass();

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
            if (onLoadClass(name)) {
                // 나한테 있어야 하는 class의 경우 그냥 로드.
                clazz = findClass(name);
            } else {
                try {
                    // 부모를 찾고.
                    clazz = parent.loadClass(name);
                } catch (ClassNotFoundException e) {
                    // class를 못찾음.
                }
                if (clazz == null) {
                    // 없으면 나한테 로드 시도.
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
