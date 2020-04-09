/*
 * Copyright 2016 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.test.classloader;

import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * copy & modify javassist.Loader
 *
 * @author Woonduk Kang(emeroad)
 */
public class TransformClassLoader extends ClassLoader {

    private final Logger logger = Logger.getLogger(TransformClassLoader.class.getName());

    private final ConcurrentMap<String, Object> lockMap = new ConcurrentHashMap<String, Object>();

    private final Set<String> notDefinedClass = new CopyOnWriteArraySet<String>();
    private final List<String> notDefinedPackages = new CopyOnWriteArrayList<String>();

    private final static ProtectionDomain DEFAULT_DOMAIN = (ProtectionDomain) AccessController.doPrivileged(new PrivilegedAction() {
        public Object run() {
            return TransformClassLoader.class.getProtectionDomain();
        }
    });

    private Translator translator;
    private ProtectionDomain domain;

    /**
     * Specifies the algorithm of class loading.
     * <p>
     * <p>This class loader uses the parent class loader for
     * <code>java.*</code> and <code>javax.*</code> classes.
     * If this variable <code>doDelegation</code>
     * is <code>false</code>, this class loader does not delegate those
     * classes to the parent class loader.
     * <p>
     * <p>The default value is <code>true</code>.
     */
    public boolean doDelegation = true;

    /**
     * Creates a new class loader.
     */
    public TransformClassLoader() {
    }


    /**
     * Creates a new class loader
     * using the specified parent class loader for delegation.
     *
     * @param parent the parent class loader.
     */
    public TransformClassLoader(ClassLoader parent) {
        super(parent);
        init();
    }

    private void init() {
        translator = null;
        domain = null;
        delegateLoadingOf("com.navercorp.pinpoint.test.classloader.TransformClassLoader");
    }

    /**
     * Adds a translator, which is called whenever a class is loaded.
     *
     * @param t  a translator.
     */
    public void addTranslator(Translator t) {
        translator = t;
        t.start();
    }

    /**
     * Records a class so that the loading of that class is delegated
     * to the parent class loader.
     * <p>
     * <p>If the given class name ends with <code>.</code> (dot), then
     * that name is interpreted as a package name.  All the classes
     * in that package and the sub packages are delegated.
     */
    public void delegateLoadingOf(String classname) {
        if (classname.endsWith(".")) {
            notDefinedPackages.add(classname);
        }
        else {
            notDefinedClass.add(classname);
        }
    }

    /**
     * Sets the protection domain for the classes handled by this class
     * loader.  Without registering an appropriate protection domain,
     * the program loaded by this loader will not work with a security
     * manager or a signed jar file.
     */
    public void setDomain(ProtectionDomain d) {
        domain = d;
    }


    /**
     * Requests the class loader to load a class.
     */
    protected Class loadClass(String name, boolean resolve)
            throws ClassFormatError, ClassNotFoundException {

        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                c = loadClassByDelegation(name);
            }

            if (c == null) {
                c = findClass(name);
            }

            if (c == null) {
                c = delegateToParent(name);
            }

            if (resolve) {
                resolveClass(c);
            }

            return c;
        }
    }

    protected Object getClassLoadingLock(String className) {

        final Object newLock = new Object();
        final Object existLock = lockMap.putIfAbsent(className, newLock);
        if (existLock != null) {
            return existLock;
        }
        return newLock;
    }

    /**
     * Finds the specified class using <code>ClassPath</code>.
     * If the source throws an exception, this returns null.
     * <p>
     * <p>This method can be overridden by a subclass of
     * <code>Loader</code>.  Note that the overridden method must not throw
     * an exception when it just fails to find a class file.
     *
     * @return null if the specified class could not be found.
     * @throws ClassNotFoundException if an exception is thrown while
     *                                obtaining a class file.
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classfile;
        try {
            if (translator != null) {
                try {
                    classfile = translator.transform(name);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            } else {
                String jarname = "/" + JavaAssistUtils.javaClassNameToJvmResourceName(name);
                InputStream in = this.getClass().getClassLoader().getResourceAsStream(jarname);
                if (in == null) {
                    return null;
                }
                classfile = IOUtils.toByteArray(in);
            }
        } catch (Exception e) {
            throw new ClassNotFoundException("caught an exception while obtaining a class file for " + name, e);
        }

        final int i = name.lastIndexOf('.');
        if (i != -1) {
            String pname = name.substring(0, i);
            if (getPackage(pname) == null)
                try {
                    definePackage(pname, null, null, null, null, null, null, null);
                } catch (IllegalArgumentException e) {
                    // ignore.  maybe the package object for the same
                    // name has been created just right away.
                }
        }

        if (domain == null) {
            if (logger.isLoggable(Level.FINE)) {
                this.logger.fine("defineClass:" + name);
            }
            return defineClass(name, classfile, 0, classfile.length, DEFAULT_DOMAIN);
        }
        else {
            if (logger.isLoggable(Level.FINE)) {
                this.logger.fine("defineClass:" + name);
            }
            return defineClass(name, classfile, 0, classfile.length, domain);
        }
    }

    protected Class<?> loadClassByDelegation(String name)
            throws ClassNotFoundException {
        /* The swing components must be loaded by a system
         * class loader.
         * javax.swing.UIManager loads a (concrete) subclass
         * of LookAndFeel by a system class loader and cast
         * an instance of the class to LookAndFeel for
         * (maybe) a security reason.  To avoid failure of
         * type conversion, LookAndFeel must not be loaded
         * by this class loader.
         */

        Class<?> c = null;
        if (doDelegation) {
            if (isJdkPackage(name) || notDelegated(name))
                c = delegateToParent(name);
        }

        return c;
    }

    private boolean isJdkPackage(String name) {
        return name.startsWith("java.")
                || name.startsWith("javax.")
                || name.startsWith("sun.")
                || name.startsWith("com.sun.")
                || name.startsWith("org.w3c.")
                || name.startsWith("org.xml.");
    }

    private boolean notDelegated(String name) {
        if (notDefinedClass.contains(name)) {
            return true;
        }

        for (String notDefinedPackage : notDefinedPackages) {
            if (name.startsWith(notDefinedPackage)) {
                return true;
            }
        }
        return false;
    }

    protected Class<?> delegateToParent(String classname)
            throws ClassNotFoundException {
        ClassLoader cl = getParent();
        if (cl != null) {
            return cl.loadClass(classname);
        } else {
            return findSystemClass(classname);
        }
    }
}
