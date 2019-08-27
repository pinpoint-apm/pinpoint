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
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public interface SchemaChangeLogService {

    String getTableName();

    boolean isAvailable(String namespace);

    boolean init(String namespace);

    boolean reset(String namespace);

    List<SchemaChangeLog> recordChangeSets(String namespace, List<ChangeSet> changeSets);

    List<SchemaChangeLog> recordChangeSets(String namespace, int executionOrder, List<ChangeSet> changeSets);

    SchemaChangeLog recordChangeSet(String namespace, ChangeSet changeSet);

    SchemaChangeLog recordChangeSet(String namespace, int executionOrder, ChangeSet changeSet);

    List<SchemaChangeLog> getSchemaChangeLogs(String namespace);

    SchemaChangeLog getSchemaChangeLog(String namespace, String id);
}
