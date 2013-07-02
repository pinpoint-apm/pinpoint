package com.nhn.pinpoint.common.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.hadoop.hbase.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class HbaseTemplate2 extends HbaseTemplate implements HbaseOperations2, InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private PooledHTableFactory pooledHTableFactory;
    private int poolSize = PooledHTableFactory.DEFAULT_POOL_SIZE;

    public HbaseTemplate2() {
    }

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
        Assert.notNull(configuration, " a valid configuration is required");
        this.pooledHTableFactory = new PooledHTableFactory(configuration, poolSize);
        this.setTableFactory(pooledHTableFactory);
    }

    @Override
    public void destroy() throws Exception {
        if (pooledHTableFactory != null) {
            this.pooledHTableFactory.destroy();
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
                ResultScanner scanner = htable.getScanner(scan);
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
    public <T> List<T> get(String tableName, final List<Get> gets, final RowMapper<T> mapper) {
        return execute(tableName, new TableCallback<List<T>>() {
            @Override
            public List<T> doInTable(HTableInterface htable) throws Throwable {
                Result[] result = htable.get(gets);
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
    public <T> List<T> find(String tableName, final List<Scan> scans, final ResultsExtractor<T> action) {
        return execute(tableName, new TableCallback<List<T>>() {
            @Override
            public List<T> doInTable(HTableInterface htable) throws Throwable {
                List<T> result = new ArrayList<T>(scans.size());
                for (Scan scan : scans) {
                    ResultScanner scanner = htable.getScanner(scan);
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
    public <T> List<List<T>> find(String tableName, List<Scan> scans, RowMapper<T> action) {
        return find(tableName, scans, new RowMapperResultsExtractor<T>(action));
    }

    public void increment(String tableName, final Increment increment) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                htable.increment(increment);
                return null;
            }
        });
    }

    public void incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                htable.incrementColumnValue(rowName, familyName, qualifier, amount);
                return null;
            }
        });
    }

    public void incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount, final boolean writeToWAL) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTableInterface htable) throws Throwable {
                htable.incrementColumnValue(rowName, familyName, qualifier, amount, writeToWAL);
                return null;
            }
        });
    }


}
