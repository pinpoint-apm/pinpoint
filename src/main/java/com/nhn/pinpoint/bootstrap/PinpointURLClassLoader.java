package com.nhn.pinpoint.bootstrap;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * mark class loader
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
}
