package com.nhn.pinpoint.profiler.context;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
@Deprecated
public class DefaultAsyncTrace implements AsyncTrace {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAsyncTrace.class);
    private static final boolean isDebug = logger.isDebugEnabled();
    private static final boolean isTrace = logger.isTraceEnabled();


    public static final int NON_REGIST = -1;
    // private int id;
    // 비동기일 경우 traceenable의 경우 애매함. span을 보내는것으로 데이터를 생성하므로 약간 이상.
    // private boolean tracingEnabled;



    private final AtomicInteger state = new AtomicInteger(STATE_INIT);

    private int asyncId = NON_REGIST;
    private SpanEvent spanEvent;

    private Storage storage;
    private TimerTask timeoutTask;

    public DefaultAsyncTrace(SpanEvent spanEvent) {
        this.spanEvent = spanEvent;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void setTimeoutTask(TimerTask timeoutTask) {
        this.timeoutTask = timeoutTask;
    }

    @Override
    public void setAsyncId(int asyncId) {
        this.asyncId = asyncId;
    }

    @Override
    public int getAsyncId() {
        return asyncId;
    }


    private Object attachObject;

    @Override
    public Object getAttachObject() {
        return attachObject;
    }

    @Override
    public void setAttachObject(Object attachObject) {
        this.attachObject = attachObject;
    }

    @Override
    public void traceBlockBegin() {
    }

    @Override
    public void markBeforeTime() {
        spanEvent.markStartTime();
    }

    @Override
    public long getBeforeTime() {
        return spanEvent.getStartTime();
    }

    @Override
    public void traceBlockEnd() {
        logSpan(this.spanEvent);
    }

    @Override
    public void markAfterTime() {
        spanEvent.markAfterTime();
    }


    @Override
    public void recordApi(MethodDescriptor methodDescriptor) {
        if (methodDescriptor == null) {
            return;
        }
        if (methodDescriptor.getApiId() == 0) {
            recordAttribute(AnnotationKey.API, methodDescriptor.getFullName());
        } else {
            spanEvent.setApiId(methodDescriptor.getApiId());
        }
    }



    @Override
    public void recordException(Object result) {
        if (result instanceof Throwable) {
            Throwable th = (Throwable) result;
            String drop = StringUtils.drop(th.getMessage());

            recordAttribute(AnnotationKey.EXCEPTION, drop);

//            TODO 비동기 api일 경우, span에 exception을 마크하기가 까다로움
//            AnnotationKey span = getCallStack().getSpan();
//            if (span.getErrCode() == 0) {
//                span.setErrCode(1);
//            }
        }
    }

    @Override
    public void recordAttribute(final AnnotationKey key, final String value) {
        spanEvent.addAnnotation(new Annotation(key.getCode(), value));
    }

    @Override
    public void recordAttribute(final AnnotationKey key, final int value) {
        spanEvent.addAnnotation(new Annotation(key.getCode(), value));
    }


    @Override
    public void recordAttribute(final AnnotationKey key, final Object value) {
        spanEvent.addAnnotation(new Annotation(key.getCode(), value));
    }

    @Override
    public void recordServiceType(final ServiceType serviceType) {
        this.spanEvent.setServiceType(serviceType.getCode());
    }

    @Override
    public void recordRpcName(final String rpcName) {
        this.spanEvent.setRpc(rpcName);

    }


    @Override
    public void recordDestinationId(String destinationId) {
        this.spanEvent.setDestinationId(destinationId);
    }

    // TODO: final String... endPoint로 받으면 합치는데 비용이 들어가 그냥 한번에 받는게 나을것 같음.
    @Override
    public void recordEndPoint(final String endPoint) {
        this.spanEvent.setEndPoint(endPoint);
    }

    private void logSpan(SpanEvent spanEvent) {
        try {
            if (isTrace) {
                Thread thread = Thread.currentThread();
                logger.trace("[WRITE SpanEvent]{} CurrentThreadID={} CurrentThreadName={}", spanEvent, thread.getId(), thread.getName());
            }
            this.storage.store(spanEvent);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public int getState() {
        return state.get();
    }

    public void timeout() {
        if (state.compareAndSet(STATE_INIT, STATE_TIMEOUT)) {
            // TODO timeout spanEvent log 던지기.
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
