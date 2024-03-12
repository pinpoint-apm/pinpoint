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
import com.navercorp.pinpoint.common.hbase.async.AsyncTableCallback;
import com.navercorp.pinpoint.common.hbase.future.FutureDecorator;
import com.navercorp.pinpoint.common.hbase.future.FutureLoggingDecorator;
import com.navercorp.pinpoint.common.hbase.parallel.ParallelResultScanner;
import com.navercorp.pinpoint.common.hbase.parallel.ScanTaskException;
import com.navercorp.pinpoint.common.hbase.scan.ResultScannerFactory;
import com.navercorp.pinpoint.common.hbase.scan.ScanUtils;
import com.navercorp.pinpoint.common.hbase.scan.Scanner;
import com.navercorp.pinpoint.common.hbase.util.CheckAndMutates;
import com.navercorp.pinpoint.common.hbase.util.EmptyScanMetricReporter;
import com.navercorp.pinpoint.common.hbase.util.MutationType;
import com.navercorp.pinpoint.common.hbase.util.ScanMetricReporter;
import com.navercorp.pinpoint.common.profiler.concurrent.ExecutorFactory;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.common.util.StopWatch;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import com.sematext.hbase.wd.DistributedScanner;
import org.apache.commons.collections4.ListUtils;
import org.apache.hadoop.conf.Configuration;
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
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * @author emeroad
 * @author HyunGil Jeong
 * @author minwoo.jung
 * @author Taejin Koo
 */
public class HbaseTemplate extends HbaseAccessor implements HbaseOperations, InitializingBean, DisposableBean {

    private static final int DEFAULT_MAX_THREADS_FOR_PARALLEL_SCANNER = 128;
    private static final int DEFAULT_MAX_THREADS_PER_PARALLEL_SCAN = 1;

    private static final long DEFAULT_DESTORY_TIMEOUT = 2000;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AtomicBoolean isClose = new AtomicBoolean(false);

    private ExecutorService executor;
    private boolean enableParallelScan = false;
    private int maxThreads = DEFAULT_MAX_THREADS_FOR_PARALLEL_SCANNER;
    private int maxThreadsPerParallelScan = DEFAULT_MAX_THREADS_PER_PARALLEL_SCAN;


    private final FutureDecorator futureDecorator = new FutureLoggingDecorator(logger);
    private boolean nativeAsync = false;

    private static final CheckAndMutateResult CHECK_AND_MUTATE_RESULT_FAILURE = new CheckAndMutateResult(false, null);

    private ScanMetricReporter scanMetric = new EmptyScanMetricReporter();

    private final ResultScannerFactory scannerFactory = new ResultScannerFactory(1024 * 2);

