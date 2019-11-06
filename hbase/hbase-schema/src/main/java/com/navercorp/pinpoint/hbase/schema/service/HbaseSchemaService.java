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

import com.navercorp.pinpoint.hbase.schema.core.HbaseSchemaStatus;
import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public interface HbaseSchemaService {

    /**
     * Returns {@code true} if schema management is available for the given {@code namespace}.
     *
     * @param namespace hbase namespace for which the schema management availability is to be tested
     * @return {@code true} if the specified namespace has schema management available
     */
    boolean isAvailable(String namespace);

    /**
     * Initializes schema management for the given {@code namespace}.
     * Returns {@code true} if schema management was already available.
     *
     * @param namespace hbase namespace to create and initialize schema management for
     * @return {@code true} if schema management was already available
     */
    boolean init(String namespace);

    /**
     * Returns the current hbase schema status for the given {@code namespace}, validating it against the specified
     * {@code changeSets} if schema management is available.
     *
     * @param namespace hbase namespace to check the schema status
     * @param changeSets change sets to validate the schema
     * @return the current hbase schema status for the specified namespace
     * @throws IllegalArgumentException if the specified {@code changeSets} is empty
     * @see HbaseSchemaStatus
     */
    HbaseSchemaStatus getSchemaStatus(String namespace, List<ChangeSet> changeSets);

    /**
     * Updates the current hbase schema for the given {@code namespace} using the specified {@code changeSets}, and
     * returns {@code true} if any changes have been made to the schema.
     *
     * @param namespace hbase namespace to update the schema
     * @param compression hbase compression algorithm to be used for table creation
     * @param changeSets change sets to be used to update the schema
     * @return {@code true} if changes have been made to the current schema
     * @throws IllegalStateException if the current hbase schema cannot be updated, or if the current schema change logs
     *                               are not valid for the specified change sets.
     *
     */
    boolean update(String namespace, String compression, List<ChangeSet> changeSets);

    /**
     * Deletes all schema change logs for the specified {@code namespace} and resets them into a clean state.
     *
     * @param namespace hbase namespace to reset the schema change logs
     * @return {@code true} if schema change logs are deleted, {@code false} if schema change log table does not exist
     */
    boolean reset(String namespace);

    /**
     * Returns all executed schema change logs for the given {@code namespace}.
     *
     * @param namespace hbase namespace to retrieve schema change logs from
     * @return list of executed schema change logs
     */
    List<SchemaChangeLog> getChangeLogs(String namespace);

    /**
     * Returns the executed schema change log with the specified {@code changeSetId} for the given {@code namespace}.
     *
     * @param namespace hbase namespace to get the schema change log from
     * @param changeSetId change set id of the schema change log to return
     * @return schema change log with the specified {@code changeSetId} for the given {@code namespace}
     */
    SchemaChangeLog getChangeLog(String namespace, String changeSetId);
}
