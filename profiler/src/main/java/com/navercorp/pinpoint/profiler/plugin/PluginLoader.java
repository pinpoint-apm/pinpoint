package com.navercorp.pinpoint.profiler.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.navercorp.pinpoint.bootstrap.plugin.PluginClassLoader;
import com.navercorp.pinpoint.exception.PinpointException;

public class PluginLoader<T> {

    public static final URL[] EMPTY_URL = new URL[0];
    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();

    private final Class<T> serviceType;
    private final ClassLoader classLoader;;
    
    public static <T> PluginLoader<T> get(Class<T> serviceType, String pluginPath) {
        URL[] jars = findJars(pluginPath);
        URLClassLoader classLoader = createPluginClassLoader(jars, ClassLoader.getSystemClassLoader());
        
        return new PluginLoader<T>(serviceType, classLoader);
    }

    private static PluginClassLoader createPluginClassLoader(final URL[] urls, final ClassLoader parent) {
        if (SECURITY_MANAGER != null) {
            return AccessController.doPrivileged(new PrivilegedAction<PluginClassLoader>() {
                public PluginClassLoader run() {
                    return new PluginClassLoader(urls, parent);
                }
            });
        } else {
            return new PluginClassLoader(urls, parent);
        }
    }
    
    public PluginLoader(Class<T> serviceType, ClassLoader classLoader) {
        this.serviceType = serviceType;
        this.classLoader = classLoader;
    }

    public List<T> loadPlugins() {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceType, classLoader);
        
        List<T> plugins = new ArrayList<T>();
        for (T plugin : serviceLoader) {
            plugins.add(plugin);
        }
        return plugins;
    }
    
    public URL[] getPluginJars() {
        if (classLoader instanceof URLClassLoader) {
            return ((URLClassLoader)classLoader).getURLs();
        } else {
            return EMPTY_URL;
        }
    }

    private static URL[] findJars(String pluginPath) {
        final File file = new File(pluginPath);
        
        if (!file.exists() || !file.isDirectory()) {
            return EMPTY_URL;
        }
        
        final File[] jars = file.listFiles(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (jars == null || jars.length == 0) {
            return EMPTY_URL;
        }
        
        final URL[] urls = new URL[jars.length];
        
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
