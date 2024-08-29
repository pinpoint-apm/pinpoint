package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.hbase.RequestNotPermittedException;
import com.navercorp.pinpoint.common.hbase.util.FutureUtils;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncPollerThread implements Closeable {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ThrottledLogger tLogger = ThrottledLogger.getLogger(logger, 100);

    private final TableWriterFactory writerFactory;

    private final BlockingQueue<WriteRequest> queue;
    private final int queueSize;
    private final int writeBufferSize;
    private final int writeBufferPeriodicFlush;
    private final int pollTimeout;

    private final Thread thread;
    private final AtomicBoolean runState = new AtomicBoolean(true);

    public static final RequestNotPermittedException OVERFLOW = new RequestNotPermittedException("write queue is full", false);

    public AsyncPollerThread(String id, TableWriterFactory writerFactory,
                             AsyncPollerOption option) {
        this.writerFactory = Objects.requireNonNull(writerFactory, "writerFactory");

        this.queueSize = option.getQueueSize();
        this.queue = new ArrayBlockingQueue<>(queueSize);

        this.writeBufferSize = option.getWriteBufferSize();
        this.writeBufferPeriodicFlush = option.getWriteBufferPeriodicFlush();
        this.pollTimeout = Math.max(writeBufferPeriodicFlush / 4, 20);

        this.thread = new Thread(this::dispatch, id);
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public List<CompletableFuture<Void>> write(TableName tableName, List<Put> puts) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(puts, "puts");
        if (isShutdown()) {
            return FutureUtils.newFutureList(() -> CompletableFuture.failedFuture(new IllegalStateException("closed")), puts.size());
        }

        WriteRequest writeRequest = new WriteRequest(tableName, puts);
        if (this.queue.offer(writeRequest)) {
            return writeRequest.getFutures();
        }
        tLogger.info("write queue overflow");
        return FutureUtils.newFutureList(() -> CompletableFuture.failedFuture(OVERFLOW), puts.size());
    }


    public void dispatch() {
        while (isRun()) {
            try {
                List<WriteRequest> requests = poll();
                if (requests == null) {
                    break;
                }
                Map<TableName, List<WriteRequest>> map = tableGroup(requests);
                for (Map.Entry<TableName, List<WriteRequest>> entry : map.entrySet()) {
                    TableName tableName = entry.getKey();
                    List<WriteRequest> writes = entry.getValue();

                    if (logger.isDebugEnabled()) {
                        logger.debug("write {} {} requests:{}", this.thread.getName(), tableName, writes.size());
                    }
                    List<Put> puts = getPuts(writes);

                    AsyncTableWriterFactory.Writer writer = this.writerFactory.writer(tableName);
                    List<CompletableFuture<Void>> hbaseResults = writer.put(puts);
                    addListeners(hbaseResults, writes);
                }
            } catch (Throwable th) {
                logger.warn("Dispatch Error {}", this.thread.getName(), th);
                if (isShutdown()) {
                    break;
                }
            }
        }
        logger.info("dispatch terminated {}", this.thread.getName());
    }

    private boolean isRun() {
        return runState.get();
    }

    private boolean isShutdown() {
        return !isRun();
    }

    private Map<TableName, List<WriteRequest>> tableGroup(List<WriteRequest> requests) {
        Map<TableName, List<WriteRequest>> map = new HashMap<>();
        for (WriteRequest req : requests) {
            TableName tableName = req.getTableName();
            List<WriteRequest> puts = map.computeIfAbsent(tableName, (key) -> new ArrayList<>());
            puts.add(req);
        }
        return map;
    }

    @Override
    public void close() {
        logger.debug("Close {}", this.thread.getName());
        this.runState.set(false);
        this.thread.interrupt();
        try {
            this.thread.join(3000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private List<Put> getPuts(List<WriteRequest> writes) {
        List<Put> puts = new ArrayList<>();
        for (WriteRequest write : writes) {
            puts.addAll(write.getPuts());
        }
        return puts;
    }

    private static void addListeners(List<CompletableFuture<Void>> hbaseResults, List<WriteRequest> requests) {
        int i = 0;
        for (WriteRequest writeRequest : requests) {
            for (CompletableFuture<Void> write : writeRequest.getFutures()) {
                CompletableFuture<Void> hbaseFuture = hbaseResults.get(i++);
                FutureUtils.addListener(hbaseFuture, write);
            }
        }
    }

    private List<WriteRequest> poll()  {
        final long startTime = System.currentTimeMillis();

        List<WriteRequest> drain = new ArrayList<>(writeBufferSize);
        int drainSize = 0;
        while (isRun()) {
            WriteRequest request = null;
            try {
                request = queue.poll(pollTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Thread.interrupted {}", this.thread.getName());
                if (isShutdown()) {
                    return null;
                }
            }
            if (request != null) {
                drain.add(request);
                drainSize += request.size();
                if (bufferOverflow(drainSize)) {
                    return drain;
                }
            }
            if (drainSize > 0) {
                if (timeout(startTime)) {
                    return drain;
                }
            }
        }
        return null;
    }

    private boolean timeout(long startTime) {
        return System.currentTimeMillis() - startTime > writeBufferPeriodicFlush;
    }

    private boolean bufferOverflow(int drainSize) {
        return drainSize >= writeBufferSize;
    }

    @Override
    public String toString() {
        return "AsyncPollerThread{" +
                "writerFactory=" + writerFactory +
                ", queueSize=" + queueSize +
                ", writeBufferSize=" + writeBufferSize +
                ", writeBufferPeriodicFlush=" + writeBufferPeriodicFlush +
                ", pollTimeout=" + pollTimeout +
                ", thread=" + thread +
                '}';
    }
}