    public HbaseTemplate() {
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

    public void setNativeAsync(boolean nativeAsync) {
        this.nativeAsync = nativeAsync;
    }

    public void setScanMetricReporter(ScanMetricReporter scanReporter) {
        this.scanMetric = scanReporter;
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

        if (isClose.compareAndSet(false, true)) {
            logger.info("HBaseTemplate.destroy()");
            final ExecutorService executor = this.executor;
            if (executor != null) {
                executor.shutdown();
                try {
                    executor.awaitTermination(DEFAULT_DESTORY_TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
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
        return execute(tableName, new TableCallback<>() {
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
    public <T> T get(TableName tableName, final Get get, final RowMapper<T> mapper) {
        return execute(tableName, new TableCallback<>() {
            @Override
            public T doInTable(Table table) throws Throwable {
                Result result = table.get(get);
                return mapper.mapRow(result, 0);
            }
        });
    }

    @Override
    public <T> List<T> get(TableName tableName, final List<Get> getList, final RowMapper<T> mapper) {
        return execute(tableName, new TableCallback<>() {
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
    public void put(TableName tableName, final Put put) {
        if (nativeAsync) {
            asyncExecute(tableName, new AsyncTableCallback<>() {
                @Override
                public Void doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                    CompletableFuture<Void> future = table.put(put);
                    futureDecorator.apply(future, tableName, MutationType.PUT);
                    return null;
                }
            });
        } else {
            execute(tableName, new TableCallback<>() {
                @Override
                public Void doInTable(Table table) throws Throwable {
                    table.put(put);
                    return null;
                }
            });
        }
    }

    @Override
    public void put(TableName tableName, final List<Put> puts) {
        if (nativeAsync) {
            asyncExecute(tableName, new AsyncTableCallback<>() {
                @Override
                public Void doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                    List<CompletableFuture<Void>> futures = table.put(puts);
                    futureDecorator.apply(futures, tableName, MutationType.PUT);
                    return null;
                }
            });
            return;
        }
        execute(tableName, new TableCallback<>() {
            @Override
            public Object doInTable(Table table) throws Throwable {
                table.put(puts);
                return null;
            }
        });
    }

    @Override
    public CheckAndMutateResult checkAndMutate(TableName tableName, CheckAndMutate checkAndMutate) {
        return execute(tableName, new TableCallback<>() {
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
        if (nativeAsync) {
            return asyncExecute(tableName, new AsyncTableCallback<>() {

                @Override
                public List<CheckAndMutateResult> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                    final List<CheckAndMutateResult> result = new ArrayList<>(checkAndMutates.size());

                    List<CompletableFuture<CheckAndMutateResult>> futures = table.checkAndMutate(checkAndMutates);
                    futureDecorator.apply(futures, tableName, MutationType.CHECK_AND_MUTATE);

                    for (CompletableFuture<CheckAndMutateResult> future : futures) {
                        result.add(future.join());
                    }
                    return result;
                }
            });
        }
        return execute(tableName, new TableCallback<>() {
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


    @Override
    public void maxColumnValue(TableName tableName, byte[] rowName, byte[] familyName, byte[] qualifier, long value) {
        final byte[] valBytes = Bytes.toBytes(value);
        Put put = new Put(rowName);
        put.addColumn(familyName, qualifier, valBytes);

        CheckAndMutate checkAndPut = CheckAndMutate.newBuilder(rowName)
                .ifNotExists(familyName, qualifier)
                .build(put);

//        this.execute(tableName, new TableCallback<Object>() {
//            @Override
//            public Object doInTable(Table table) throws Throwable {
//                CheckAndMutateResult result = table.checkAndMutate(checkAndPut);
//                if (result.isSuccess()) {
//                    logger.debug("MaxUpdate success for null");
//                    return null;
//                }
//                CheckAndMutate checkAndMax = checkAndMax(rowName, familyName, qualifier, valBytes, put);
//                CheckAndMutateResult maxResult = table.checkAndMutate(checkAndMax);
//                if (maxResult.isSuccess()) {
//                    logger.debug("MaxUpdate success for GREATER");
//                } else {
//                    logger.trace("MaxUpdate failure for ConcurrentUpdate");
//                }
//                return null;
//            }
//        });

        this.asyncExecute(tableName, new AsyncTableCallback<>() {
            @Override
            public Void doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                CompletableFuture<CheckAndMutateResult> result = table.checkAndMutate(checkAndPut);
                result.whenCompleteAsync(new BiConsumer<CheckAndMutateResult, Throwable>() {
                    @Override
                    public void accept(CheckAndMutateResult checkAndMutateResult, Throwable throwable) {
                        if (throwable != null) {
                            logger.warn("{} MaxUpdate(EQUALS) failure", tableName, throwable);
                            return;
                        }
                        if (checkAndMutateResult.isSuccess()) {
                            logger.debug("{} MaxUpdate(EQUALS) success", tableName);
                        } else {
                            CheckAndMutate checkAndMax = CheckAndMutates.max(rowName, familyName, qualifier, valBytes, put);
                            CompletableFuture<CheckAndMutateResult> maxFuture = table.checkAndMutate(checkAndMax);
                            maxFuture.whenComplete(new BiConsumer<CheckAndMutateResult, Throwable>() {
                                @Override
                                public void accept(CheckAndMutateResult maxResult, Throwable throwable) {
                                    if (throwable != null) {
                                        logger.warn("{} MaxUpdate(GREATER) exceptionally", tableName, throwable);
                                        return;
                                    }
                                    if (maxResult.isSuccess()) {
                                        logger.debug("{} MaxUpdate(GREATER) success", tableName);
                                    } else {
                                        logger.trace("{} MaxUpdate(GREATER) failure", tableName);
                                    }
                                }
                            });
                        }
                    }
                });
                return null;
            }
        });
    }


    @Override
    public void delete(TableName tableName, final Delete delete) {
        execute(tableName, new TableCallback<>() {
            @Override
            public Void doInTable(Table table) throws Throwable {
                table.delete(delete);
                return null;
            }
        });
    }

    @Override
    public void delete(TableName tableName, final List<Delete> deletes) {
        execute(tableName, new TableCallback<>() {
            @Override
            public Void doInTable(Table table) throws Throwable {
                table.delete(deletes);
                return null;
            }
        });
    }

    @Override
    public <T> List<T> find(TableName tableName, final List<Scan> scanList, final ResultsExtractor<T> action) {
        return execute(tableName, new TableCallback<>() {
            @Override
            public List<T> doInTable(Table table) throws Throwable {
                Scan[] copy = scanList.toArray(new Scan[0]);
                final ScanMetricReporter.Reporter reporter = scanMetric.newReporter(tableName, "find", copy);
                Scanner<T> scanner = scannerFactory.newScanner(table, copy);

                List<T> result = scanner.extractData(action);

                reporter.report(scanner::getScanMetrics);
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
        if (isSimpleScan(scans.size())) {
            return find(tableName, scans, action);
        }
        return asyncExecute(tableName, new AdvancedAsyncTableCallback<>() {
            @Override
            public List<T> doInTable(AsyncTable<AdvancedScanResultConsumer> table) throws Throwable {
                final Scan[] copy = scans.toArray(new Scan[0]);

                final ScanMetricReporter.Reporter reporter = scanMetric.newReporter(tableName, "async-multi", copy);

                Scanner<T> scanner = scannerFactory.newScanner(table, copy);
                List<T> results = scanner.extractData(action);
                reporter.report(scanner::getScanMetrics);
                return results;
            }
        });
    }


    public <T> List<T> findParallel_block(final TableName tableName, final List<Scan> scans, final ResultsExtractor<T> action) {
        assertAccessAvailable();
        final Scan[] copy = scans.toArray(new Scan[0]);
        if (isSimpleScan(copy.length)) {
            return find(tableName, scans, action);
        }
        
        final ScanMetricReporter.Reporter reporter = scanMetric.newReporter(tableName, "block-multi", copy);
        final Collection<ScanMetrics> scanMetricsList = new ArrayBlockingQueue<>(copy.length);

        List<Callable<T>> callables = callable(tableName, action, copy, scanMetricsList);

        List<T> results = new ArrayList<>(copy.length);
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
        reporter.report(scanMetricsList);
        return results;
    }

    private <T> List<Callable<T>> callable(TableName tableName, ResultsExtractor<T> action, Scan[] scans, Collection<ScanMetrics> scanMetricsList) {
        List<Callable<T>> callables = new ArrayList<>(scans.length);

        for (Scan scan: scans) {
            callables.add(new Callable<T>() {

                @Override
                public T call() throws Exception {
                    return execute(tableName, new TableCallback<>() {
                        @Override
                        public T doInTable(Table table) throws Throwable {
                            ResultScanner scanner = table.getScanner(scan);
                            try {
                                return action.extractData(scanner);
                            } finally {
                                IOUtils.closeQuietly(scanner);
                                scanMetricsList.add(scanner.getScanMetrics());
                            }
                        }
                    });
                }
            });
        }
        return callables;
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
        return execute(tableName, new TableCallback<>() {
            @Override
            public T doInTable(Table table) throws Throwable {
                final StopWatch watch = StopWatch.createStarted();
                final boolean debugEnabled = logger.isDebugEnabled();

                Scan[] scans = ScanUtils.splitScans(scan, rowKeyDistributor);
                final ScanMetricReporter.Reporter reporter = scanMetric.newReporter(tableName, "block-multi", scans);

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
                    reporter.report(splitScanners);
                }
            }
        });
    }

    protected final <T> T executeDistributedScan_async(TableName tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action) {
        assertAccessAvailable();

        final T result = asyncExecute(tableName, new AdvancedAsyncTableCallback<>() {
            @Override
            public T doInTable(AsyncTable<AdvancedScanResultConsumer> table) throws Throwable {
                final StopWatch watch = StopWatch.createStarted();
                final boolean debugEnabled = logger.isDebugEnabled();

                Scan[] scans = ScanUtils.splitScans(scan, rowKeyDistributor);
                final ScanMetricReporter.Reporter reporter = scanMetric.newReporter(tableName, "async-multi", scans);
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
                    reporter.report(splitScanners);
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

            final Scan[] scans = ScanUtils.splitScans(scan, rowKeyDistributor);
            T result = asyncExecute(tableName, new AdvancedAsyncTableCallback<T>() {
                @Override
                public T doInTable(AsyncTable<AdvancedScanResultConsumer> table) throws Throwable {
                    ScanMetricReporter.Reporter reporter = scanMetric.newReporter(tableName, "async-multi", scans);
                    ResultScanner[] resultScanners = ScanUtils.newScanners(table, scans);

                    ResultScanner scanner = new DistributedScanner(rowKeyDistributor, resultScanners);
                    try (scanner) {
                        return action.extractData(scanner);
                    } finally {
                        reporter.report(resultScanners);
                    }
                }
            });

            if (logger.isDebugEnabled()) {
                logger.debug("executeParallelDistributedScan createTime: {}ms", watch.stop());
            }
            return result;
        } catch (Exception e) {
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
        return execute(tableName, new TableCallback<>() {
            @Override
            public Result doInTable(Table table) throws Throwable {
                return table.increment(increment);
            }
        });
    }

    @Override
    public CompletableFuture<Long> asyncIncrement(TableName tableName, byte[] row, byte[] family, byte[] qualifier, long amount, Durability durability) {
        return asyncExecute(tableName, new AsyncTableCallback<CompletableFuture<Long>>() {
            @Override
            public CompletableFuture<Long> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                CompletableFuture<Long> future = table.incrementColumnValue(row, family, qualifier, amount, durability);

                futureDecorator.apply(future, table.getName(), MutationType.INCREMENT);
                return future;
            }
        });
    }


    @Override
    public List<Result> increment(final TableName tableName, final List<Increment> incrementList) {
        if (nativeAsync) {
            AsyncTable<ScanResultConsumer> table = getAsyncTable(tableName);

            @SuppressWarnings("unchecked")
            CompletableFuture<Result>[] futures = new CompletableFuture[incrementList.size()];

            for (int i = 0; i < incrementList.size(); i++) {
                Increment increment = incrementList.get(i);
                CompletableFuture<Result> result = table.increment(increment);
                futures[i] = result;
            }
            List<Result> results = new ArrayList<>(futures.length);
            for (CompletableFuture<Result> future : futures) {
                results.add(future.join());
            }
            return results;
        }

        return execute(tableName, new TableCallback<>() {
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
    public List<CompletableFuture<Result>> asyncIncrement(final TableName tableName, final List<Increment> incrementList) {
        return asyncExecute(tableName, new AsyncTableCallback<List<CompletableFuture<Result>>>() {
            @Override
            public List<CompletableFuture<Result>> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                List<CompletableFuture<Result>> results = new ArrayList<>(incrementList.size());
                for (Increment increment : incrementList) {
                    CompletableFuture<Result> result = table.increment(increment);
                    futureDecorator.apply(result, tableName, MutationType.INCREMENT);
                    results.add(result);
                }
                return results;
            }
        });
    }

    @Override
    public CompletableFuture<Result> asyncIncrement(final TableName tableName, Increment increment) {
        return asyncExecute(tableName, new AsyncTableCallback<CompletableFuture<Result>>() {
            @Override
            public CompletableFuture<Result> doInTable(AsyncTable<ScanResultConsumer> table) throws Throwable {
                CompletableFuture<Result> future = table.increment(increment);
                futureDecorator.apply(future, tableName, MutationType.INCREMENT);
                return future;
            }
        });
    }

    @Override
    public long incrementColumnValue(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount) {
        return execute(tableName, new TableCallback<>() {
            @Override
            public Long doInTable(Table table) throws Throwable {
                return table.incrementColumnValue(rowName, familyName, qualifier, amount);
            }
        });
    }

    @Override
    public long incrementColumnValue(TableName tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount, final Durability durability) {
        return execute(tableName, new TableCallback<>() {
            @Override
            public Long doInTable(Table table) throws Throwable {
                return table.incrementColumnValue(rowName, familyName, qualifier, amount, durability);
            }
        });
    }

    @Override
    public <T> T execute(TableName tableName, TableCallback<T> action) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(action, "action");
        assertAccessAvailable();

        final Table table = getTable(tableName);

        try {
            return action.doInTable(table);
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
    public <T> T asyncExecute(TableName tableName, AdvancedAsyncTableCallback<T> action) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(action, "action");
        assertAccessAvailable();

        final AsyncTable<AdvancedScanResultConsumer> table = getAdvancedAsyncTable(tableName);
        try {
            return action.doInTable(table);
        } catch (Throwable e) {
            return rethrowHbaseException(e);
        }
    }

    private <T> T rethrowHbaseException(Throwable e) {
        if (e instanceof RuntimeException ex) {
            throw ex;
        }
        if (e instanceof Error error) {
            throw error;
        }
        throw new HbaseSystemException(e);
    }


    @Override
    public <T> T asyncExecute(TableName tableName, AsyncTableCallback<T> action) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(action, "action");
        assertAccessAvailable();

        final AsyncTable<ScanResultConsumer> table = getAsyncTable(tableName);
        try {
            return action.doInTable(table);
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
