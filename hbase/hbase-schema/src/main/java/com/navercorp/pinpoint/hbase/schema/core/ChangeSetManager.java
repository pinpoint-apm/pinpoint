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

package com.navercorp.pinpoint.hbase.schema.core;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ChangeSetManager {

    private final List<ChangeSet> changeSets;

    public ChangeSetManager(List<ChangeSet> changeSets) {
        if (CollectionUtils.isEmpty(changeSets)) {
            throw new IllegalArgumentException("changeSets must not be empty");
        }
        this.changeSets = changeSets;
    }

    /**
     * Returns a new list of {@link ChangeSet} that has already been executed as specified by {@code schemaChangeLogs}.
     *
     * @param schemaChangeLogs logs of change sets already executed
     * @return a list of change sets already executed
     * @throws IllegalArgumentException if the current change sets are not valid for the specified{@code schemaChangeLogs}
     */
    public List<ChangeSet> getExecutedChangeSets(List<SchemaChangeLog> schemaChangeLogs) {
        if (CollectionUtils.isEmpty(schemaChangeLogs)) {
            return Collections.emptyList();
        }
        List<SchemaChangeLog> sortedSchemaChangeLogs = sortSchemaChangeLogs(schemaChangeLogs);
        List<ChangeSet> executedChangeSets = new ArrayList<>();
        for (int i = 0; i < changeSets.size() && i < sortedSchemaChangeLogs.size(); i++) {
            ChangeSet changeSet = changeSets.get(i);
            SchemaChangeLog executedSchemaChangeLog = sortedSchemaChangeLogs.get(i);
            int expectedOrder = i + 1;
            verifyChangeLog(executedSchemaChangeLog, changeSet, expectedOrder);
            executedChangeSets.add(changeSet);
        }
        return executedChangeSets;
    }

    /**
     * Returns a new list of {@link ChangeSet} that has not been executed as specified by {@code schemaChangeLogs}.
     *
     * @param schemaChangeLogs logs of change sets already executed
     * @return a list of change sets not yet executed
     * @throws IllegalArgumentException if the current change sets are not valid for the specified{@code schemaChangeLogs}
     */
    public List<ChangeSet> filterExecutedChangeSets(List<SchemaChangeLog> schemaChangeLogs) {
        if (CollectionUtils.isEmpty(schemaChangeLogs)) {
            return new ArrayList<>(changeSets);
        }
        List<SchemaChangeLog> sortedSchemaChangeLogs = sortSchemaChangeLogs(schemaChangeLogs);
        int i = 0;
        while (i < changeSets.size() && i < sortedSchemaChangeLogs.size()) {
            ChangeSet changeSet = changeSets.get(i);
            SchemaChangeLog executedSchemaChangeLog = sortedSchemaChangeLogs.get(i);
            int expectedOrder = i + 1;
            verifyChangeLog(executedSchemaChangeLog, changeSet, expectedOrder);
            i++;
        }
        if (i >= changeSets.size()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(changeSets.subList(i, changeSets.size()));
    }

    private List<SchemaChangeLog> sortSchemaChangeLogs(List<SchemaChangeLog> schemaChangeLogs) {
        List<SchemaChangeLog> sortedSchemaChangeLogs = new ArrayList<>(schemaChangeLogs);
        sortedSchemaChangeLogs.sort(Comparator.comparingInt(SchemaChangeLog::getExecOrder));
        return sortedSchemaChangeLogs;
    }

    private void verifyChangeLog(SchemaChangeLog schemaChangeLog, ChangeSet changeSet, int expectedOrder) {
        String changeSetId = changeSet.getId();
        String schemaChangeLogId = schemaChangeLog.getId();
        if (!changeSetId.equals(schemaChangeLogId)) {
            throw new IllegalArgumentException("Unexpected schema change log id, expected : " + changeSetId + " , was : " + schemaChangeLogId);
        }

        CheckSum actualCheckSum = schemaChangeLog.getCheckSum();
        CheckSum expectedCheckSum = CheckSum.compute(actualCheckSum.getVersion(), changeSet.getValue());
        if (!expectedCheckSum.equals(actualCheckSum)) {
            throw new IllegalArgumentException("Unexpected schema change log check sum for : " + schemaChangeLogId);
        }
        int actualOrder = schemaChangeLog.getExecOrder();
        if (expectedOrder != actualOrder) {
            throw new IllegalArgumentException("Unexpected schema change log execution order for " + schemaChangeLogId + ", expected : " + expectedOrder + ", was : " + actualOrder);
        }
    }
}
