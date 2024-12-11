package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.test.plugin.util.ClassPath;
import com.navercorp.pinpoint.test.plugin.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClassLoading {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public ClassLoading() {
    }

    public List<String> collectLibs(ClassLoader sourceCl) {
        List<String> result = new ArrayList<>();
        final ClassLoader termCl = ClassLoader.getSystemClassLoader().getParent();
        for (ClassLoader cl : iterateClassLoaderChain(sourceCl, termCl)) {
            final List<String> libs = extractLibrariesFromClassLoader(cl);
            if (libs != null) {
                result.addAll(libs);
                if (logger.isDebugEnabled()) {
                    logger.debug("classLoader: {}", cl);
                    for (String lib : libs) {
                        logger.debug("  -> {}", lib);
                    }
                }
            }
        }
        return result;
    }

    private Iterable<ClassLoader> iterateClassLoaderChain(ClassLoader src, ClassLoader term) {
        final List<ClassLoader> classLoaders = new ArrayList<>(8);
        ClassLoader cl = src;
        while (cl != term) {
            classLoaders.add(cl);
            if (cl == Object.class.getClassLoader()) {
                break;
            }
            cl = cl.getParent();
        }
        return classLoaders;
    }

    private List<String> extractLibrariesFromClassLoader(ClassLoader cl) {
        if (cl instanceof URLClassLoader) {
            return extractLibrariesFromURLClassLoader((URLClassLoader) cl);
        }
        if (cl == ClassLoader.getSystemClassLoader()) {
            return extractLibrariesFromSystemClassLoader();
        }
        return null;
    }

    private List<String> extractLibrariesFromURLClassLoader(URLClassLoader cl) {
        final URL[] urls = cl.getURLs();
        final List<String> paths = new ArrayList<>(urls.length);
        for (URL url : urls) {
            paths.add(ConfigResolver.toPathString(url).toString());
        }
        return paths;
    }

    private List<String> extractLibrariesFromSystemClassLoader() {
        final String classPath = System.getProperty("java.class.path");
        if (StringUtils.isEmpty(classPath)) {
            return Collections.emptyList();
        }
        final String[] paths = ClassPath.parse(classPath);
        return Arrays.asList(paths);
    }

    public List<String> filterLibs(List<String> classPaths, LibraryFilter classPathFilter) {
        final Set<String> result = new LinkedHashSet<>();
        for (String classPath : classPaths) {
            if (classPathFilter.filter(classPath)) {
                result.add(classPath);
            }
        }
        return new ArrayList<>(result);
    }
}
