package com.profiler.context;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.common.AnnotationNames;
import com.profiler.common.ServiceType;
import com.profiler.logging.LoggingUtils;

/**
 *
 */
public class AsyncTrace {
    private static final Logger logger = Logger.getLogger(AsyncTrace.class.getName());
    private static final boolean isDebug = LoggingUtils.isDebug(logger);

    public static final int NON_REGIST = -1;
    // private int id;
    // 비동기일 경우 traceenable의 경우 애매함. span을 보내는것으로 데이터를 생성하므로 약간 이상.
    // private boolean tracingEnabled;

    public static final int STATE_INIT = 0;
    public static final int STATE_FIRE = 1;
    public static final int STATE_TIMEOUT = 2;

    private final AtomicInteger state = new AtomicInteger(STATE_INIT);

    private int asyncId = NON_REGIST;
    private SubSpan subSpan;

    private Storage storage;
    private TimerTask timeoutTask;

    public AsyncTrace(SubSpan subspan) {
        this.subSpan = subspan;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setTimeoutTask(TimerTask timeoutTask) {
        this.timeoutTask = timeoutTask;
    }

    public void setAsyncId(int asyncId) {
        this.asyncId = asyncId;
    }

    public int getAsyncId() {
        return asyncId;
    }

    public SubSpan getSubSpan() {
        return subSpan;
    }

    private Object attachObject;

    public Object getAttachObject() {
        return attachObject;
    }

    public void setAttachObject(Object attachObject) {
        this.attachObject = attachObject;
    }

    public void traceBlockBegin() {
    }

    public void markBeforeTime() {
        subSpan.setStartTime(System.currentTimeMillis());
    }

    public long getBeforeTime() {
        return subSpan.getStartTime();
    }

    public void traceBlockEnd() {
        logSpan(this.subSpan);
//        clearReference();
    }

    private void clearReference() {
        this.storage = null;
    }

    public void markAfterTime() {
        subSpan.setEndTime(System.currentTimeMillis());
    }


    public void recordAttribute(final AnnotationNames key, final String value) {
        recordAttibute(key, (Object) value);
    }

    public void recordAttibute(final AnnotationNames key, final Object value) {
        subSpan.addAnnotation(new Annotation(key, value));
    }


    public void recordRpcName(final ServiceType serviceType, final String service, final String rpc) {
        this.subSpan.setServiceType(serviceType);
        this.subSpan.setServiceName(service);
        this.subSpan.setRpc(rpc);
    }

    // TODO: final String... endPoint로 받으면 합치는데 비용이 들어가 그냥 한번에 받는게 나을것 같음.
    public void recordEndPoint(final String endPoint) {
        this.subSpan.setEndPoint(endPoint);
    }

    private void annotate(final AnnotationNames key) {
        this.subSpan.addAnnotation(new Annotation(key));

    }

    void logSpan(SubSpan subSpan) {
        try {
            if (isDebug) {
                Thread thread = Thread.currentThread();
                logger.info("[WRITE SubSPAN]" + subSpan + " CurrentThreadID=" + thread.getId() + ",\n\t CurrentThreadName=" + thread.getName());
            }
            this.storage.store(subSpan);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public int getState() {
        return state.get();
    }

    public void timeout() {
        if (state.compareAndSet(STATE_INIT, STATE_TIMEOUT)) {
            // TODO timeout subspan log 던지기.
            // 뭘 어떤 내용을 던져야 되는지 아직 모르겠음????
        }
    }

    public boolean fire() {
        if (state.compareAndSet(STATE_INIT, STATE_FIRE)) {
            if (timeoutTask != null) {
                // timeout이 걸려 있는 asynctrace일 경우 호출해 준다.
                this.timeoutTask.cancel();
            }
            return true;
        }
        return false;
    }
}
