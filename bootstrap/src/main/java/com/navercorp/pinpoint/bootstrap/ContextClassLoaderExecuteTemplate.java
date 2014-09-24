package com.nhn.pinpoint.bootstrap;

import java.util.concurrent.Callable;

/**
 * contextClassLoader에 별도의 classLoader를 세팅하고 실행하는 template
 * @author emeroad
 */
public class ContextClassLoaderExecuteTemplate<V> {
    private final ClassLoader classLoader;

    public ContextClassLoaderExecuteTemplate(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader must not be null");
        }
        this.classLoader = classLoader;
    }

    public V execute(Callable<V> callable) throws BootStrapException {
        try {
            final Thread currentThread = Thread.currentThread();
            final ClassLoader before = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(ContextClassLoaderExecuteTemplate.this.classLoader);
            try {
                return callable.call();
            } finally {
                // null일 경우도 다시 원복하는게 맞음.
                // getContextClassLoader 호출시 에러가 발생하였을 경우 여기서 호출당하지 않으므로 이부분에서 원복하는게 맞음.
                currentThread.setContextClassLoader(before);
            }
        } catch (BootStrapException ex){
            throw ex;
        } catch (Exception ex) {
            throw new BootStrapException("execute fail. Caused:" + ex.getMessage(), ex);
        }
    }
}
