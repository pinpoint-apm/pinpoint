package com.profiler.common.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.hadoop.hbase.*;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class HbaseTemplate2 implements HbaseOperations2, InitializingBean, DisposableBean {

    private boolean autoFlush = true;
    private Charset charset = Charset.forName("UTF-8");

    private Configuration configuration;
    private HTablePool hTablePool;
    private int hTablePoolSize = 256;


    public HbaseTemplate2() {
    }

    public HbaseTemplate2(Configuration configuration) {
        Assert.notNull(configuration);
        this.configuration = configuration;
    }

    public HbaseTemplate2(Configuration configuration, int hTablePoolSize) {
        Assert.notNull(configuration);
        this.configuration = configuration;
        this.hTablePoolSize = hTablePoolSize;
    }

    public Charset getCharset() {
        return charset;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.hTablePool = new HTablePool(getConfiguration(), this.hTablePoolSize);
    }

    @Override
    public void destroy() throws Exception {
        this.hTablePool.close();
    }

    @Override
    public <T> T execute(String tableName, TableCallback<T> action) {
        Assert.notNull(action, "Callback object must not be null");
        Assert.notNull(tableName, "No table specified");

        HTable table = getTable(tableName);

        try {
            boolean previousFlushSetting = applyFlushSetting(table);
            T result = action.doInTable(table);
            flushIfNecessary(table, previousFlushSetting);
            return result;
        } catch (Throwable th) {
            if (th instanceof Error) {
                throw ((Error) th);
            }
            if (th instanceof RuntimeException) {
                throw ((RuntimeException) th);
            }
            throw convertHbaseAccessException((Exception) th);
        } finally {
            releaseTable(tableName, table);
        }
    }


    private HTable getTable(String tableName) {
        try {
            return (HTable) this.hTablePool.getTable(tableName);
        } catch (Exception e) {
            throw convertHbaseAccessException(e);
        }
    }

    private void releaseTable(String tableName, HTable table) {
        try {
            table.close();
        } catch (IOException e) {
            throw convertHbaseAccessException(e);
        }
    }


    private boolean applyFlushSetting(HTable table) {
        boolean autoFlush = table.isAutoFlush();
        table.setAutoFlush(this.autoFlush);
        return autoFlush;
    }

    private void flushIfNecessary(HTable table, boolean oldFlush) throws IOException {
        // TODO: check whether we can consider or not a table scope
        table.flushCommits();
        if (table.isAutoFlush() != oldFlush) {
            table.setAutoFlush(oldFlush);
        }
    }

    public DataAccessException convertHbaseAccessException(Exception ex) {
        return HbaseUtils.convertHbaseException(ex);
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
            public T doInTable(HTable htable) throws Throwable {
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
            public T doInTable(HTable htable) throws Throwable {
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

    /**
     * Sets the auto flush.
     *
     * @param autoFlush The autoFlush to set.
     */
    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
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
            public T doInTable(HTable htable) throws Throwable {
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
            public T doInTable(HTable htable) throws Throwable {
                Result result = htable.get(get);
                return mapper.mapRow(result, 0);
            }
        });
    }

    @Override
    public <T> List<T> get(String tableName, final List<Get> gets, final RowMapper<T> mapper) {
        return execute(tableName, new TableCallback<List<T>>() {
            @Override
            public List<T> doInTable(HTable htable) throws Throwable {
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
            public Object doInTable(HTable htable) throws Throwable {
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
            public T doInTable(HTable htable) throws Throwable {
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
            public Object doInTable(HTable htable) throws Throwable {
                htable.put(put);
                return null;
            }
        });
    }

    public void put(String tableName, final List<Put> puts) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTable htable) throws Throwable {
                htable.put(puts);
                return null;
            }
        });
    }

    public void delete(String tableName, final Delete delete) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTable htable) throws Throwable {
                htable.delete(delete);
                return null;
            }
        });
    }

    public void delete(String tableName, final List<Delete> deletes) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTable htable) throws Throwable {
                htable.delete(deletes);
                return null;
            }
        });
    }

    @Override
    public <T> List<T> find(String tableName, final List<Scan> scans, final ResultsExtractor<T> action) {
        return execute(tableName, new TableCallback<List<T>>() {
            @Override
            public List<T> doInTable(HTable htable) throws Throwable {
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
            public Object doInTable(HTable htable) throws Throwable {
                htable.increment(increment);
                return null;
            }
        });
    }

    public void incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTable htable) throws Throwable {
                htable.incrementColumnValue(rowName, familyName, qualifier, amount);
                return null;
            }
        });
    }

    public void incrementColumnValue(String tableName, final byte[] rowName, final byte[] familyName, final byte[] qualifier, final long amount, final boolean writeToWAL) {
        execute(tableName, new TableCallback() {
            @Override
            public Object doInTable(HTable htable) throws Throwable {
                htable.incrementColumnValue(rowName, familyName, qualifier, amount, writeToWAL);
                return null;
            }
        });
    }

	public void doUserBatchJob(String tableName, final HBaseBatchJob job) {
		execute(tableName, new TableCallback<Object>() {
			@Override
			public Object doInTable(HTable htable) throws Throwable {
				job.doBatch(htable);
				return null;
			}
		});
	}
}
