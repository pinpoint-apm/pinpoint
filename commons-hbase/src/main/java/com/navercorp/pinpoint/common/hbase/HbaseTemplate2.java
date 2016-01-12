/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.hbase.parallel.ParallelResultScanner;
import com.navercorp.pinpoint.common.hbase.parallel.ScanTaskException;
import com.navercorp.pinpoint.common.util.ExecutorFactory;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.StopWatch;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import com.sematext.hbase.wd.DistributedScanner;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.data.hadoop.hbase.TableCallback;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class HbaseTemplate2 extends HbaseTemplate implements HbaseOperations2, InitializingBean, DisposableBean {

    private static final int DEFAULT_MAX_THREADS_FOR_PARALLEL_SCANNER = 128;
    private static final int DEFAULT_MAX_THREADS_PER_PARALLEL_SCAN = 1;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean debugEnabled = this.logger.isDebugEnabled();

    private ExecutorService executor;
    private boolean enableParallelScan = false;
    private int maxThreads = DEFAULT_MAX_THREADS_FOR_PARALLEL_SCANNER;
    private int maxThreadsPerParallelScan = DEFAULT_MAX_THREADS_PER_PARALLEL_SCAN;

    public HbaseTemplate2() {
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

    public HbaseTemplate2(Configuration configuration) {
        Assert.notNull(configuration);
        setConfiguration(configuration);
    }

    @Override
    public void afterPropertiesSet() {
        Configuration configuration = getConfiguration();
        Assert.notNull(configuration, "configuration is required");
        Assert.notNull(getTableFactory(), "tableFactory is required");
        PinpointThreadFactory parallelScannerThreadFactory = new PinpointThreadFactory("Pinpoint-parallel-scanner");
        if (this.maxThreadsPerParallelScan <= 1) {
            this.enableParallelScan = false;
            this.executor = Executors.newSingleThreadExecutor(parallelScannerThreadFactory);
        } else {
            this.executor = ExecutorFactory.newFixedThreadPool(this.maxThreads, 1024, parallelScannerThreadFactory);
        }
    }

    @Override
    public void destroy() throws Exception {
        logger.info("HbaseTemplate2.destroy()");
        final ExecutorService executor = this.executor;
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public <T> T find(String tableName, String family, final ResultsExtractor<T> action) {
        Scan scan = new Scan();
        scan.addFamily(family.getBytes(getCharset()));
        return find(tableName, scan, action);
    }

    @Override
    public <T> T find(String tableName, String family, String qualifier, final ResultsExtractor<T> action) {
        Scan scan = new Scan();
        scan.addColumn(family.getBytes(getCharset()), qualifier.getBytes(getCharset()));
        return find(tableName, scan, action);
    }

    @Override
    public <T> T find(String tableName, final Scan scan, final ResultsExtractor<T> action) {
        return execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(HTableInterface htable) throws Throwable {
                final ResultScanner scanner = htable.getScanner(scan);
                try {
                    return action.extractData(scanner);
                } finally {
                    scanner.close();
                }
            }
        });
    }

    @Override
    public <T> List<T> find(String tableName, String family, final RowMapper<T> action) {
        Scan scan = new Scan();
        scan.addFamily(family.getBytes(getCharset()));
        return find(tableName, scan, action);
    }

    @Override
    public <T> List<T> find(String tableName, String family, String qualifier, final RowMapper<T> action) {
        Scan scan = new Scan();
        scan.addColumn(family.getBytes(getCharset()), qualifier.getBytes(getCharset()));
        return find(tableName, scan, action);
    }

    @Override
    public <T> List<T> find(String tableName, final Scan scan, final RowMapper<T> action) {
        return find(tableName, scan, new RowMapperResultsExtractor<>(action));
    }

    @Override
    public <T> T get(String tableName, String rowName, final RowMapper<T> mapper) {
        return get(tableName, rowName, null, null, mapper);
    }

    @Override
    public <T> T get(String tableName, String rowName, String familyName, final RowMapper<T> mapper) {
        return get(tableName, rowName, familyName, null, mapper);
    }

    @Override
    public <T> T get(String tableName, final String rowName, final String familyName, final String qualifier, final RowMapper<T> mapper) {
        return execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(HTableInterface htable) throws Throwable {
                Get get = new Get(rowName.getBytes(getCharset()));
                if (familyName != null) {
                    byte[] family = familyName.getBytes(getCharset());

                    if (qualifier != null) {
                        get.addColumn(family, qualifier.getBytes(getCharset()));
                    } else {
                        get.addFamily(family);
                    }
                }
                Result result = htable.get(get);
                return mapper.mapRow(result, 0);
            }
        });
    }

    @Override
    public <T> T get(String tableName, byte[] rowName, RowMapper<T> mapper) {
        return get(tableName, rowName, null, null, mapper);
    }

    @Override
    public <T> T get(String tableName, byte[] rowName, byte[] familyName, RowMapper<T> mapper) {
        return get(tableName, rowName, familyName, null, mapper);
    }

    @Override
    public <T> T get(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final RowMapper<T> mapper) {
        return execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(HTableInterface htable) throws Throwable {
                Get get = new Get(rowName);
                if (familyName != null) {
                    if (qualifier != null) {
                        get.addColumn(familyName, qualifier);
                    } else {
                        get.addFamily(familyName);
                    }
                }
                Result result = htable.get(get);
                return mapper.mapRow(result, 0);
            }
        });
    }

    @Override
    public <T> T get(String tableName, final Get get, final RowMapper<T> mapper) {
        return execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(HTableInterface htable) throws Throwable {
                Result result = htable.get(get);
                return mapper.mapRow(result, 0);
            }
        });
    }

    @Override
    public <T> List<T> get(String tableName, final List<Get> getList, final RowMapper<T> mapper) {
        return execute(tableName, new TableCallback<List<T>>() {
            @Override
            public List<T> doInTable(HTableInterface htable) throws Throwable {
                Result[] result = htable.get(getList);
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
    public void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final byte[] value) {
        put(tableName, rowName, familyName, qualifier, null, value);
    }

    @Override
    public void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final Long timestamp, final byte[] value) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                Put put = new Put(rowName);
                if (familyName != null) {
                    if (timestamp == null) {
                        put.addColumn(familyName, qualifier, value);
                    } else {
                        put.addColumn(familyName, qualifier, timestamp, value);
                    }
                }
                htable.put(put);
                return null;
            }
        });
    }

    @Override
    public <T> void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final T value, final ValueMapper<T> mapper) {
        put(tableName, rowName, familyName, qualifier, null, value, mapper);
    }

    @Override
    public <T> void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final Long timestamp, final T value, final ValueMapper<T> mapper) {
        execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(HTableInterface htable) throws Throwable {
                Put put = new Put(rowName);
                byte[] bytes = mapper.mapValue(value);
                if (familyName != null) {
                    if (timestamp == null) {
                        put.add(familyName, qualifier, bytes);
                    } else {
                        put.add(familyName, qualifier, timestamp, bytes);
                    }
                }
                htable.put(put);
                return null;
            }
        });
    }

    @Override
    public void put(String tableName, final Put put) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                htable.put(put);
                return null;
            }
        });
    }

    @Override
    public void put(String tableName, final List<Put> puts) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                htable.put(puts);
                return null;
            }
        });
    }

    @Override
    public void delete(String tableName, final Delete delete) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                htable.delete(delete);
                return null;
            }
        });
    }

    @Override
    public void delete(String tableName, final List<Delete> deletes) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                htable.delete(deletes);
                return null;
            }
        });
    }

    @Override
    public <T> List<T> find(String tableName, final List<Scan> scanList, final ResultsExtractor<T> action) {
        return execute(tableName, new TableCallback<List<T>>() {
            @Override
            public List<T> doInTable(HTableInterface htable) throws Throwable {
                List<T> result = new ArrayList<>(scanList.size());
                for (Scan scan : scanList) {
                    final ResultScanner scanner = htable.getScanner(scan);
                    try {
                        T t = action.extractData(scanner);
                        result.add(t);
                    } finally {
                        scanner.close();
                    }
                }
                return result;
            }
        });
    }

    @Override
    public <T> List<List<T>> find(String tableName, List<Scan> scanList, RowMapper<T> action) {
        return find(tableName, scanList, new RowMapperResultsExtractor<>(action));
    }

    @Override
    public <T> List<T> findParallel(final String tableName, final List<Scan> scans, final ResultsExtractor<T> action) {
        if (!this.enableParallelScan || scans.size() == 1) {
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
                        public T doInTable(HTableInterface table) throws Throwable {
                            final ResultScanner scanner = table.getScanner(scan);
                            try {
                                return action.extractData(scanner);
                            } finally {
                                scanner.close();
                            }
                        }
                    });
                }
            });
        }
        List<List<Callable<T>>> callablePartitions = Lists.partition(callables, this.maxThreadsPerParallelScan);
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

    @Override
    public <T> List<List<T>> findParallel(String tableName, final List<Scan> scans, final RowMapper<T> action) {
        return findParallel(tableName, scans, new RowMapperResultsExtractor<>(action));
    }

    @Override
    public <T> List<T> find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final RowMapper<T> action) {
        final ResultsExtractor<List<T>> resultsExtractor = new RowMapperResultsExtractor<>(action);
        return executeDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor);
    }

    @Override
    public <T> List<T> find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final int limit, final RowMapper<T> action) {
        final ResultsExtractor<List<T>> resultsExtractor = new LimitRowMapperResultsExtractor<>(action, limit);
        return executeDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor);
    }

    @Override
    public <T> List<T> find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action, final LimitEventHandler limitEventHandler) {
        final LimitRowMapperResultsExtractor<T> resultsExtractor = new LimitRowMapperResultsExtractor<>(action, limit, limitEventHandler);
        return executeDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor);
    }

    @Override
    public <T> T find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action) {
        return executeDistributedScan(tableName, scan, rowKeyDistributor, action);
    }

    protected final <T> T executeDistributedScan(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action) {
        return execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(HTableInterface table) throws Throwable {
                StopWatch watch = null;
                if (debugEnabled) {
                    watch = new StopWatch();
                    watch.start();
                }
                final ResultScanner[] splitScanners = splitScan(table, scan, rowKeyDistributor);
                final ResultScanner scanner = new DistributedScanner(rowKeyDistributor, splitScanners);
                if (debugEnabled) {
                    logger.debug("DistributedScanner createTime: {}ms", watch.stop());
                    watch.start();
                }
                try {
                    return action.extractData(scanner);
                } finally {
                    scanner.close();
                    if (debugEnabled) {
                        logger.debug("DistributedScanner scanTime: {}ms", watch.stop());
                    }
                }
            }
        });
    }

    private ResultScanner[] splitScan(HTableInterface htable, Scan originalScan, AbstractRowKeyDistributor rowKeyDistributor) throws IOException {
        Scan[] scans = rowKeyDistributor.getDistributedScans(originalScan);
        final int length = scans.length;
        for(int i = 0; i < length; i++) {
            Scan scan = scans[i];
            // other properties are already set upon construction
            scan.setId(scan.getId() + "-" + i);
        }

        ResultScanner[] scanners = new ResultScanner[length];
        boolean success = false;
        try {
            for (int i = 0; i < length; i++) {
                scanners[i] = htable.getScanner(scans[i]);
            }
            success = true;
        } finally {
            if (!success) {
                closeScanner(scanners);
            }
        }
        return scanners;
    }

    private void closeScanner(ResultScanner[] scannerList ) {
        for (ResultScanner scanner : scannerList) {
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Exception e) {
                    logger.warn("Scanner.close() error Caused:{}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public <T> List<T> findParallel(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, RowMapper<T> action, int numParallelThreads) {
        if (!this.enableParallelScan || numParallelThreads <= 1) {
            // use DistributedScanner if parallel scan is disabled or if called to use a single thread
            return find(tableName, scan, rowKeyDistributor, action);
        } else {
            int numThreadsUsed = numParallelThreads < this.maxThreadsPerParallelScan ? numParallelThreads : this.maxThreadsPerParallelScan;
            final ResultsExtractor<List<T>> resultsExtractor = new RowMapperResultsExtractor<>(action);
            return executeParallelDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor, numThreadsUsed);
        }
    }

    @Override
    public <T> List<T> findParallel(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action, int numParallelThreads) {
        if (!this.enableParallelScan || numParallelThreads <= 1) {
            // use DistributedScanner if parallel scan is disabled or if called to use a single thread
            return find(tableName, scan, rowKeyDistributor, limit, action);
        } else {
            int numThreadsUsed = numParallelThreads < this.maxThreadsPerParallelScan ? numParallelThreads : this.maxThreadsPerParallelScan;
            final ResultsExtractor<List<T>> resultsExtractor = new LimitRowMapperResultsExtractor<>(action, limit);
            return executeParallelDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor, numThreadsUsed);
        }
    }

    @Override
    public <T> List<T> findParallel(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action, LimitEventHandler limitEventHandler, int numParallelThreads) {
        if (!this.enableParallelScan || numParallelThreads <= 1) {
            // use DistributedScanner if parallel scan is disabled or if called to use a single thread
            return find(tableName, scan, rowKeyDistributor, limit, action, limitEventHandler);
        } else {
            int numThreadsUsed = numParallelThreads < this.maxThreadsPerParallelScan ? numParallelThreads : this.maxThreadsPerParallelScan;
            final LimitRowMapperResultsExtractor<T> resultsExtractor = new LimitRowMapperResultsExtractor<>(action, limit, limitEventHandler);
            return executeParallelDistributedScan(tableName, scan, rowKeyDistributor, resultsExtractor, numThreadsUsed);
        }
    }

    @Override
    public <T> T findParallel(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> action, int numParallelThreads) {
        if (!this.enableParallelScan || numParallelThreads <= 1) {
            // use DistributedScanner if parallel scan is disabled or if called to use a single thread
            return find(tableName, scan, rowKeyDistributor, action);
        } else {
            int numThreadsUsed = numParallelThreads < this.maxThreadsPerParallelScan ? numParallelThreads : this.maxThreadsPerParallelScan;
            return executeParallelDistributedScan(tableName, scan, rowKeyDistributor, action, numThreadsUsed);
        }
    }

    protected final <T> T executeParallelDistributedScan(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> action, int numParallelThreads) {
        try {
            StopWatch watch = null;
            if (debugEnabled) {
                watch = new StopWatch();
                watch.start();
            }
            ParallelResultScanner scanner = new ParallelResultScanner(tableName, this, this.executor, scan, rowKeyDistributor, numParallelThreads);
            if (debugEnabled) {
                logger.debug("ParallelDistributedScanner createTime: {}ms", watch.stop());
                watch.start();
            }
            try {
                return action.extractData(scanner);
            } finally {
                scanner.close();
                if (debugEnabled) {
                    logger.debug("ParallelDistributedScanner scanTime: {}ms", watch.stop());
                }
            }
        } catch (Throwable th) {
            Throwable throwable = th;
            if (th instanceof ScanTaskException) {
                throwable = th.getCause();
            }
            if (throwable instanceof Error) {
                throw ((Error) th);
            }
            if (throwable instanceof RuntimeException) {
                throw ((RuntimeException) th);
            }
            throw convertHbaseAccessException((Exception) throwable);
        }
    }

    @Override
    public Result increment(String tableName, final Increment increment) {
        return execute(tableName, new TableCallback<Result>() {
            @Override
            public Result doInTable(HTableInterface htable) throws Throwable {
                return htable.increment(increment);
            }
        });
    }

    @Override
    public List<Result> increment(final String tableName, final List<Increment> incrementList) {
        return execute(tableName, new TableCallback<List<Result>>() {
            @Override
            public List<Result> doInTable(HTableInterface htable) throws Throwable {
                final List<Result> resultList = new ArrayList<>(incrementList.size());

                Exception lastException = null;
                for (Increment increment : incrementList) {
                    try {
                        Result result = htable.increment(increment);
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
    public long incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount) {
        return execute(tableName, new TableCallback<Long>() {
            @Override
            public Long doInTable(HTableInterface htable) throws Throwable {
                return htable.incrementColumnValue(rowName, familyName, qualifier, amount);
            }
        });
    }

    @Override
    public long incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount, final boolean writeToWAL) {
        return execute(tableName, new TableCallback<Long>() {
            @Override
            public Long doInTable(HTableInterface htable) throws Throwable {
                return htable.incrementColumnValue(rowName, familyName, qualifier, amount, writeToWAL);
            }
        });
    }


}
