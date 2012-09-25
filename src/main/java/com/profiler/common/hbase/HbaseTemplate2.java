package com.profiler.common.hbase;

import org.apache.hadoop.hbase.client.*;
import org.springframework.data.hadoop.hbase.*;

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
                    }
                    else {
                        get.addFamily(familyName);
                    }
                }
                Result result = htable.get(get);
                return mapper.mapRow(result, 0);
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
                    if(timestamp == null) {
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
                    if(timestamp == null) {
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

    public <T> void put(String tableName, final Put put) {
        execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(HTable htable) throws Throwable {
                htable.put(put);
                return null;
            }
        });
    }

    public <T> void put(String tableName, final List<Put> puts) {
        execute(tableName, new TableCallback<T>() {
            @Override
            public T doInTable(HTable htable) throws Throwable {
                htable.put(puts);
                return null;
            }
        });
    }

}
