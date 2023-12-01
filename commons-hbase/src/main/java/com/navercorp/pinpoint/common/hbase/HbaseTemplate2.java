/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase;

import com.navercorp.pinpoint.common.hbase.async.AdvancedAsyncTableCallback;
import com.navercorp.pinpoint.common.hbase.async.AsyncDistributedScanner;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableCallback;
import com.navercorp.pinpoint.common.hbase.parallel.ParallelResultScanner;
import com.navercorp.pinpoint.common.hbase.parallel.ScanTaskException;
import com.navercorp.pinpoint.common.hbase.parallel.ScanUtils;
import com.navercorp.pinpoint.common.profiler.concurrent.ExecutorFactory;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.StopWatch;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import com.sematext.hbase.wd.DistributedScanner;
import org.apache.commons.collections4.ListUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.AdvancedScanResultConsumer;
import org.apache.hadoop.hbase.client.AsyncTable;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.ScanResultConsumer;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author emeroad
 * @author HyunGil Jeong
 * @author minwoo.jung
 * @author Taejin Koo
 */
public class HbaseTemplate2 extends HbaseAccessor implements HbaseOperations2, InitializingBean, DisposableBean {

    private static final int DEFAULT_MAX_THREADS_FOR_PARALLEL_SCANNER = 128;
    private static final int DEFAULT_MAX_THREADS_PER_PARALLEL_SCAN = 1;

    private static final long DEFAULT_DESTORY_TIMEOUT = 2000;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AtomicBoolean isClose = new AtomicBoolean(false);

    private ExecutorService executor;
    private boolean enableParallelScan = false;
    private int maxThreads = DEFAULT_MAX_THREADS_FOR_PARALLEL_SCANNER;
    private int maxThreadsPerParallelScan = DEFAULT_MAX_THREADS_PER_PARALLEL_SCAN;

    private HBaseAsyncOperation asyncOperation = DisabledHBaseAsyncOperation.INSTANCE;

    private boolean nativeAsync = false;

    private static final CheckAndMutateResult CHECK_AND_MUTATE_RESULT_FAILURE = new CheckAndMutateResult(false, null);

    public HbaseTemplate2() {
    }
    
    private Table getTable(TableName tableName) {
        return getTableFactory().getTable(tableName);
    }
    
