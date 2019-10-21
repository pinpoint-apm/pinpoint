/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.schema.service;

import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import com.navercorp.pinpoint.hbase.schema.dao.SchemaChangeLogDao;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import com.navercorp.pinpoint.hbase.schema.core.CheckSum;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author HyunGil Jeong
 */
public class SchemaChangeLogServiceImpl implements SchemaChangeLogService {

    private final SchemaChangeLogDao schemaChangeLogDao;

    public SchemaChangeLogServiceImpl(SchemaChangeLogDao schemaChangeLogDao) {
        this.schemaChangeLogDao = Objects.requireNonNull(schemaChangeLogDao, "schemaChangeLogDao");
    }

    @Override
    public String getTableName() {
        return schemaChangeLogDao.getSchemaChangeLogTableName();
    }

    @Override
    public boolean isAvailable(String namespace) {
        return schemaChangeLogDao.tableExists(namespace);
    }

    @Override
    public boolean init(String namespace) {
        return schemaChangeLogDao.createTable(namespace);
    }

    @Override
    public boolean reset(String namespace) {
        return schemaChangeLogDao.resetTable(namespace);
    }

    @Override
    public List<SchemaChangeLog> recordChangeSets(String namespace, List<ChangeSet> changeSets) {
        return recordChangeSets(namespace, 1, changeSets);
    }

    @Override
    public List<SchemaChangeLog> recordChangeSets(String namespace, int executionOrder, List<ChangeSet> changeSets) {
        if (CollectionUtils.isEmpty(changeSets)) {
            return Collections.emptyList();
        }
        int startingOrder = executionOrder;
        long executionTimestamp = System.currentTimeMillis();
        List<SchemaChangeLog> schemaChangeLogs = new ArrayList<>();
        for (ChangeSet changeSet : changeSets) {
            SchemaChangeLog schemaChangeLog = recordChangeSet(namespace, executionTimestamp, startingOrder, changeSet);
            schemaChangeLogs.add(schemaChangeLog);
            startingOrder++;
        }
        return schemaChangeLogs;
    }

    @Override
    public SchemaChangeLog recordChangeSet(String namespace, ChangeSet changeSet) {
        return recordChangeSet(namespace, 1, changeSet);
    }

    @Override
    public SchemaChangeLog recordChangeSet(String namespace, int executionOrder, ChangeSet changeSet) {
        long executionTimestamp = System.currentTimeMillis();
        return recordChangeSet(namespace, executionTimestamp, executionOrder, changeSet);
    }

    private SchemaChangeLog recordChangeSet(String namespace, long executionTimestamp, int executionOrder, ChangeSet changeSet) {
        CheckSum checkSum = CheckSum.compute(CheckSum.getCurrentVersion(), changeSet.getValue());
        SchemaChangeLog schemaChangeLog = new SchemaChangeLog.Builder()
                .id(changeSet.getId())
                .execTimestamp(executionTimestamp)
                .execOrder(executionOrder)
                .checkSum(checkSum)
                .value(changeSet.getValue())
                .build();
        schemaChangeLogDao.insertChangeLog(namespace, schemaChangeLog);
        return schemaChangeLog;
    }

    @Override
    public List<SchemaChangeLog> getSchemaChangeLogs(String namespace) {
        List<SchemaChangeLog> schemaChangeLogs = schemaChangeLogDao.getChangeLogs(namespace);
        Map<Integer, SchemaChangeLog> orderedSchemaChangeLogs = new TreeMap<>();
        Set<String> schemaChangeLogIds = new HashSet<>();
        for (SchemaChangeLog schemaChangeLog : schemaChangeLogs) {
            Integer execOrder = schemaChangeLog.getExecOrder();
            String id = schemaChangeLog.getId();
            SchemaChangeLog previousLog = orderedSchemaChangeLogs.put(execOrder, schemaChangeLog);
            if (previousLog != null) {
                throw new IllegalStateException("Corrupted schema change logs. Duplicate order for change set : " + id);
            }
            if (!schemaChangeLogIds.add(schemaChangeLog.getId())) {
                throw new IllegalStateException("Corrupted schema change logs. Duplicate change set : " + id);
            }
        }
        return new ArrayList<>(orderedSchemaChangeLogs.values());
    }

    @Override
    public SchemaChangeLog getSchemaChangeLog(String namespace, String id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("Change set id must not be empty");
        }
        return schemaChangeLogDao.getChangeLog(namespace, id);
    }

}
