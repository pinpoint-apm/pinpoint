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

import com.nhn.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.nhn.pinpoint.exception.PinpointException;

public class PluginLoader {
    private final URL[] jars;;
    
    public static PluginLoader get(String pluginPath) {
        URL[] jars = findJars(pluginPath);
        return new PluginLoader(jars);
    }

    public PluginLoader(URL[] jars) {
        this.jars = jars;
    }

    public List<ProfilerPlugin> loadPlugins() {
        URLClassLoader classLoader = new URLClassLoader(jars, ClassLoader.getSystemClassLoader());
        ServiceLoader<ProfilerPlugin> loader = ServiceLoader.load(ProfilerPlugin.class, classLoader);
        
        Iterator<ProfilerPlugin> iterator = loader.iterator();
        List<ProfilerPlugin> plugins = new ArrayList<ProfilerPlugin>(jars.length);
        
        while (iterator.hasNext()) {
            plugins.add(iterator.next());
        }
        
        return plugins;
    }
    
    public URL[] getPluginJars() {
        return jars;
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
