package com.nhn.pinpoint.bootstrap.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;

public class PluginClassLoaderFactory {
    private final URL[] pluginJars;
    private final ConcurrentHashMap<ClassLoader, ClassLoader> cache = new ConcurrentHashMap<ClassLoader, ClassLoader>();
    
    public PluginClassLoaderFactory(URL[] pluginJars) {
        this.pluginJars = pluginJars;
    }
    
    public ClassLoader get(ClassLoader loader) { 
        ClassLoader forPlugin = cache.get(loader);
        
        if (forPlugin != null) {
            return forPlugin;
        }
        
        ClassLoader newInstance = new URLClassLoader(pluginJars, loader);
        ClassLoader inCache = cache.putIfAbsent(loader, newInstance);
        
        return inCache == null ? newInstance : inCache;
    }

}
