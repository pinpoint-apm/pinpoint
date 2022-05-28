package com.navercorp.pinpoint.sdk.v1.concurrent;

import com.navercorp.pinpoint.sdk.v1.concurrent.util.Counter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ForkJoinTask;

/**
 * {@link ForkJoinTask} for TraceContext propagation
 *
 * @author jimolonely
 **/
public class TraceForkJoinTask<V> extends ForkJoinTask<V> {

    private final ForkJoinTask<V> task;
    /**
     * The result of the computation.
     */
    private V result;

    public TraceForkJoinTask(ForkJoinTask<V> task) {
        this.task = task;
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
     * they all have the compute method, which seems an implicit convention.
     */
    @Override
    protected boolean exec() {
        try {
            Method exec = task.getClass().getDeclaredMethod("exec");
            exec.setAccessible(true);
            boolean finished = (boolean) exec.invoke(task);
            if (finished) {
                result = task.getRawResult();
            }
            // for performance test
            Counter.add();
            return finished;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            // no exec method, then try the compute method
            result = compute();
        }
        return true;
    }

    /**
     * for most child classes of {@link ForkJoinTask} with compute method
     */
    public V compute() {
        try {
            Method compute = task.getClass().getDeclaredMethod("compute");
            compute.setAccessible(true);
            //noinspection unchecked
            result = (V) compute.invoke(task);
            // for performance test
            Counter.add();
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            // ignore
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