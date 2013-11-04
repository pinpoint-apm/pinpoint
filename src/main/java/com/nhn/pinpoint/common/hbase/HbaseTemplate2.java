package com.nhn.pinpoint.common.hbase;

import com.nhn.pinpoint.common.util.StopWatch;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import com.sematext.hbase.wd.DistributedScanner;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.hadoop.hbase.*;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author emeroad
 */
public class HbaseTemplate2 extends HbaseTemplate implements HbaseOperations2, InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private PooledHTableFactory pooledHTableFactory;
    private int poolSize = PooledHTableFactory.DEFAULT_POOL_SIZE;

    private ExecutorService executor = newCachedThreadPool();

    public HbaseTemplate2() {
    }

    public ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, 128,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

//    public Executor getExecutor() {
//        return executor;
//    }

//    public void setExecutor(Executor executor) {
//        this.executor = executor;
//    }

    public HbaseTemplate2(Configuration configuration) {
        Assert.notNull(configuration);
    }

    public HbaseTemplate2(Configuration configuration, int poolSize) {
        Assert.notNull(configuration);
        this.poolSize = poolSize;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int hTablePoolSize) {
        this.poolSize = hTablePoolSize;
    }

    @Override
    public void afterPropertiesSet() {
        Configuration configuration = getConfiguration();
        Assert.notNull(configuration, "configuration is required");
        this.pooledHTableFactory = new PooledHTableFactory(configuration, poolSize);
        this.setTableFactory(pooledHTableFactory);
    }

    @Override
    public void destroy() throws Exception {
        if (pooledHTableFactory != null) {
            this.pooledHTableFactory.destroy();
        }

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
        return find(tableName, scan, new RowMapperResultsExtractor<T>(action));
    }

