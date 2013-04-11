package com.profiler;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

/**
 *
 */
public class DummyInstrumentation implements Instrumentation {
    @Override
    public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean removeTransformer(ClassFileTransformer transformer) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRetransformClassesSupported() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRedefineClassesSupported() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void redefineClasses(ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isModifiableClass(Class<?> theClass) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class[] getAllLoadedClasses() {
        return new Class[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class[] getInitiatedClasses(ClassLoader loader) {
        return new Class[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getObjectSize(Object objectToSize) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void appendToBootstrapClassLoaderSearch(JarFile jarfile) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void appendToSystemClassLoaderSearch(JarFile jarfile) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isNativeMethodPrefixSupported() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
