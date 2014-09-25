package com.nhn.pinpoint.rpc;

import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class DefaultFuture<T> implements TimerTask, Future<T> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFuture.class);

    private final long timeoutMillis;
    private int waiters = 0;

    private boolean ready = false;

    private T result;
    private Throwable cause;

    private Timeout timeout;
    private FailureEventHandler failureEventHandler;
    private FutureListener<T> listener;


    public DefaultFuture() {
        this(3000);
    }

    public DefaultFuture(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public synchronized T getResult() {
        if (this.cause != null) {
            throw new PinpointSocketException(cause);
        }
        return result;
    }

    @Override
    public synchronized Throwable getCause() {
        return cause;
    }

    @Override
    public synchronized boolean isReady() {
        return ready;
    }

    @Override
    public synchronized boolean isSuccess() {
        return ready && cause == null;
    }

    public boolean setResult(T message) {
        synchronized (this) {
            if (ready) {
                return false;
            }
            this.ready = true;

            this.result = message;
            if (waiters > 0) {
                notifyAll();
            }
        }
        cancelTimeout();
        notifyListener();
        return true;

    }

    public boolean setFailure(Throwable cause) {
        synchronized (this) {
            if (ready) {
                return false;
            }
            this.ready = true;

            this.cause = cause;

            if (waiters > 0) {
                notifyAll();
            }
        }

        cancelTimeout();
        notifyFailureHandle();
        notifyListener();
        return true;
    }

    private boolean fireTimeout() {
        synchronized (this) {
            if (ready) {
                return false;
            }
            this.ready = true;

            this.cause = new PinpointSocketException("timeout");

            if (waiters > 0) {
                notifyAll();
            }
        }
        // 이미 timeout 되서 들어오기 때문에 tieout.cancel시킬필요가 없음.
        notifyFailureHandle();
        notifyListener();
        return true;
    }

    private void cancelTimeout() {
        final Timeout timeout = this.timeout;
        if (timeout != null) {
            timeout.cancel();
            this.timeout = null;
        }
    }

    private void notifyListener() {
        FutureListener<T> listener = this.listener;
        if (listener != null) {
            fireOnComplete(listener);
            this.listener = null;
        }
    }

    protected void notifyFailureHandle() {

        FailureEventHandler failureEventHandler = this.failureEventHandler;
        if (failureEventHandler != null) {
            failureEventHandler.fireFailure();
            this.failureEventHandler = null;
        }
    }

    @Override
    public boolean setListener(FutureListener<T> listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }

        boolean alreadyReady = false;
        synchronized (this) {
            if (ready) {
                alreadyReady = true;
            } else {
                this.listener = listener;
            }

        }

        if (alreadyReady) {
            fireOnComplete(listener);
        }
        return !alreadyReady;
    }

    private boolean fireOnComplete(FutureListener<T> listener) {
        try {
            listener.onComplete(this);
            return true;
        } catch (Throwable th) {
            logger.warn("FutureListener.onComplete() fail Caused:{}", th.getMessage(), th);
            return false;
        }
    }

    @Override
    public boolean await(long timeoutMillis) {
        return await0(timeoutMillis);
    }

    @Override
    public boolean await() {
        return await0(this.timeoutMillis);
    }

    private boolean await0(long timeoutMillis) {
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("timeoutMillis must not be negative :" + timeoutMillis);
        }

        boolean interrupted = false;

        synchronized (this) {
            if (ready) {
                return true;
            }

            try {
                this.waiters++;
                wait(timeoutMillis);
            } catch (InterruptedException e) {
                interrupted = true;
            } finally {
                this.waiters--;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return ready;

    }


    @Override
    public void run(Timeout timeout) throws Exception {
        if (timeout.isCancelled()) {
            return;
        }
        this.fireTimeout();
    }

    public void setTimeout(Timeout timeout) {
        if (timeout == null) {
            throw new NullPointerException("timeout");
        }
        this.timeout = timeout;
    }

    public void setFailureEventHandler(FailureEventHandler failureEventHandler) {
        if (failureEventHandler == null) {
            throw new NullPointerException("failureEventHandler");
        }
        this.failureEventHandler = failureEventHandler;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DefaultFuture");
        sb.append("{ready=").append(ready);
        sb.append(", result=").append(result);
        sb.append(", cause=").append(cause);
        sb.append('}');
        return sb.toString();
    }
}
