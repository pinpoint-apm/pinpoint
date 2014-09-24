package com.nhn.pinpoint.bootstrap.interceptor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 */
public class InterceptorRegistry {

    private static final LoggingInterceptor DUMMY = new LoggingInterceptor("com.nhn.pinpoint.profiler.interceptor.DUMMY");

    public static final InterceptorRegistry REGISTRY = new InterceptorRegistry();

    private final static int DEFAULT_MAX = 4096;
    private final int max;

    private final AtomicInteger id = new AtomicInteger(0);
    private final StaticAroundInterceptor[] index;
    private final SimpleAroundInterceptor[] simpleIndex;

//    private final ConcurrentMap<String, Integer> nameIndex = new ConcurrentHashMap<String, Integer>();

    public InterceptorRegistry() {
        this(DEFAULT_MAX);
    }

    InterceptorRegistry(int max) {
        this.max = max;
        this.index = new StaticAroundInterceptor[max];
        this.simpleIndex = new SimpleAroundInterceptor[max];
    }


    public int addInterceptor0(StaticAroundInterceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }
        int newId = nextId();

        if (newId > max) {
            throw new IndexOutOfBoundsException("size=" + index.length + ", id=" + id);
        }

        this.index[newId] = interceptor;
//        this.nameIndex.put(interceptor.getClass().getName(), newId);
        return newId;
    }

    private int nextId() {
        int number =  id.getAndIncrement();
        
        return number;
    }

    int addSimpleInterceptor0(SimpleAroundInterceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }
        int newId = nextId();
        if (newId >= max) {
            throw new IndexOutOfBoundsException("size=" + index.length + ", id=" + id);
        }

        this.simpleIndex[newId] = interceptor;
//        this.nameIndex.put(interceptor.getClass().getName(), newId);
        return newId;
    }

    public StaticAroundInterceptor getInterceptor0(int key) {
        StaticAroundInterceptor interceptor = index[key];
        if (interceptor == null) {
            // 로직이 잘못되었을 경우 에러가 발생하지 않도록 더미를 리턴.
            return DUMMY;
        }
        return interceptor;
    }

    SimpleAroundInterceptor getSimpleInterceptor0(int key) {
        SimpleAroundInterceptor interceptor = simpleIndex[key];
        if (interceptor == null) {
            // 로직이 잘못되었을경우  에러가 발생하지 않도록 더미를 리턴.
            return DUMMY;
        }
        return interceptor;
    }

//    SimpleAroundInterceptor getInterceptor0(int key) {
//        StaticAfterInterceptor interceptor = index[key];
//        if (interceptor == null) {
//            // 로직이 잘못되었을 경우 에러가 발생하지 않도록 더미를 리턴.
//            return DUMMY;
//        }
//        return interceptor;
//    }
//    public Interceptor findInterceptor0(String interceptorName) {
//        Integer indexNumber = this.nameIndex.get(interceptorName);
//        if (indexNumber != null) {
//            return index[indexNumber];
//        }
//        return null;
//    }

    public static int addInterceptor(StaticAroundInterceptor interceptor) {
        return REGISTRY.addInterceptor0(interceptor);
    }

    public static StaticAroundInterceptor getInterceptor(int key) {
        return REGISTRY.getInterceptor0(key);
    }


    public static Interceptor findInterceptor(int key) {
        SimpleAroundInterceptor simpleInterceptor = REGISTRY.getSimpleInterceptor0(key);
        if (simpleInterceptor != null) {
            return simpleInterceptor;
        }
        StaticAroundInterceptor staticAroundInterceptor = REGISTRY.getInterceptor0(key);
        if (staticAroundInterceptor != null) {
            return staticAroundInterceptor;
        }
        Logger logger = Logger.getLogger(InterceptorRegistry.class.getName());
        if (logger.isLoggable(Level.WARNING)) {
            logger.warning("interceptor not found. id:" + key);
        }
        return DUMMY;
    }

    public static int addSimpleInterceptor(SimpleAroundInterceptor interceptor) {
        return REGISTRY.addSimpleInterceptor0(interceptor);
    }


    public static SimpleAroundInterceptor getSimpleInterceptor(int key) {
        return REGISTRY.getSimpleInterceptor0(key);
    }

}
