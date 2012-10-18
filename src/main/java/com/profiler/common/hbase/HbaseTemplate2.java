package com.profiler.common.hbase;

import org.apache.hadoop.hbase.client.*;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.data.hadoop.hbase.TableCallback;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class HbaseTemplate2 extends HbaseTemplate implements HbaseOperations2 {

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
}
