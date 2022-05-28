package com.navercorp.pinpoint.sdk.v1.concurrent;

import com.navercorp.pinpoint.sdk.v1.concurrent.util.Counter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;

/**
 * {@link ForkJoinTask} for TraceContext propagation
 *
 * @author jimolonely
 **/
public class TraceForkJoinTask<V> extends ForkJoinTask<V> {

    private static class MethodCache {
        Method exec;
        Method compute;

        public MethodCache(Method exec, Method compute) {
            this.exec = exec;
            this.compute = compute;
        }
    }

    private final ForkJoinTask<V> task;
    /**
     * The result of the computation.
     */
    private V result;

    private static final Map<String, MethodCache> cacheMethodMap = new HashMap<>(16);

    private MethodCache cacheMethod;

    public TraceForkJoinTask(ForkJoinTask<V> task) {
        this.task = task;
        setMethodCache();
    }

    /**
     * getMethod is the key of performance loss using reflection, so we cache it,
     * and the performance improves dozens of times
     */
    private void setMethodCache() {
        String className = task.getClass().getCanonicalName();
        MethodCache cache = cacheMethodMap.get(className);
        if (cache == null) {
            cache = new MethodCache(reflectMethod("exec"), reflectMethod("compute"));
            cacheMethodMap.put(className, cache);
        }
        cacheMethod = cache;
    }

    private Method reflectMethod(String method) {
        try {
            Method exec = task.getClass().getDeclaredMethod(method);
            exec.setAccessible(true);
            return exec;
        } catch (NoSuchMethodException e) {
            // ignore
        }
        return null;
    }

    @Override
    public V getRawResult() {
        return result;
    }

    @Override
    protected void setRawResult(V value) {
        result = value;
    }

    /**
     * if the task is a direct subclass of ForkJoinTask, we can get the exec method through reflect.
     * otherwise, we try the compute method.
     * eg: for {@link java.util.concurrent.RecursiveAction},{@link java.util.concurrent.RecursiveTask},
     * * they all have the compute method, which seems an implicit convention.
     */

    @Override
    protected boolean exec() {
        try {
            if (cacheMethod.exec != null) {
                boolean ok = (boolean) cacheMethod.exec.invoke(task);
                if (ok) {
                    result = task.getRawResult();
                }
                // test
                Counter.add();
                return ok;
            }
            result = compute();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * for most child classes of {@link ForkJoinTask} with compute method
     */
    public V compute() {
        try {
            if (cacheMethod.compute != null) {
                //noinspection unchecked
                result = (V) cacheMethod.compute.invoke(task);
                // test
                Counter.add();
                return result;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static <V> ForkJoinTask<V> wrap(ForkJoinTask<V> delegate) {
        return new TraceForkJoinTask<>(delegate);
    }

    public static <V> ForkJoinTask<V> asyncEntry(ForkJoinTask<V> delegate) {
        return new TraceForkJoinTask<>(delegate);
    }
}