    public void setEnableParallelScan(boolean enableParallelScan) {
        this.enableParallelScan = enableParallelScan;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void setMaxThreadsPerParallelScan(int maxThreadsPerParallelScan) {
        this.maxThreadsPerParallelScan = maxThreadsPerParallelScan;
    }

    public void setAsyncOperation(HBaseAsyncOperation asyncOperation) {
        this.asyncOperation = Objects.requireNonNull(asyncOperation, "asyncOperation");
    }

    public void setNativeAsync(boolean nativeAsync) {
        this.nativeAsync = nativeAsync;
    }

    @Override
    public void afterPropertiesSet() {
        Configuration configuration = getConfiguration();
        Objects.requireNonNull(configuration, "configuration is required");
        Objects.requireNonNull(getTableFactory(), "tableFactory is required");
        Objects.requireNonNull(getAsyncTableFactory(), "asyncTableFactory is required");

        PinpointThreadFactory parallelScannerThreadFactory = new PinpointThreadFactory("Pinpoint-parallel-scanner", true);
        if (this.maxThreadsPerParallelScan <= 1) {
            this.enableParallelScan = false;
            this.executor = Executors.newSingleThreadExecutor(parallelScannerThreadFactory);
        } else {
            this.executor = ExecutorFactory.newFixedThreadPool(this.maxThreads, 1024, parallelScannerThreadFactory);
        }
    }

    @Override
    public void destroy() throws Exception {
        StopWatch stopWatch = StopWatch.createStarted();

        if (isClose.compareAndSet(false, true)) {
            logger.info("HBaseTemplate2.destroy()");
            final ExecutorService executor = this.executor;
            if (executor != null) {
                executor.shutdown();
                try {
                    executor.awaitTermination(DEFAULT_DESTORY_TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            long remainingTime = Math.max(DEFAULT_DESTORY_TIMEOUT - stopWatch.stop(), 100);
            awaitAsyncPutOpsCleared(remainingTime, 50);
        }
    }

    private boolean awaitAsyncPutOpsCleared(long waitTimeout, long checkUnitTime) {
        StopWatch stopWatch = StopWatch.createStarted();

        while (true) {
            Long currentPutOpsCount = asyncOperation.getCurrentOpsCount();
            if (currentPutOpsCount <= 0L) {
                return true;
            }

            if (stopWatch.stop() > waitTimeout) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Incomplete asynchronous operation exists. operations={}, waitTimeout={}, checkUnitTime={}", currentPutOpsCount, waitTimeout, checkUnitTime);
                }
                return false;
            }

            if (logger.isWarnEnabled()) {
                logger.warn("Waiting for asynchronous operation to complete. operations={}, waitTimeout={}, checkUnitTime={}", currentPutOpsCount, waitTimeout, checkUnitTime);
            }

            try {
                Thread.sleep(checkUnitTime);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private void assertAccessAvailable() {
        if (isClose.get()) {
            throw new HBaseAccessException("Already closed");
        }
    }

    @Override
    public <T> T find(TableName tableName, final Scan scan, final ResultsExtractor<T> action) {
        return execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(Table table) throws Throwable {
                try (ResultScanner scanner = table.getScanner(scan)) {
                    return action.extractData(scanner);
                }
            }
        });
    }

    @Override
    public <T> List<T> find(TableName tableName, final Scan scan, final RowMapper<T> action) {
        return find(tableName, scan, new RowMapperResultsExtractor<>(action));
    }

    @Override
    public <T> T get(TableName tableName, byte[] rowName, RowMapper<T> mapper) {
        return get0(tableName, rowName, null, null, mapper);
    }

    @Override
    public <T> T get(TableName tableName, byte[] rowName, byte[] familyName, RowMapper<T> mapper) {
        return get0(tableName, rowName, familyName, null, mapper);
    }

    @Override
    public <T> T get(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final RowMapper<T> mapper) {
        return get0(tableName, rowName, familyName, qualifier, mapper);
    }

    private <T> T get0(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final RowMapper<T> mapper) {
        return execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(Table table) throws Throwable {
                Get get = new Get(rowName);

                if (familyName != null) {
                    if (qualifier != null) {
                        get.addColumn(familyName, qualifier);
                    } else {
                        get.addFamily(familyName);
                    }
                }

                Result result = table.get(get);
                return mapper.mapRow(result, 0);
            }
        });
    }


    @Override
    public <T> T get(TableName tableName, final Get get, final RowMapper<T> mapper) {
        return execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(Table table) throws Throwable {
                Result result = table.get(get);
                return mapper.mapRow(result, 0);
            }
        });
    }

    @Override
    public <T> List<T> get(TableName tableName, final List<Get> getList, final RowMapper<T> mapper) {
        return execute(tableName, new TableCallback<List<T>>() {
            @Override
            public List<T> doInTable(Table table) throws Throwable {
                Result[] result = table.get(getList);
                List<T> list = new ArrayList<>(result.length);
                for (int i = 0; i < result.length; i++) {
                    T t = mapper.mapRow(result[i], i);
                    list.add(t);
                }
                return list;
            }
        });
    }

    @Override
    public void put(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final byte[] value) {
        put(tableName, rowName, familyName, qualifier, null, value);
    }

    @Override
    public void put(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final Long timestamp, final byte[] value) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(Table table) throws Throwable {
                Put put = createPut(rowName, familyName, timestamp, qualifier, value);
                table.put(put);
                return null;
            }
        });
    }

    @Override
    public <T> void put(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final T value, final ValueMapper<T> mapper) {
        put(tableName, rowName, familyName, qualifier, null, value, mapper);
    }

    @Override
    public <T> void put(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final Long timestamp, final T value, final ValueMapper<T> mapper) {
        execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(Table table) throws Throwable {
                byte[] bytes = mapper.mapValue(value);
                Put put = createPut(rowName, familyName, timestamp, qualifier, bytes);
                table.put(put);
                return null;
            }
        });
    }

    @Override
    public void put(TableName tableName, final Put put) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(Table table) throws Throwable {
                table.put(put);
                return null;
            }
        });
    }

    @Override
    public void put(TableName tableName, final List<Put> puts) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(Table table) throws Throwable {
                table.put(puts);
                return null;
            }
        });
    }

    @Override
    public CheckAndMutateResult checkAndMutate(TableName tableName, CheckAndMutate checkAndMutate) {
        return (CheckAndMutateResult) execute(tableName, new TableCallback() {
            @Override
            public CheckAndMutateResult doInTable(Table table) throws Throwable {
                try {
                    return table.checkAndMutate(checkAndMutate);
                } catch (IOException e) {
                    return CHECK_AND_MUTATE_RESULT_FAILURE;
                }
            }
        });
    }

    @Override
    public List<CheckAndMutateResult> checkAndMutate(TableName tableName, List<CheckAndMutate> checkAndMutates) {
        return (List<CheckAndMutateResult>) execute(tableName, new TableCallback() {
            @Override
            public List<CheckAndMutateResult> doInTable(Table table) throws Throwable {
                try {
                    return table.checkAndMutate(checkAndMutates);
                } catch (IOException e) {
                    return List.of(CHECK_AND_MUTATE_RESULT_FAILURE, CHECK_AND_MUTATE_RESULT_FAILURE);
                }
            }
        });
    }

    /**
     * Atomically checks if a row/family/qualifier value matches the expected
     * value. If it does, it adds the put.  If the passed value is null, the check
     * is for the lack of column (ie: non-existence)
     *
     * @param tableName  target table
     * @param rowName    to check
     * @param familyName column family to check
     * @param qualifier  column qualifier to check
     * @param compareOp  comparison operator to use
     * @param value      the expected value
     * @param put        data to put if check succeeds
     * @return true if the new put was executed, false otherwise
     */
    @Override
    public boolean checkAndPut(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, CompareOperator compareOp, byte[] value, Put put) {

        CheckAndMutate checkAndMutate = CheckAndMutate.newBuilder(rowName)
                .ifMatches(familyName, qualifier, compareOp, value)
                .build(put);

        CheckAndMutateResult result = this.checkAndMutate(tableName, checkAndMutate);
        return result.isSuccess();
    }

    @Override
    public void maxColumnValue(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, long value) {
        final byte[] valBytes = Bytes.toBytes(value);
        Put put = new Put(rowName);
        put.addColumn(familyName, qualifier, valBytes);

        CheckAndMutate checkAndPut = CheckAndMutate.newBuilder(rowName)
                .ifMatches(familyName, qualifier, CompareOperator.EQUAL, null)
                .build(put);

        this.execute(tableName, new TableCallback<Object>() {
            @Override
            public Object doInTable(Table table) throws Throwable {
                CheckAndMutateResult result = table.checkAndMutate(checkAndPut);
                if (result.isSuccess()) {
                    logger.debug("MaxUpdate success for null");
                    return null;
                }
                CheckAndMutate checkAndMax = checkAndMax(rowName, familyName, qualifier, valBytes, put);
                CheckAndMutateResult maxResult = table.checkAndMutate(checkAndMax);
                if (maxResult.isSuccess()) {
                    logger.debug("MaxUpdate success for GREATER");
                } else {
                    logger.trace("MaxUpdate failure for ConcurrentUpdate");
                }
                return null;
            }
        });
    }

    private CheckAndMutate checkAndMax(byte[] rowName, byte[] familyName, byte[] qualifier, byte[] valBytes, Put put) {
        return CheckAndMutate.newBuilder(rowName)
                .ifMatches(familyName, qualifier, CompareOperator.GREATER, valBytes)
                .build(put);
    }

    @Override
    public boolean asyncPut(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, byte[] value) {
        return asyncPut(tableName, rowName, familyName, qualifier, null, value);
    }

    @Override
    public boolean asyncPut(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, Long timestamp, byte[] value) {
        Put put = createPut(rowName, familyName, timestamp, qualifier, value);
        return asyncPut(tableName, put);
    }

    @Override
    public <T> boolean asyncPut(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, T value, ValueMapper<T> mapper) {
        return asyncPut(tableName, rowName, familyName, qualifier, null, value, mapper);
    }

    @Override
    public <T> boolean asyncPut(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, Long timestamp, T value, ValueMapper<T> mapper) {
        byte[] bytes = mapper.mapValue(value);
        Put put = createPut(rowName, familyName, timestamp, qualifier, bytes);
        return asyncPut(tableName, put);
    }

    @Override
    public boolean asyncPut(TableName tableName, Put put) {
        assertAccessAvailable();
        if (asyncOperation.isAvailable()) {
            return asyncOperation.put(tableName, put);
        } else {
            put(tableName, put);
            return true;
        }
    }

    @Override
    public List<Put> asyncPut(TableName tableName, List<Put> puts) {
        assertAccessAvailable();
        if (asyncOperation.isAvailable()) {
            return asyncOperation.put(tableName, puts);
        } else {
            put(tableName, puts);
            return Collections.emptyList();
        }
    }

    private Put createPut(byte[] rowName, byte[] familyName, Long timestamp, byte[] qualifier, byte[] value) {
        Put put = new Put(rowName);
        if (familyName != null) {
            if (timestamp == null) {
                put.addColumn(familyName, qualifier, value);
            } else {
                put.addColumn(familyName, qualifier, timestamp, value);
            }
        }
        return put;
    }

    @Override
    public void delete(TableName tableName, final Delete delete) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(Table table) throws Throwable {
                table.delete(delete);
                return null;
            }
        });
    }

    @Override
    public void delete(TableName tableName, final List<Delete> deletes) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(Table table) throws Throwable {
                table.delete(deletes);
                return null;
            }
        });
    }

    @Override
    public <T> List<T> find(TableName tableName, final List<Scan> scanList, final ResultsExtractor<T> action) {
        return execute(tableName, new TableCallback<List<T>>() {
            @Override
            public List<T> doInTable(Table table) throws Throwable {
                List<T> result = new ArrayList<>(scanList.size());
                for (Scan scan : scanList) {
                    try (ResultScanner scanner = table.getScanner(scan)) {
                        T t = action.extractData(scanner);
                        result.add(t);
                    }
                }
                return result;
            }
        });
    }

    @Override
    public <T> List<List<T>> find(TableName tableName, List<Scan> scanList, RowMapper<T> action) {
        return find(tableName, scanList, new RowMapperResultsExtractor<>(action));
    }

    public <T> List<T> findParallel(final TableName tableName, final List<Scan> scans, final ResultsExtractor<T> action) {
        if (nativeAsync) {
            return findParallel_async(tableName, scans, action);
        }
        return findParallel_block(tableName, scans, action);
    }

    public <T> List<T> findParallel_async(final TableName tableName, final List<Scan> scans, final ResultsExtractor<T> action) {
        assertAccessAvailable();
        if (isSimpleScan(scans)) {
            return find(tableName, scans, action);
        }
        return executeAsync(tableName, new AdvancedAsyncTableCallback<>() {
            @Override
            public List<T> doInTable(AsyncTable<AdvancedScanResultConsumer> table) throws Throwable {
                final Scan[] copy = scans.toArray(new Scan[0]);
                final List<T> results = new ArrayList<>(copy.length);

                ResultScanner[] resultScanners = ScanUtils.newScanners(table, copy);
                for (final ResultScanner scanner : resultScanners) {
                    try (scanner) {
                        T t = action.extractData(scanner);
                        results.add(t);
                    } catch (Throwable th) {
                        // close all scanners if exception occurred
                        ScanUtils.closeScanner(resultScanners);
                        throw new HbaseSystemException(th);
                    }
                }
                return results;
            }
        });
    }


    public <T> List<T> findParallel_block(final TableName tableName, final List<Scan> scans, final ResultsExtractor<T> action) {
        assertAccessAvailable();
        if (isSimpleScan(scans)) {
            return find(tableName, scans, action);
        }
        List<T> results = new ArrayList<>(scans.size());
        List<Callable<T>> callables = new ArrayList<>(scans.size());
        for (final Scan scan : scans) {
            callables.add(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return execute(tableName, new TableCallback<T>() {
                        @Override
                        public T doInTable(Table table) throws Throwable {
                            try (ResultScanner scanner = table.getScanner(scan)) {
                                return action.extractData(scanner);
                            }
                        }
                    });
                }
            });
        }
        List<List<Callable<T>>> callablePartitions = ListUtils.partition(callables, this.maxThreadsPerParallelScan);
        for (List<Callable<T>> callablePartition : callablePartitions) {
            try {
                List<Future<T>> futures = this.executor.invokeAll(callablePartition);
                for (Future<T> future : futures) {
                    results.add(future.get());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("interrupted while findParallel [{}].", tableName);
                return Collections.emptyList();
            } catch (ExecutionException e) {
                logger.warn("findParallel [{}], error : {}", tableName, e);
                return Collections.emptyList();
            }
        }
        return results;
    }

    private boolean isSimpleScan(List<Scan> scans) {
        return isSimpleScan(scans.size());
    }

    private boolean isSimpleScan(int parallelism) {
        return !this.enableParallelScan || parallelism <= 1;
    }

    @Override
    public <T> List<List<T>> findParallel(TableName tableName, final List<Scan> scans, final RowMapper<T> action) {
        return findParallel(tableName, scans, new RowMapperResultsExtractor<>(action));
    }

    @Override
    public <T> List<T> find(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final RowMapper<T> action) {
        final ResultsExtractor<List<T>> resultsExtractor = new RowMapperResultsExtractor<>(action);
        return executeDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor);
    }

    @Override
    public <T> List<T> find(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final int limit, final RowMapper<T> action) {
        final ResultsExtractor<List<T>> resultsExtractor = new LimitRowMapperResultsExtractor<>(action, limit);
        return executeDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor);
    }

    @Override
    public <T> List<T> find(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action, final LimitEventHandler limitEventHandler) {
        final LimitRowMapperResultsExtractor<T> resultsExtractor = new LimitRowMapperResultsExtractor<>(action, limit, limitEventHandler);
        return executeDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor);
    }

    @Override
    public <T> T find(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action) {
        return executeDistributedScan(tableName, scan, rowKeyDistributor, action);
    }

    protected final <T> T executeDistributedScan(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action) {
        if (nativeAsync) {
            return executeDistributedScan_async(tableName, scan, rowKeyDistributor, action);
        }
        return executeDistributedScan_block(tableName, scan, rowKeyDistributor, action);
    }

    protected final <T> T executeDistributedScan_block(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action) {
        assertAccessAvailable();
        return execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(Table table) throws Throwable {
                final StopWatch watch = StopWatch.createStarted();
                final boolean debugEnabled = logger.isDebugEnabled();

                Scan[] scans = ScanUtils.splitScans(scan, rowKeyDistributor);
                final ResultScanner[] splitScanners = ScanUtils.newScanners(table, scans);
                try (ResultScanner scanner = new DistributedScanner(rowKeyDistributor, splitScanners)) {
                    if (debugEnabled) {
                        logger.debug("DistributedScanner createTime: {}ms", watch.stop());
                    }
                    return action.extractData(scanner);
                } finally {
                    if (debugEnabled) {
                        logger.debug("DistributedScanner scanTime: {}ms", watch.stop());
                    }
                }
            }
        });
    }

    protected final <T> T executeDistributedScan_async(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action) {
        assertAccessAvailable();
        final T result = executeAsync(tableName, new AdvancedAsyncTableCallback<T>() {
            @Override
            public T doInTable(AsyncTable<AdvancedScanResultConsumer> table) throws Throwable {
                final StopWatch watch = StopWatch.createStarted();
                final boolean debugEnabled = logger.isDebugEnabled();

                Scan[] scans = ScanUtils.splitScans(scan, rowKeyDistributor);
                final ResultScanner[] splitScanners = ScanUtils.newScanners(table, scans);
                try (ResultScanner scanner = new DistributedScanner(rowKeyDistributor, splitScanners)) {
                    if (debugEnabled) {
                        logger.debug("DistributedScanner createTime: {}ms", watch.stop());
                        watch.start();
                    }
                    return action.extractData(scanner);
                } finally {
                    if (debugEnabled) {
                        logger.debug("DistributedScanner scanTime: {}ms", watch.stop());
                    }
                }
            }
        });
        return result;
    }


    @Override
    public <T> List<T> findParallel(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, RowMapper<T> action, int numParallelThreads) {
        if (isSimpleScan(numParallelThreads)) {
            // use DistributedScanner if parallel scan is disabled or if called to use a single thread
            return find(tableName, scan, rowKeyDistributor, action);
        } else {
            int numThreadsUsed = getThreadsUsedNum(numParallelThreads);
            final ResultsExtractor<List<T>> resultsExtractor = new RowMapperResultsExtractor<>(action);
            return executeParallelDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor, numThreadsUsed);
        }
    }

    private int getThreadsUsedNum(int numParallelThreads) {
        return Math.min(numParallelThreads, this.maxThreadsPerParallelScan);
    }

    @Override
    public <T> List<T> findParallel(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action, int numParallelThreads) {
        if (isSimpleScan(numParallelThreads)) {
            // use DistributedScanner if parallel scan is disabled or if called to use a single thread
            return find(tableName, scan, rowKeyDistributor, limit, action);
        } else {
            int numThreadsUsed = getThreadsUsedNum(numParallelThreads);
            final ResultsExtractor<List<T>> resultsExtractor = new LimitRowMapperResultsExtractor<>(action, limit);
            return executeParallelDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor, numThreadsUsed);
        }
    }

    @Override
    public <T> List<T> findParallel(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action, LimitEventHandler limitEventHandler, int numParallelThreads) {
        if (isSimpleScan(numParallelThreads)) {
            // use DistributedScanner if parallel scan is disabled or if called to use a single thread
            return find(tableName, scan, rowKeyDistributor, limit, action, limitEventHandler);
        } else {
            int numThreadsUsed = getThreadsUsedNum(numParallelThreads);
            final LimitRowMapperResultsExtractor<T> resultsExtractor = new LimitRowMapperResultsExtractor<>(action, limit, limitEventHandler);
            return executeParallelDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor, numThreadsUsed);
        }
    }

    @Override
    public <T> T findParallel(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> action, int numParallelThreads) {
        if (isSimpleScan(numParallelThreads)) {
            // use DistributedScanner if parallel scan is disabled or if called to use a single thread
            return find(tableName, scan, rowKeyDistributor, action);
        } else {
            int numThreadsUsed = getThreadsUsedNum(numParallelThreads);
            return executeParallelDistributedScan(tableName, scan, rowKeyDistributor, action, numThreadsUsed);
        }
    }

    protected final <T> T executeParallelDistributedScan(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> action, int numParallelThreads) {
        if (nativeAsync) {
            return executeParallelDistributedScan_async(tableName, scan, rowKeyDistributor, action, numParallelThreads);
        }
        return executeParallelDistributedScan_block(tableName, scan, rowKeyDistributor, action, numParallelThreads);
    }

    protected final <T> T executeParallelDistributedScan_async(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> action, int numParallelThreads) {
        assertAccessAvailable();
        try {
            StopWatch watch = StopWatch.createStarted();
            final boolean debugEnabled = logger.isDebugEnabled();

            final Scan[] scans = ScanUtils.splitScans(scan, rowKeyDistributor);
            T result = executeAsync(tableName, new AdvancedAsyncTableCallback<T>() {
                @Override
                public T doInTable(AsyncTable<AdvancedScanResultConsumer> table) throws Throwable {
                    ResultScanner[] resultScanners = ScanUtils.newScanners(table, scans);

                    ResultScanner scanner = new AsyncDistributedScanner(rowKeyDistributor, resultScanners);
                    try (scanner) {
                        return action.extractData(scanner);
                    }
                }
            });

            if (debugEnabled) {
                logger.debug("executeParallelDistributedScan createTime: {}ms", watch.stop());
            }
            return result;
        } catch (Exception e){
            throw new HbaseSystemException(e);
        }
    }


    protected final <T> T executeParallelDistributedScan_block(TableName tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> action, int numParallelThreads) {
        assertAccessAvailable();
        try {
            StopWatch watch = StopWatch.createStarted();
            final boolean debugEnabled = logger.isDebugEnabled();

            try (ParallelResultScanner scanner = new ParallelResultScanner(tableName, this, this.executor, scan, rowKeyDistributor, numParallelThreads)) {
                if (debugEnabled) {
                    logger.debug("ParallelDistributedScanner createTime: {}ms", watch.stop());
                    watch.start();
                }
                return action.extractData(scanner);
            } finally {
                if (debugEnabled) {
                    logger.debug("ParallelDistributedScanner scanTime: {}ms", watch.stop());
                }
            }
        } catch (Throwable th) {
            Throwable throwable = th;
            if (th instanceof ScanTaskException) {
                throwable = th.getCause();
            }
            return rethrowHbaseException(throwable);
        }
    }

    @Override
    public Result increment(TableName tableName, final Increment increment) {
        return execute(tableName, new TableCallback<Result>() {
            @Override
            public Result doInTable(Table table) throws Throwable {
                return table.increment(increment);
            }
        });
    }

    @Override
    public List<Result> increment(final TableName tableName, final List<Increment> incrementList) {
        return execute(tableName, new TableCallback<List<Result>>() {
            @Override
            public List<Result> doInTable(Table table) throws Throwable {
                final List<Result> resultList = new ArrayList<>(incrementList.size());

                Exception lastException = null;
                for (Increment increment : incrementList) {
                    try {
                        Result result = table.increment(increment);
                        resultList.add(result);
                    } catch (IOException e) {
                        logger.warn("{} increment error Caused:{}", tableName, e.getMessage(), e);
                        lastException = e;
                    }
                }
                if (lastException != null) {
                    throw lastException;
                }
                return resultList;
            }
        });
    }

    @Override
    public long incrementColumnValue(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount) {
        return execute(tableName, new TableCallback<Long>() {
            @Override
            public Long doInTable(Table table) throws Throwable {
                return table.incrementColumnValue(rowName, familyName, qualifier, amount);
            }
        });
    }

    @Override
    public long incrementColumnValue(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount, final boolean writeToWAL) {
        return execute(tableName, new TableCallback<Long>() {
            @Override
            public Long doInTable(Table table) throws Throwable {
                return table.incrementColumnValue(rowName, familyName, qualifier, amount, writeToWAL ? Durability.SKIP_WAL : Durability.USE_DEFAULT);
            }
        });
    }

    @Override
    public <T> T execute(TableName tableName, TableCallback<T> action) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(action, "action");
        assertAccessAvailable();

        Table table = getTable(tableName);

        try {
            T result = action.doInTable(table);
            return result;
        } catch (Throwable e) {
            return rethrowHbaseException(e);
        } finally {
            releaseTable(table);
        }
    }

    private void releaseTable(Table table) {
        getTableFactory().releaseTable(table);
    }


    @Override
    public <T> T executeAsync(TableName tableName, AdvancedAsyncTableCallback<T> action) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(action, "action");
        assertAccessAvailable();

        AsyncTable<AdvancedScanResultConsumer> table = getAdvancedAsyncTable(tableName);

        try {
            T result = action.doInTable(table);
            return result;
        } catch (Throwable e) {
            return rethrowHbaseException(e);
        }
    }

    private <T> T rethrowHbaseException(Throwable e) {
        if (e instanceof RuntimeException) {
            throw ((RuntimeException) e);
        }
        if (e instanceof Error) {
            throw ((Error) e);
        }
        throw new HbaseSystemException((Exception) e);
    }


    @Override
    public <T> T executeAsync(TableName tableName, AsyncTableCallback<T> action) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(action, "action");
        assertAccessAvailable();

        AsyncTable<ScanResultConsumer> table = getAsyncTable(tableName);

        try {
            T result = action.doInTable(table);
            return result;
        } catch (Throwable e) {
            return rethrowHbaseException(e);
        }
    }

    private AsyncTable<ScanResultConsumer> getAsyncTable(TableName tableName) {
        return getAsyncTableFactory().getTable(tableName, executor);
    }

    private AsyncTable<AdvancedScanResultConsumer> getAdvancedAsyncTable(TableName tableName) {
        return getAsyncTableFactory().getTable(tableName);
    }
}
