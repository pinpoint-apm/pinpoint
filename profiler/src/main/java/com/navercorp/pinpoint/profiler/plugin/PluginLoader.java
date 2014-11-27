package com.nhn.pinpoint.profiler.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.nhn.pinpoint.exception.PinpointException;

public class PluginLoader<T> {
    private final Class<T> serviceType;
    private final ClassLoader classLoader;;
    
    public static <T> PluginLoader<T> get(Class<T> serviceType, String pluginPath) {
        URL[] jars = findJars(pluginPath);
        URLClassLoader classLoader = new URLClassLoader(jars, ClassLoader.getSystemClassLoader());
        
        return new PluginLoader<T>(serviceType, classLoader);
    }
    
    public PluginLoader(Class<T> serviceType, ClassLoader classLoader) {
        this.serviceType = serviceType;
        this.classLoader = classLoader;
    }

    public List<T> loadPlugins() {
        ServiceLoader<T> loader = ServiceLoader.load(serviceType, classLoader);
        
        Iterator<T> iterator = loader.iterator();
        List<T> plugins = new ArrayList<T>();
        
        while (iterator.hasNext()) {
            plugins.add(iterator.next());
        }
        
        return plugins;
    }
    
    public URL[] getPluginJars() {
        if (classLoader instanceof URLClassLoader) {
            return ((URLClassLoader)classLoader).getURLs();
        } else {
            return new URL[0];
        }
    }

    private static URL[] findJars(String pluginPath) {
        File file = new File(pluginPath);
        
        if (!file.exists() || !file.isDirectory()) {
            return new URL[0];
        }
        
        File[] jars = file.listFiles(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        
        
        URL[] urls = new URL[jars.length];
        
        for (int i = 0; i < jars.length; i++) {
            try {
                urls[i] = jars[i].toURI().toURL();
            } catch (MalformedURLException e) {
                throw new PinpointException("Fail to load plugin jars", e);
            }
        }
        
        return urls;
    }
}
