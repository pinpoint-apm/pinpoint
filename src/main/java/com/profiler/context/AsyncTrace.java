package com.profiler.context;

import com.profiler.common.util.AnnotationTranscoder;
import com.profiler.sender.DataSender;


import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class AsyncTrace {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public static final int NON_REGIST = -1;
    // 일단 c&p
    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();
    //    private int id;
    // 비동기일 경우 traceenable의 경우 애매함. span을 보내는것으로 데이터를 생성하므로 약간 이상.
//    private boolean tracingEnabled;

    public static final int STATE_INIT = 0;
    public static final int STATE_FIRE = 1;
    public static final int STATE_TIMEOUT = 2;


    private final AtomicInteger state = new AtomicInteger(STATE_INIT);

    private int asyncId = NON_REGIST;
    private Span span;
    private DataSender dataSender;
    private TimerTask timeoutTask;

    public AsyncTrace(Span span) {
        this.span = span;
    }

    public void setDataSender(DataSender dataSender) {
        this.dataSender = dataSender;
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

    public Span getSpan() {
        return span;
    }

    private Object attachObject;

    public Object getAttachObject() {
        return attachObject;
    }

    public void setAttachObject(Object attachObject) {
        this.attachObject = attachObject;
    }


    public void record(Annotation annotation) {
        annotate(annotation.getCode(), null);
    }

    public void record(Annotation annotation, long duration) {
        annotate(annotation.getCode(), duration);
    }

    public void recordAttribute(final String key, final String value) {
        recordAttibute(key, (Object) value);
    }

    public void recordAttibute(final String key, final Object value) {
        try {
            // TODO 사용자 thread에서 encoding을 하지 않도록 변경.
            AnnotationTranscoder.Encoded enc = transcoder.encode(value);
            span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key, enc.getValueType(), enc.getBytes(), null));
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordMessage(String message) {
        annotate(message, null);
    }

    public void recordRpcName(final String service, final String rpc) {
        try {
            this.span.setServiceName(service);
            this.span.setName(rpc);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordTerminalEndPoint(final String endPoint) {
        recordEndPoint(endPoint, true);
    }

    public void recordEndPoint(final String endPoint) {
        recordEndPoint(endPoint, false);
    }

    // TODO: final String... endPoint로 받으면 합치는데 비용이 들어가 그냥 한번에 받는게 나을것 같음.
    private void recordEndPoint(final String endPoint, final boolean isTerminal) {
        try {
            this.span.setEndPoint(endPoint);
            this.span.setTerminal(isTerminal);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void annotate(final String key, final Long duration) {

        try {
            this.span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key, duration));
            logSpan(key, span);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }


    private void logSpan(String key, Span span) {
        if (key == null) {
            return;
        }
        if (key.equals(Annotation.ClientRecv.getCode()) || key.equals(Annotation.ServerSend.getCode())) {
            logSpan(span);
        }
    }

    void logSpan(Span span) {
        try {
            if (logger.isLoggable(Level.INFO)) {
                Thread thread = Thread.currentThread();
                logger.info("[WRITE SPAN]" + span + " CurrentThreadID=" + thread.getId() + ",\n\t CurrentThreadName=" + thread.getName());
            }

            this.dataSender.send(span.toThrift());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public int getState() {
        return state.get();
    }

    public void timeout() {
        if (state.compareAndSet(STATE_INIT, STATE_TIMEOUT)) {
            // TODO timeout span log 던지기.
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
