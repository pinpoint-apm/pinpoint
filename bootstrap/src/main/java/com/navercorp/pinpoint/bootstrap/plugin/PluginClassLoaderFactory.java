package com.navercorp.pinpoint.bootstrap.plugin;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginClassLoaderFactory {

    private final Logger logger = Logger.getLogger(PluginClassLoaderFactory.class.getName());


    private final URL[] pluginJars;
    private final ConcurrentHashMap<ClassLoader, ClassLoader> cache = new ConcurrentHashMap<ClassLoader, ClassLoader>();
    
    public PluginClassLoaderFactory(URL[] pluginJars) {
        this.pluginJars = pluginJars;
    }
    
    public ClassLoader get(ClassLoader loader) { 
        final ClassLoader forPlugin = cache.get(loader);
        if (forPlugin != null) {
            return forPlugin;
        }
        
        final ClassLoader newInstance = new URLClassLoader(pluginJars, loader);
        final ClassLoader before = cache.putIfAbsent(loader, newInstance);
        if (before == null) {
            return newInstance;
        }
        else {
            close (newInstance);
            return before;
        }
    }

    private void close(ClassLoader classLoader) {
        if (classLoader == null) {
            return;
        }
        // after jdk 1.7
        if (classLoader instanceof Closeable) {
            try {
                ((Closeable)classLoader).close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "PluginClassLoader.close() fail. Caused:" + e.getMessage(), e);
            }
        }
    }

}