//    public class ParallelScan<T> {
//        private String tableName;
//        private Scan scan;
//        private RowMapper<T> mapper;
//
//        public String getTableName() {
//            return tableName;
//        }
//
//        public void setTableName(String tableName) {
//            this.tableName = tableName;
//        }
//
//        public Scan getScan() {
//            return scan;
//        }
//
//        public void setScan(Scan scan) {
//            this.scan = scan;
//        }
//
//        public RowMapper<T> getMapper() {
//            return mapper;
//        }
//
//        public void setMapper(RowMapper<T> action) {
//            this.mapper = action;
//        }
//    }
//
//    /**
//     * sanner를 병렬로 돌리기 위한 api
//     * scanner 구현 자체가 얼마나 병렬인지 애매해서 무조껀 만들기도 그러니 일단 주석처리.
//     * @return
//     */
//    public <T> List<Future<List<T>>> findParallel(final ParallelScan<T> parallelScans) {
//        Callable<List<T>> tCallable = new Callable<List<T>>() {
//            @Override
//            public List<T> call() throws Exception {
//                return find(parallelScans.getTableName(), parallelScans.getScan(), parallelScans.getMapper());
//            }
//        };
//        ArrayList<Callable<List<T>>> callables = new ArrayList<Callable<List<T>>>();
//        callables.add(tCallable);
//
//        List<Future<List<T>>> futures = null;
//        try {
//            futures = this.executor.invokeAll(callables);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//        return futures;
//    }

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
                List<T> list = new ArrayList<T>(result.length);
                for (int i = 0; i < result.length; i++) {
                    T t = mapper.mapRow(result[i], i);
                    list.add(t);
                }
                return list;
            }
        });
    }


    public void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final byte[] value) {
        put(tableName, rowName, familyName, qualifier, null, value);
    }

    public void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final Long timestamp, final byte[] value) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                Put put = new Put(rowName);
                if (familyName != null) {
                    if (timestamp == null) {
                        put.add(familyName, qualifier, value);
                    } else {
                        put.add(familyName, qualifier, timestamp, value);
                    }
                }
                htable.put(put);
                return null;
            }
        });
    }

    public <T> void put(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final T value, final ValueMapper<T> mapper) {
        put(tableName, rowName, familyName, qualifier, null, value, mapper);
    }

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

    public void put(String tableName, final Put put) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                htable.put(put);
                return null;
            }
        });
    }

    public void put(String tableName, final List<Put> puts) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                htable.put(puts);
                return null;
            }
        });
    }

    public void delete(String tableName, final Delete delete) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                htable.delete(delete);
                return null;
            }
        });
    }

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
                List<T> result = new ArrayList<T>(scanList.size());
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
        return find(tableName, scanList, new RowMapperResultsExtractor<T>(action));
    }

    public <T> List<T> find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final RowMapper<T> action) {
        final ResultsExtractor<List<T>> resultsExtractor = new RowMapperResultsExtractor<T>(action);
        return execute(tableName, new TableCallback<List<T>>() {
            @Override
            public List<T> doInTable(HTableInterface htable) throws Throwable {
                final ResultScanner scanner = createDistributeScanner(htable, scan, rowKeyDistributor);
                try {
                    return resultsExtractor.extractData(scanner);
                } finally {
                    scanner.close();
                }
            }
        });
    }

    public <T> List<T> find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action) {
        final ResultsExtractor<List<T>> resultsExtractor = new LimitRowMapperResultsExtractor<T>(action, limit);
        return execute(tableName, new TableCallback<List<T>>() {
            @Override
            public List<T> doInTable(HTableInterface htable) throws Throwable {
                final ResultScanner scanner = createDistributeScanner(htable, scan, rowKeyDistributor);
                try {
                    return resultsExtractor.extractData(scanner);
                } finally {
                    scanner.close();
                }
            }
        });
    }
    
	public <T> List<T> find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, int limit, final RowMapper<T> action, final LimitEventHandler limitEventHandler) {
		final LimitRowMapperResultsExtractor<T> resultsExtractor = new LimitRowMapperResultsExtractor<T>(action, limit, limitEventHandler);
		return execute(tableName, new TableCallback<List<T>>() {
			@Override
			public List<T> doInTable(HTableInterface htable) throws Throwable {
				final ResultScanner scanner = createDistributeScanner(htable, scan, rowKeyDistributor);
				try {
					return resultsExtractor.extractData(scanner);
				} finally {
					scanner.close();
				}
			}
		});
	}


    @Override
    public <T> T find(String tableName, final Scan scan, final AbstractRowKeyDistributor rowKeyDistributor, final ResultsExtractor<T> action) {

        return execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(HTableInterface htable) throws Throwable {
                final boolean debugEnabled = logger.isDebugEnabled();
                StopWatch watch = null;
                if (debugEnabled) {
                    watch = new StopWatch();
                    watch.start();
                }
                final ResultScanner scanner = createDistributeScanner(htable, scan, rowKeyDistributor);
                if (debugEnabled) {
                    logger.debug("DistributeScanner createTime:{}", watch.stop());
                }
                if (debugEnabled) {
                    watch.start();
                }
                try {
                    return action.extractData(scanner);
                } finally {
                    scanner.close();
                    if (debugEnabled) {
                        logger.debug("DistributeScanner scanTime:{}", watch.stop());
                    }
                }
            }
        });
    }

    public ResultScanner createDistributeScanner(HTableInterface htable, Scan originalScan, AbstractRowKeyDistributor rowKeyDistributor) throws IOException {

        Scan[] scans = rowKeyDistributor.getDistributedScans(originalScan);
        for(int i = 0; i < scans.length; i++) {
            Scan scan = scans[i];
            scan.setId(originalScan.getId() + "-" + i);
            // caching만 넣으면 되나?
            scan.setCaching(originalScan.getCaching());
        }

        ResultScanner[] scanner = new ResultScanner[scans.length];
        boolean success = false;
        try {
            for (int i = 0; i < scans.length; i++) {
                scanner[i] = htable.getScanner(scans[i]);
            }
            success = true;
        } finally {
            if (!success) {
                closeScanner(scanner);
            }
        }

        return new DistributedScanner(rowKeyDistributor, scanner);
    }

    private void closeScanner(ResultScanner[] scannerList ) {
        for (ResultScanner scanner : scannerList) {
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public Result increment(String tableName, final Increment increment) {
        return execute(tableName, new TableCallback<Result>() {
            @Override
            public Result doInTable(HTableInterface htable) throws Throwable {
                return htable.increment(increment);
            }
        });
    }

    public List<Result> increment(String tableName, final List<Increment> incrementList) {
        return execute(tableName, new TableCallback<List<Result>>() {
            @Override
            public List<Result> doInTable(HTableInterface htable) throws Throwable {
                final List<Result> resultList = new ArrayList<Result>(incrementList.size());

                Exception lastException = null;
                for (Increment increment : incrementList) {
                    try {
                        Result result = htable.increment(increment);
                        resultList.add(result);
                    } catch (IOException e) {
                        lastException = e;
                    }
                }
                if(lastException != null) {
                    throw lastException;
                }
                return resultList;
            }
        });
    }

    public long incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount) {
        return execute(tableName, new TableCallback<Long>() {
            @Override
            public Long doInTable(HTableInterface htable) throws Throwable {
                return htable.incrementColumnValue(rowName, familyName, qualifier, amount);
            }
        });
    }

    public long incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount, final boolean writeToWAL) {
        return execute(tableName, new TableCallback<Long>() {
            @Override
            public Long doInTable(HTableInterface htable) throws Throwable {
                return htable.incrementColumnValue(rowName, familyName, qualifier, amount, writeToWAL);
            }
        });
    }


}
