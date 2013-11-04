package com.nhn.pinpoint.bootstrap;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * mark class loader
 * @author emeroad
 */
public class PinpointURLClassLoader extends URLClassLoader {
    public PinpointURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public PinpointURLClassLoader(URL[] urls) {
        super(urls);
    }

    public PinpointURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }


    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // classLoading문제시에 좀더 쉽게 찾을수 있도록 override
        return super.loadClass(name);
    }

}
