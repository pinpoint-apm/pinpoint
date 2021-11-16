package com.navercorp.pinpoint.common.hbase.batch;

import com.navercorp.pinpoint.common.hbase.HbaseSystemException;
import com.navercorp.pinpoint.common.hbase.util.HBaseExceptionUtils;
import com.navercorp.pinpoint.common.hbase.util.SharedExecutorService;
import com.navercorp.pinpoint.common.profiler.concurrent.ExecutorFactory;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.CpuUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.BufferedMutatorImpl;
import org.apache.hadoop.hbase.client.BufferedMutatorParams;
import org.apache.hadoop.hbase.client.BufferedMutatorUtils;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

public class BufferedMutatorWriter implements DisposableBean, HbaseBatchWriter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BufferedMutatorConfiguration configuration;
    private final boolean autoFlush;

    //    private final ReadWriteLock lock = new ReentrantReadWriteLock();
//    private final Striped<Lock> lock = Striped.lock(128);

    private final ConcurrentMap<TableName, BufferedMutatorImpl> mutatorMap = new ConcurrentHashMap<>();
    private final Function<TableName, BufferedMutatorImpl> mutatorSupplier;

    private final LongAdder successCounter = new LongAdder();
    private final LongAdder errorCounter = new LongAdder();

    private final ExecutorService pool;
    private final ExecutorService sharedPool;


    public BufferedMutatorWriter(Connection connection, BufferedMutatorConfiguration configuration) {
        Objects.requireNonNull(connection, "connection");
        logger.info("{}", configuration);
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.autoFlush = configuration.isAutoFlush();

        this.mutatorSupplier = new MutatorFactory(connection);

        this.pool = newExecutorService();
        this.sharedPool = new SharedExecutorService(this.pool);
    }

    private ExecutorService newExecutorService() {
        ThreadFactory factory = PinpointThreadFactory.createThreadFactory("BufferedMutatorWriter");
        final ThreadPoolExecutor pool = ExecutorFactory.newFixedThreadPool(CpuUtils.cpuCount(), 1024, factory);
        pool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // error log
                logger.warn("Async batch job rejected job:{} ", r);
            }
        });
        return pool;
    }

    private BufferedMutatorParams newBufferedMutatorParams(TableName tableName) {
        BufferedMutatorParams params = new BufferedMutatorParams(tableName);
        params.writeBufferSize(configuration.getWriteBufferSize());
        params.setWriteBufferPeriodicFlushTimeoutMs(configuration.getWriteBufferPeriodicFlushTimerTickMs());
        params.setWriteBufferPeriodicFlushTimerTickMs(configuration.getWriteBufferPeriodicFlushTimerTickMs());
        params.pool(this.sharedPool);

        params.listener(new BufferedMutator.ExceptionListener() {
            @Override
            public void onException(RetriesExhaustedWithDetailsException e, BufferedMutator bufferedMutator) throws RetriesExhaustedWithDetailsException {

                errorCounter.increment();
                // fail count
                TableName tableName = bufferedMutator.getName();
                int numExceptions = e.getNumExceptions();
                if (e.mayHaveClusterIssues()) {
                    String hosts = HBaseExceptionUtils.getErrorHost(e);
                    logger.warn("Batch write error(mayHaveClusterIssues) {} numExceptions:{} {}", tableName, numExceptions, hosts);
                } else {
                    logger.warn("Batch write error {} numExceptions:{}", tableName, numExceptions);
                }
                if (logger.isDebugEnabled()) {
                    String exhaustiveDescription = e.getExhaustiveDescription();
                    logger.debug("ExhaustiveDescription {}", exhaustiveDescription);
                }
            }

        });
        return params;
    }

    @Override
    public void write(TableName tableName, List<? extends Mutation> mutations) {
        Objects.requireNonNull(tableName, "tableName");

        final BufferedMutatorImpl mutator = getBufferedMutator(tableName);

        final long currentWriteBufferSize = BufferedMutatorUtils.getCurrentWriteBufferSize(mutator);
        if (currentWriteBufferSize > configuration.getWriteBufferHeapLimit()) {
            this.errorCounter.increment();
            return;
        }
        try {
            mutator.mutate(mutations);
            this.successCounter.increment();
            autoFlush(mutator);
        } catch (IOException e) {
            this.errorCounter.increment();
            throw new HbaseSystemException(e);
        }
    }

    private void autoFlush(BufferedMutatorImpl mutator) throws InterruptedIOException, RetriesExhaustedWithDetailsException {
        if (autoFlush) {
            mutator.flush();
        }
    }

    private BufferedMutatorImpl getBufferedMutator(TableName tableName) {
        // workaround https://bugs.openjdk.java.net/browse/JDK-8161372
        final BufferedMutatorImpl mutator = this.mutatorMap.get(tableName);
        if (mutator != null) {
            return mutator;
        }

        return this.mutatorMap.computeIfAbsent(tableName, this.mutatorSupplier);
//        final Lock lock = this.lock.get(tableName);
//        try {
//            lock.lock();
//            return this.mutatorMap.computeIfAbsent(tableName, this.mutatorSupplier);
//        } finally {
//            lock.unlock();
//        }
    }

    @Override
    public void write(TableName tableName, Mutation mutation) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(mutation, "mutation");

        write(tableName, Collections.singletonList(mutation));
    }

    private class MutatorFactory implements Function<TableName, BufferedMutatorImpl> {
        private final Connection connection;

        public MutatorFactory(Connection connection) {
            this.connection = Objects.requireNonNull(connection, "connection");
        }

        @Override
        public BufferedMutatorImpl apply(TableName tableName) {
            try {
                BufferedMutatorParams params = newBufferedMutatorParams(tableName);
                return (BufferedMutatorImpl) this.connection.getBufferedMutator(params);
            } catch (IOException e) {
                throw new HbaseSystemException(e);
            }
        }
    }

    public long getSuccessCount() {
        return successCounter.sum();
    }

    public long getErrorCount() {
        return errorCounter.sum();
    }

    @Override
    public void destroy() throws Exception {
        logger.info("destroy {}", this.mutatorMap.size());
//        final Lock lock = this.lock.get(tableName);
//        lock.lock();
//        try {
//            closeMutator();
//        } finally {
//            lock.unlock();
//        }
        closeMutator();
        this.pool.shutdown();
        this.pool.awaitTermination(1000 * 3, TimeUnit.MICROSECONDS);

    }

    private void closeMutator() {
        for (BufferedMutator mutator : this.mutatorMap.values()) {
            try {
                mutator.close();
            } catch (IOException ignore) {
                //
            }
        }
    }
}
