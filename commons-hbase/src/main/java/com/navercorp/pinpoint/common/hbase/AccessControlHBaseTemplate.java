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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.data.hadoop.hbase.TableCallback;

import com.sematext.hbase.wd.AbstractRowKeyDistributor;

/**
 * @author Taejin Koo
 */
public class AccessControlHBaseTemplate implements AccessControlOperations, HbaseOperations2 {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private volatile HbaseOperations2 hbaseOperations;
    
    private final HbaseOperations2 defaultHBaseOperation;
    private final HbaseOperations2 acessDeniedHbaseOperation;
    
    public AccessControlHBaseTemplate(HbaseOperations2 hbaseOperations) {
        this.defaultHBaseOperation = hbaseOperations;
        this.acessDeniedHbaseOperation = new AccessDeniedHBaseOperations(); 
    }
    
    @Override
    public void enableAccess() {
        logger.warn("Enable HBase Access.");
        this.hbaseOperations = defaultHBaseOperation;
    }

    @Override
    public void disableAccess() {
        logger.warn("Disable HBase Access.");
        this.hbaseOperations = acessDeniedHbaseOperation;
    }

    @Override
    public boolean isEnableAccess() {
        return hbaseOperations == defaultHBaseOperation;
    }

    @Override
    public <T> T execute(String tableName, TableCallback<T> action) {
        return hbaseOperations.execute(tableName, action);
    }

    @Override
    public <T> T find(String tableName, String family, ResultsExtractor<T> action) {
        return hbaseOperations.find(tableName, family, action);
    }

    @Override
    public <T> T find(String tableName, String family, String qualifier, ResultsExtractor<T> action) {
        return hbaseOperations.find(tableName, family, qualifier, action);
    }

    @Override
    public <T> T find(String tableName, Scan scan, ResultsExtractor<T> action) {
        return hbaseOperations.find(tableName, scan, action);
    }

    @Override
    public <T> List<T> find(String tableName, String family, RowMapper<T> action) {
        return hbaseOperations.find(tableName, family, action);
    }

    @Override
    public <T> List<T> find(String tableName, String family, String qualifier, RowMapper<T> action) {
        return hbaseOperations.find(tableName, family, qualifier, action);
    }

    @Override
    public <T> List<T> find(String tableName, Scan scan, RowMapper<T> action) {
        return hbaseOperations.find(tableName, scan, action);
    }

    @Override
    public <T> T get(String tableName, String rowName, RowMapper<T> mapper) {
        return hbaseOperations.get(tableName, rowName, mapper);
    }

    @Override
    public <T> T get(String tableName, String rowName, String familyName, RowMapper<T> mapper) {
        return hbaseOperations.get(tableName, rowName, familyName, mapper);
    }

    @Override
    public <T> T get(String tableName, String rowName, String familyName, String qualifier, RowMapper<T> mapper) {
        return hbaseOperations.get(tableName, rowName, familyName, qualifier, mapper);
    }

    @Override
    public <T> T get(String tableName, byte[] rowName, RowMapper<T> mapper) {
        return hbaseOperations.get(tableName, rowName, mapper);
    }

    @Override
    public <T> T get(String tableName, byte[] rowName, byte[] familyName, RowMapper<T> mapper) {
        return hbaseOperations.get(tableName, rowName, familyName, mapper);
    }

    @Override
    public <T> T get(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, RowMapper<T> mapper) {
        return hbaseOperations.get(tableName, rowName, familyName, qualifier, mapper);
    }

    @Override
    public <T> T get(String tableName, Get get, RowMapper<T> mapper) {
        return hbaseOperations.get(tableName, get, mapper);
    }

    @Override
    public <T> List<T> get(String tableName, List<Get> get, RowMapper<T> mapper) {
        return hbaseOperations.get(tableName, get, mapper);
    }

    @Override
    public void put(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, byte[] value) {
        hbaseOperations.put(tableName, rowName, familyName, qualifier, value);
        
    }

    @Override
    public void put(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, Long timestamp, byte[] value) {
        hbaseOperations.put(tableName, rowName, familyName, qualifier, timestamp, value);
    }

    @Override
    public <T> void put(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, T value, ValueMapper<T> mapper) {
        hbaseOperations.put(tableName, rowName, familyName, qualifier, value, mapper);
    }

    @Override
    public <T> void put(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, Long timestamp, T value, ValueMapper<T> mapper) {
        hbaseOperations.put(tableName, rowName, familyName, qualifier, timestamp, value, mapper);
    }

    @Override
    public void put(String tableName, Put put) {
        hbaseOperations.put(tableName, put);
    }

    @Override
    public void put(String tableName, List<Put> puts) {
        hbaseOperations.put(tableName, puts);
    }

    @Override
    public void delete(String tableName, Delete delete) {
        hbaseOperations.delete(tableName, delete);
    }

    @Override
    public void delete(String tableName, List<Delete> deletes) {
        hbaseOperations.delete(tableName, deletes);
    }

    @Override
    public <T> List<T> find(String tableName, List<Scan> scans, ResultsExtractor<T> action) {
        return hbaseOperations.find(tableName, scans, action);
    }

    @Override
    public <T> List<List<T>> find(String tableName, List<Scan> scans, RowMapper<T> action) {
        return hbaseOperations.find(tableName, scans, action);
    }

    @Override
    public <T> List<T> find(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, RowMapper<T> action) {
        return hbaseOperations.find(tableName, scan, rowKeyDistributor, action);
    }

    @Override
    public <T> List<T> find(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action) {
        return hbaseOperations.find(tableName, scan, rowKeyDistributor, limit, action);
    }

    @Override
    public <T> List<T> find(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, int limit, RowMapper<T> action,
            LimitEventHandler limitEventHandler) {
        return hbaseOperations.find(tableName, scan, rowKeyDistributor, limit, action, limitEventHandler);
    }

    @Override
    public <T> T find(String tableName, Scan scan, AbstractRowKeyDistributor rowKeyDistributor, ResultsExtractor<T> action) {
        return hbaseOperations.find(tableName, scan, rowKeyDistributor, action);
    }

    @Override
    public Result increment(String tableName, Increment increment) {
        return hbaseOperations.increment(tableName, increment);
    }

    @Override
    public List<Result> increment(String tableName, List<Increment> incrementList) {
        return hbaseOperations.increment(tableName, incrementList);
    }

    @Override
    public long incrementColumnValue(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, long amount) {
        return hbaseOperations.incrementColumnValue(tableName, rowName, familyName, qualifier, amount);
    }

    @Override
    public long incrementColumnValue(String tableName, byte[] rowName, byte[] familyName, byte[] qualifier, long amount, boolean writeToWAL) {
        return hbaseOperations.incrementColumnValue(tableName, rowName, familyName, qualifier, amount, writeToWAL);
    }
    
}
