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

public class PluginLoader {
    public List<ProfilerPlugin> load(String pluginPath) {
        File[] jars = findJars(pluginPath);
        URLClassLoader classLoader = getClassLoader(jars);
        List<ProfilerPlugin> plugins = loadPlugins(jars, classLoader);
        
        return plugins;
    }

    private List<ProfilerPlugin> loadPlugins(File[] jars, URLClassLoader classLoader) {
        List<ProfilerPlugin> plugins = new ArrayList<ProfilerPlugin>(jars.length);
        
        ServiceLoader<ProfilerPlugin> loader = ServiceLoader.load(ProfilerPlugin.class, classLoader);
        Iterator<ProfilerPlugin> iterator = loader.iterator();
        
        while (iterator.hasNext()) {
            plugins.add(iterator.next());
        }
        
        return plugins;
    }

    private URLClassLoader getClassLoader(File[] jars) {
        URL[] urls = new URL[jars.length];
        
        for (int i = 0; i < jars.length; i++) {
            try {
                urls[i] = jars[i].toURI().toURL();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        return classLoader;
    }

    private File[] findJars(String pluginPath) {
        File file = new File(pluginPath);
        
        File[] jars = file.listFiles(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        return jars;
    }
}
