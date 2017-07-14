/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.classloading;

import com.navercorp.pinpoint.common.util.jsr166.ConcurrentWeakHashMap;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.instrument.BootstrapPackage;
import com.navercorp.pinpoint.profiler.instrument.classreading.SimpleClassMetadata;
import com.navercorp.pinpoint.profiler.instrument.classreading.SimpleClassMetadataReader;
import com.navercorp.pinpoint.profiler.plugin.ClassLoadingChecker;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import com.navercorp.pinpoint.profiler.util.ExtensionFilter;
import com.navercorp.pinpoint.profiler.util.FileBinary;
import com.navercorp.pinpoint.profiler.util.JarReader;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class PlainClassLoaderHandler implements ClassInjector {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final Method DEFINE_CLASS;
    private final JarReader pluginJarReader;

    private final BootstrapPackage bootstrapPackage = new BootstrapPackage();

    // TODO remove static field
    private static final ConcurrentMap<ClassLoader, ClassLoaderAttachment> classLoaderAttachment = new ConcurrentWeakHashMap<ClassLoader, ClassLoaderAttachment>();

    static {
        try {
            DEFINE_CLASS = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            DEFINE_CLASS.setAccessible(true);
        } catch (Exception e) {
            throw new PinpointException("Cannot access ClassLoader.defineClass(String, byte[], int, int)", e);
        }
    }

    private final PluginConfig pluginConfig;

    public PlainClassLoaderHandler(PluginConfig pluginConfig) {
        if (pluginConfig == null) {
            throw new NullPointerException("pluginConfig must not be null");
        }
        this.pluginConfig = pluginConfig;
        this.pluginJarReader = new JarReader(pluginConfig.getPluginJarFile());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        try {
            if (bootstrapPackage.isBootstrapPackage(className)) {
                ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                return loadClass(systemClassLoader, className);
            }
            if (!isPluginPackage(className)) {
                return loadClass(classLoader, className);
            }
            return (Class<T>) injectClass0(classLoader, className);
        } catch (Exception e) {
            logger.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new PinpointException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader targetClassLoader, String classPath) {
        try {
            String name = JavaAssistUtils.jvmNameToJavaName(classPath);
            if (bootstrapPackage.isBootstrapPackage(name)) {
                ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                if (systemClassLoader != null) {
                    return systemClassLoader.getResourceAsStream(classPath);
                }
                return null;
            }
            if (!isPluginPackage(name)) {
                return targetClassLoader.getResourceAsStream(classPath);
            }
            final int fileExtensionPosition = name.lastIndexOf(".class");
            if (fileExtensionPosition != -1) {
                name = name.substring(0, fileExtensionPosition);
            }

            final InputStream inputStream = getInputStream(targetClassLoader, name);
            if (inputStream == null) {
                if (logger.isInfoEnabled()) {
                    logger.info("can not find resource : {} {} ", classPath, pluginConfig.getPluginJarURLExternalForm());
                }
                // fallback
                return targetClassLoader.getResourceAsStream(classPath);
            }
            return inputStream;
        } catch (Exception e) {
            logger.warn("Failed to load plugin resource as stream {} with classLoader {}", classPath, targetClassLoader, e);
            return null;
        }
    }

    private boolean isPluginPackage(String className) {
        return pluginConfig.getPluginPackageFilter().accept(className);
    }



    private Class<?> injectClass0(ClassLoader classLoader, String className) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (isDebug) {
            logger.debug("Inject class className:{} cl:{}", className, classLoader);
        }
        final String pluginJarPath = pluginConfig.getPluginJarURLExternalForm();
        final ClassLoaderAttachment attachment = getClassLoaderAttachment(classLoader, pluginJarPath);
        final Class<?> findClazz = attachment.getClass(className);
        if (findClazz == null) {
            if (logger.isInfoEnabled()) {
                logger.info("can not find class : {} {} ", className, pluginConfig.getPluginJarURLExternalForm());
            }
            // fallback
            return loadClass(classLoader, className);
        }
        return findClazz;

    }

    private InputStream getInputStream(ClassLoader classLoader, String classPath) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (isDebug) {
            logger.debug("Get input stream className:{} cl:{}", classPath, classLoader);

        }
        final String pluginJarPath = pluginConfig.getPluginJarURLExternalForm();
        final ClassLoaderAttachment attachment = getClassLoaderAttachment(classLoader, pluginJarPath);
        try {
            return pluginJarReader.getInputStream(classPath);
        } catch(Exception ex) {
            if (isDebug) {
                logger.debug("Failed to read plugin jar: {}", pluginConfig.getPluginJarURLExternalForm(), ex);
            }
        }

        // not found.
        return null;
    }

    private ClassLoaderAttachment getClassLoaderAttachment(ClassLoader classLoader, final String pluginJarPath) {
        final ClassLoaderAttachment attachment = getClassLoaderAttachment(classLoader);

//        this order is thread safe ?
//        final Class<?> alreadyExist = attachment.getClass(className);
//        if (alreadyExist != null) {
//            return alreadyExist;
//        }

        final PluginLock pluginLock = attachment.getPluginLock(pluginJarPath);
        synchronized (pluginLock) {
            if (!pluginLock.isLoaded()) {
                pluginLock.setLoaded();
                defineJarClass(classLoader, attachment);
            }
        }

        return attachment;
    }

    private ClassLoaderAttachment getClassLoaderAttachment(ClassLoader classLoader) {

        final ClassLoaderAttachment exist = classLoaderAttachment.get(classLoader);
        if (exist != null) {
            return exist;
        }
        final ClassLoaderAttachment newInfo = new ClassLoaderAttachment();
        final ClassLoaderAttachment old = classLoaderAttachment.putIfAbsent(classLoader, newInfo);
        if (old != null) {
            return old;
        }
        return newInfo;
    }


    private <T> Class<T> loadClass(ClassLoader classLoader, String className) {
        try {
            if (isDebug) {
                logger.debug("loadClass:{}", className);
            }
            return (Class<T>) classLoader.loadClass(className);

        } catch (ClassNotFoundException ex) {
            if (isDebug) {
                logger.debug("ClassNotFound {} cl:{}", ex.getMessage(), classLoader);
            }
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private void defineJarClass(ClassLoader classLoader, ClassLoaderAttachment attachment) {
        if (isDebug) {
            logger.debug("define Jar:{}", pluginConfig.getPluginJar());
        }

        List<FileBinary> fileBinaryList = readJar();

        Map<String, SimpleClassMetadata> classEntryMap = parse(fileBinaryList);

        for (Map.Entry<String, SimpleClassMetadata> entry : classEntryMap.entrySet()) {

            final SimpleClassMetadata classMetadata = entry.getValue();
            ClassLoadingChecker classLoadingChecker = new ClassLoadingChecker();
            classLoadingChecker.isFirstLoad(classMetadata.getClassName());
            define0(classLoader, attachment, classMetadata, classEntryMap, classLoadingChecker);
        }
    }

    private List<FileBinary> readJar() {
        try {
            return pluginJarReader.read(ExtensionFilter.CLASS_FILTER);
        } catch (IOException ex) {
            throw new RuntimeException(pluginConfig.getPluginJarURLExternalForm() + " read fail." + ex.getMessage(), ex);
        }
    }

    private Map<String, SimpleClassMetadata> parse(List<FileBinary> fileBinaryList) {
        Map<String, SimpleClassMetadata> parseMap = new HashMap<String, SimpleClassMetadata>();
        for (FileBinary fileBinary : fileBinaryList) {
            SimpleClassMetadata classNode = parseClass(fileBinary);
            parseMap.put(classNode.getClassName(), classNode);
        }
        return parseMap;
    }

    private SimpleClassMetadata parseClass(FileBinary fileBinary) {
        byte[] fileBinaryArray = fileBinary.getFileBinary();
        SimpleClassMetadata classMetadata = SimpleClassMetadataReader.readSimpleClassMetadata(fileBinaryArray);
        return classMetadata;
    }

    private void define0(ClassLoader classLoader, ClassLoaderAttachment attachment, SimpleClassMetadata currentClass, Map<String, SimpleClassMetadata> classMetaMap, ClassLoadingChecker classLoadingChecker) {
        if ("java.lang.Object".equals(currentClass.getClassName())) {
            return;
        }
        if (attachment.containsClass(currentClass.getClassName())) {
            return;
        }


        final String superName = currentClass.getSuperClassName();
        if (isDebug) {
            logger.debug("className:{} super:{}", currentClass.getClassName(), superName);
        }
        if (!"java.lang.Object".equals(superName)) {
            if (!isSkipClass(superName, classLoadingChecker)) {
                SimpleClassMetadata superClassBinary = classMetaMap.get(superName);
                if (isDebug) {
                    logger.debug("superClass dependency define super:{} ori:{}", superClassBinary.getClassName(), currentClass.getClassName());
                }
                define0(classLoader, attachment, superClassBinary, classMetaMap, classLoadingChecker);

            }
        }

        final List<String> interfaceList = currentClass.getInterfaceNames();
        for (String interfaceName : interfaceList) {
            if (!isSkipClass(interfaceName, classLoadingChecker)) {
                SimpleClassMetadata interfaceClassBinary = classMetaMap.get(interfaceName);
                if (isDebug) {
                    logger.debug("interface dependency define interface:{} ori:{}", interfaceClassBinary.getClassName(), interfaceClassBinary.getClassName());
                }
                define0(classLoader, attachment, interfaceClassBinary, classMetaMap, classLoadingChecker);
            }
        }

        final Class<?> clazz = defineClass(classLoader, currentClass);
        attachment.putClass(currentClass.getClassName(), clazz);
    }

    private Class<?> defineClass(ClassLoader classLoader, SimpleClassMetadata classMetadata) {
        classLoader = getClassLoader(classLoader);
        if (isDebug) {
            logger.debug("define class:{} cl:{}", classMetadata.getClassName(), classLoader);
        }
        // for debug
        byte[] classBytes = classMetadata.getClassBinary();
        final Integer offset = 0;
        final Integer length = classBytes.length;
        try {
            return (Class<?>) DEFINE_CLASS.invoke(classLoader, classMetadata.getClassName(), classBytes, offset, length);
        } catch (IllegalAccessException e) {
            throw handleDefineClassFail(e, classLoader, classMetadata);
        } catch (InvocationTargetException e) {
            throw handleDefineClassFail(e, classLoader, classMetadata);
        }
    }

    private RuntimeException handleDefineClassFail(Throwable throwable, ClassLoader classLoader, SimpleClassMetadata classMetadata) {

        logger.warn("{} define fail classMetadata:{} cl:{} Caused by:{}", classMetadata.getClassName(), classMetadata, classLoader, throwable.getMessage(), throwable);

        return new RuntimeException(classMetadata.getClassName() + " define fail Caused by:" + throwable.getMessage(), throwable);
    }


    private boolean isSkipClass(final String className, final ClassLoadingChecker classLoadingChecker) {
        if (!isPluginPackage(className)) {
            if (isDebug) {
                logger.debug("PluginFilter skip class:{}", className);
            }
            return true;
        }
        if (!classLoadingChecker.isFirstLoad(className)) {
            if (isDebug) {
                logger.debug("skip already loaded class:{}", className);
            }
            return true;
        }

        return false;
    }

    private class ClassLoaderAttachment {

        private final ConcurrentMap<String, PluginLock> pluginLock = new ConcurrentHashMap<String, PluginLock>();

        private final ConcurrentMap<String, Class<?>> classCache = new ConcurrentHashMap<String, Class<?>>();

        public PluginLock getPluginLock(String jarFile) {
            final PluginLock exist = this.pluginLock.get(jarFile);
            if (exist != null) {
                return exist;
            }

            final PluginLock newPluginLock = new PluginLock();
            final PluginLock old = this.pluginLock.putIfAbsent(jarFile, newPluginLock);
            if (old != null) {
                return old;
            }
            return newPluginLock;
        }

        public void putClass(String className, Class<?> clazz) {
            final Class<?> duplicatedClass = this.classCache.putIfAbsent(className, clazz);
            if (duplicatedClass != null) {
                if (logger.isWarnEnabled()) {
                    logger.warn("duplicated pluginClass {}", className);
                }
            }
        }

        public Class<?> getClass(String className) {
            return this.classCache.get(className);
        }

        public boolean containsClass(String className) {
            return this.classCache.containsKey(className);
        }
    }

    private static class PluginLock {

        private boolean loaded = false;

        public boolean isLoaded() {
            return this.loaded;
        }

        public void setLoaded() {
            this.loaded = true;
        }

    }

    private static ClassLoader getClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }

}