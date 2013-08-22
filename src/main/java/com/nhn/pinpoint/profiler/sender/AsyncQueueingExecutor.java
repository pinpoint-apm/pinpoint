package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class AsyncQueueingExecutor implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkedBlockingQueue<Object> queue;
    private final PinpointThreadFactory threadFactory;
    private final AtomicBoolean isRun = new AtomicBoolean(true);
    private final Thread ioThread;
    private String executorName;

    private final int maxDrainSize = 10;
    // 주의 single thread용임. ArrayList보다 더 단순한 오퍼레이션을 수행하는 Collection.
    private Collection<Object> drain = new UnsafeArrayCollection<Object>(maxDrainSize);

    private AsyncQueueingExecutorListener listener;


    public AsyncQueueingExecutor() {
        this(1024 * 5, "Pinpoint-AsyncQueueingExecutor");
    }

    public AsyncQueueingExecutor(int queueSize, String executorName) {
        this.executorName = executorName;
        this.queue = new LinkedBlockingQueue<Object>(queueSize);
        this.threadFactory = new PinpointThreadFactory(executorName, true);
        this.ioThread = this.createIoThread();
    }

    private Thread createIoThread() {
        Thread thread = threadFactory.newThread(this);
        thread.start();
        return thread;
    }

    public void setThreadName(String threadName) {
        this.threadFactory.setThreadName(threadName);
    }

    @Override
    public void run() {
        Thread thread = Thread.currentThread();
        logger.info("{}-({}) started.", thread.getName(), thread.getId());
        doExecute();
    }

    protected void doExecute() {
        drainStartEntry:
        while (true) {
            try {
                if (isShutdown()) {
                    break;
                }

                Collection<Object> dtoList = getDrainQueue();
                int drainSize = takeN(dtoList, this.maxDrainSize);
                if (drainSize > 0) {
                    doExecute(dtoList);
                    continue;
                }

                while (true) {
                    if (isShutdown()) {
                        break;
                    }

                    Object dto = takeOne();
                    if (dto != null) {
                        doExecute(dto);
                        continue drainStartEntry;
                    }
                }
            } catch (Throwable th) {
                logger.warn("{} doExecute(). Unexpected Error. Cause:{}", new Object[]{executorName, th.getMessage(), th});
            }
        }
        flushQueue();
    }

    private void flushQueue() {
        boolean debugEnabled = logger.isDebugEnabled();
        if (debugEnabled) {
            logger.debug("Loop is stop.");
        }
        while(true) {
            Collection<Object> dtoList = getDrainQueue();
           int drainSize = takeN(dtoList, this.maxDrainSize);
            if (drainSize == 0) {
                break;
            }
            if (debugEnabled) {
                logger.debug("flushData size {}", drainSize);
            }
            doExecute(dtoList);
        }
    }

    protected Object takeOne() {
        try {
            return queue.poll(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // Thread.currentThread().interrupt();
            // 인터럽트 한번은 그냥 무시한다.
            return null;
        }
    }

    protected int takeN(Collection<Object> drain, int maxDrainSize) {
        return queue.drainTo(drain, maxDrainSize);
    }

    public boolean execute(Object data) {
        final boolean warnEnabled = logger.isWarnEnabled();
        if (data == null) {
            if (warnEnabled) {
                logger.warn("execute(). data is null");
            }
            return false;
        }
        if (!isRun.get()) {
            if (warnEnabled) {
                logger.warn("{} is shutdown. discard data:{}", this.executorName, data);
            }
            return false;
        }
        boolean offer = queue.offer(data);
        if (!offer) {
            if (warnEnabled) {
                logger.warn("{} Drop data. queue is full. size:{}", this.executorName, queue.size());
            }
        }
        return offer;
    }

    public void setListener(AsyncQueueingExecutorListener listener) {
        this.listener = listener;
    }

    private void doExecute(Collection<Object> dtoList) {
        this.listener.execute(dtoList);
    }

    private void doExecute(Object dto) {
        this.listener.execute(dto);
    }

    public boolean isEmpty() {
        return queue.size() == 0;
    }

    public boolean isShutdown() {
        return !isRun.get();
    }

    public void stop() {
        isRun.set(false);

        if (!isEmpty()) {
            logger.info("Wait 5 seconds. Flushing queued data.");
        }

        try {
            ioThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("{} stopped incompletely.", executorName);
        }

        logger.info("{} stopped.", executorName);
    }

    Collection<Object> getDrainQueue() {
        this.drain.clear();
        return drain;
    }
}
