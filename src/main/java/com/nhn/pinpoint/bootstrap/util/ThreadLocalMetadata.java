package com.nhn.pinpoint.bootstrap.util;

/**
 * WARN
 * 주의 ------------------  <br>
 * 이 data 전달자는 threadlocal로 전달하기 때문에. proxy클래스로 감쌋을 경우도 경계를 넘어 갈수 있으나. <br>
 * 상태를 가질수 없음. static method나 parameter의 인자로 들어오는 값을 잠시 저장하여 다른 곳으로 넘기는 경우만 사용해야 함. <br>
 * 절대 object의 상태를 가지고 있는데 사용할수 없음. <br>
 * 주의 ------------------
 * @author emeroad
 */
@Deprecated
public class ThreadLocalMetadata<T> {
    private final String name;
    private final ThreadLocal<T> threadLocal;

    public ThreadLocalMetadata(String metadataName) {
        this.name = metadataName;
        this.threadLocal = new ThreadLocal<T>();
    }

    public void set(T object) {
        threadLocal.set(object);
    }

    public T get() {
        return threadLocal.get();
    }

    public void remove() {
        threadLocal.remove();
    }

    public T getAndRemove() {
        final ThreadLocal<T> threadLocal = this.threadLocal;
        final T t = threadLocal.get();
        threadLocal.remove();
        return t;
    }
}
