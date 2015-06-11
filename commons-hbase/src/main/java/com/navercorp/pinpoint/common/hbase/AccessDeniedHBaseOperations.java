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

import java.util.List;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.data.hadoop.hbase.TableCallback;

import com.navercorp.pinpoint.common.hbase.exception.HBaseAccessDeniedException;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

/**
 * @author Taejin Koo
 */
public class AccessDeniedHBaseOperations implements HbaseOperations2 {

    @Override
    public <T> T execute(String tableName, TableCallback<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> T find(String tableName, String family, ResultsExtractor<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> T find(String tableName, String family, String qualifier, ResultsExtractor<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> T find(String tableName, Scan scan, ResultsExtractor<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> List<T> find(String tableName, String family, RowMapper<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> List<T> find(String tableName, String family, String qualifier, RowMapper<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> List<T> find(String tableName, Scan scan, RowMapper<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> T get(String tableName, String rowName, RowMapper<T> mapper) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> T get(String tableName, String rowName, String familyName, RowMapper<T> mapper) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> T get(String tableName, String rowName, String familyName, String qualifier, RowMapper<T> mapper) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> T get(String tableName, byte[] rowName, RowMapper<T> mapper) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> T get(String tableName, byte[] rowName, byte[] familyName, RowMapper<T> mapper) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> T get(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, RowMapper<T> mapper) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> T get(String tableName, Get get, RowMapper<T> mapper) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> List<T> get(String tableName, List<Get> get, RowMapper<T> mapper) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public void put(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, byte[] value) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public void put(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, Long timestamp, byte[] value) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> void put(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, T value, ValueMapper<T> mapper) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> void put(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, Long timestamp, T value, ValueMapper<T> mapper) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public void put(String tableName, Put put) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public void put(String tableName, List<Put> puts) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public void delete(String tableName, Delete delete) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public void delete(String tableName, List<Delete> deletes) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> List<T> find(String tableName, List<Scan> scans, ResultsExtractor<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> List<List<T>> find(String tableName, List<Scan> scans, RowMapper<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> List<T> find(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, RowMapper<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> List<T> find(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> List<T> find(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action,
            LimitEventHandler limitEventHandler) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public <T> T find(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> action) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public Result increment(String tableName, Increment increment) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public List<Result> increment(String tableName, List<Increment> incrementList) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public long incrementColumnValue(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, long amount) {
        throw new HBaseAccessDeniedException();
    }

    @Override
    public long incrementColumnValue(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, long amount, boolean writeToWAL) {
        throw new HBaseAccessDeniedException();
    }

}
