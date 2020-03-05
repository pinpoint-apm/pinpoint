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

import com.navercorp.pinpoint.common.hbase.HbaseAdminOperation;
import com.navercorp.pinpoint.hbase.schema.core.ChangeSetManager;
import com.navercorp.pinpoint.hbase.schema.core.HbaseSchemaStatus;
import com.navercorp.pinpoint.hbase.schema.core.HbaseSchemaVerifier;
import com.navercorp.pinpoint.hbase.schema.core.command.TableCommand;
import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import com.navercorp.pinpoint.hbase.schema.core.command.HbaseSchemaCommandManager;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HyunGil Jeong
 */
public class HbaseSchemaServiceImpl implements HbaseSchemaService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseAdminOperation hbaseAdminOperation;

    private final SchemaChangeLogService schemaChangeLogService;

    private final HbaseSchemaVerifier<HTableDescriptor> hbaseSchemaVerifier;

    public HbaseSchemaServiceImpl(HbaseAdminOperation hbaseAdminOperation,
                                  SchemaChangeLogService schemaChangeLogService,
                                  HbaseSchemaVerifier<HTableDescriptor> hbaseSchemaVerifier) {
        this.hbaseAdminOperation = Objects.requireNonNull(hbaseAdminOperation, "hbaseAdminOperation");
        this.schemaChangeLogService = Objects.requireNonNull(schemaChangeLogService, "schemaChangeLogService");
        this.hbaseSchemaVerifier = Objects.requireNonNull(hbaseSchemaVerifier, "hbaseSchemaVerifier");
    }

    @Override
    public boolean isAvailable(String namespace) {
        return schemaChangeLogService.isAvailable(namespace);
    }


    protected boolean createNamespaceIfNotExists(String namespace) {
        return hbaseAdminOperation.createNamespaceIfNotExists(namespace);
    }

    /**
     * This implementation also creates a new namespace specified by {@code namespace} if it is not already available.
     */
    @Override
    public boolean init(String namespace) {
        if (createNamespaceIfNotExists(namespace)) {
            logger.info("[{}] Namespace created.", namespace);
        }
        if (schemaChangeLogService.isAvailable(namespace)) {
            return false;
        }
        schemaChangeLogService.init(namespace);
        return true;
    }

    /**
     * This implementation uses the executed schema change logs to compare against the specified {@code changeSets} to
     * check the current schema status, regardless of how the actual schema looks like.
     */
    // TODO May be a good idea to compare the actual tables against each of the change sets in the future.
    @Override
    public HbaseSchemaStatus getSchemaStatus(String namespace, List<ChangeSet> changeSets) {
        if (CollectionUtils.isEmpty(changeSets)) {
            throw new IllegalArgumentException("changeSets must not be empty");
        }
        if (!schemaChangeLogService.isAvailable(namespace)) {
            return HbaseSchemaStatus.NONE;
        }
        List<SchemaChangeLog> schemaChangeLogs = schemaChangeLogService.getSchemaChangeLogs(namespace);
        if (CollectionUtils.isEmpty(schemaChangeLogs)) {
            return HbaseSchemaStatus.NONE;
        }

        List<ChangeSet> unexecutedChangeSets;
        ChangeSetManager changeSetManager = new ChangeSetManager(changeSets);
        try {
            unexecutedChangeSets = changeSetManager.filterExecutedChangeSets(schemaChangeLogs);
        } catch (IllegalArgumentException e) {
            logger.error("[{}] Invalid hbase schema, cause : {}", namespace, e.getMessage());
            return HbaseSchemaStatus.INVALID;
        }
        if (CollectionUtils.isEmpty(unexecutedChangeSets)) {
            return HbaseSchemaStatus.VALID;
        }
        return HbaseSchemaStatus.VALID_OUT_OF_DATE;
    }

    @Override
    public boolean update(String namespace, String compression, List<ChangeSet> changeSets) {
        if (CollectionUtils.isEmpty(changeSets)) {
            return false;
        }

        logger.info("[{}] Hbase schema update started.", namespace);
        assertInitialization(namespace);

        List<HTableDescriptor> currentHtds = getCurrentSchema(namespace);
        List<SchemaChangeLog> schemaChangeLogs = schemaChangeLogService.getSchemaChangeLogs(namespace);

        if (CollectionUtils.isEmpty(currentHtds)) {
            if (CollectionUtils.isEmpty(schemaChangeLogs)) {
                logger.info("[{}] Schema change logs not found.", namespace);
                return init(namespace, compression, changeSets);
            }
            logger.warn("[{}] Schema change logs found, but no tables are present. Attempting recovery by resetting all schema change logs.", namespace);
            reset(namespace);
            return init(namespace, compression, changeSets);
        }

        if (CollectionUtils.isEmpty(schemaChangeLogs)) {
            return initFromExistingTables(namespace, compression, changeSets, currentHtds);
        }
        return updateExistingSchemas(namespace, compression, changeSets, currentHtds, schemaChangeLogs);
    }

    private List<HTableDescriptor> getCurrentSchema(String namespace) {
        List<HTableDescriptor> currentHtds = hbaseAdminOperation.getTableDescriptors(namespace);
        TableName schemaChangeLogTableName = TableName.valueOf(namespace, schemaChangeLogService.getTableName());
        return currentHtds.stream()
                .filter((currentHtd) -> !schemaChangeLogTableName.equals(currentHtd.getTableName()))
                .collect(Collectors.toList());
    }

    private boolean init(String namespace, String compression, List<ChangeSet> changeSets) {
        logger.info("[{}] Initializing hbase schema.", namespace);

        HbaseSchemaCommandManager commandManager = new HbaseSchemaCommandManager(namespace, compression);
        return applyChangeSets(commandManager, changeSets, Collections.emptyList());
    }

    private boolean initFromExistingTables(String namespace, String compression, List<ChangeSet> changeSets, List<HTableDescriptor> currentHtds) {
        logger.info("[{}] Initializing hbase schema from existing tables.", namespace);

        // Replay change sets one by one and compare it against the current hbase schema.
        // If they match, all change sets up to that point are seen as already applied.
        HbaseSchemaCommandManager initCommandManager = new HbaseSchemaCommandManager(namespace, compression);
        List<ChangeSet> appliedChangeSets = new ArrayList<>();
        List<ChangeSet> changeSetsToApply = new ArrayList<>();
        for (ChangeSet changeSet : changeSets) {
            initCommandManager.applyChangeSet(changeSet);
            changeSetsToApply.add(changeSet);
            if (hbaseSchemaVerifier.verifySchemas(initCommandManager.getSchemaSnapshot(), currentHtds)) {
                appliedChangeSets.addAll(changeSetsToApply);
                changeSetsToApply = new ArrayList<>();
            }
        }

        if (appliedChangeSets.isEmpty()) {
            logger.info("[{}] Current table schema does not match any schema from the change sets.", namespace);
        } else {
            List<String> appliedChangeSetIds = appliedChangeSets.stream().map(ChangeSet::getId).collect(Collectors.toList());
            logger.info("[{}] Change sets already applied : {}", namespace, appliedChangeSetIds);
        }

        List<SchemaChangeLog> executedLogs = schemaChangeLogService.recordChangeSets(namespace, appliedChangeSets);
        if (changeSetsToApply.isEmpty()) {
            logger.info("[{}] Hbase schema already at latest version.", namespace);
            return false;
        }

        HbaseSchemaCommandManager updateCommandManager = new HbaseSchemaCommandManager(namespace, compression, currentHtds);
        return applyChangeSets(updateCommandManager, changeSetsToApply, executedLogs);
    }

    private boolean updateExistingSchemas(String namespace, String compression, List<ChangeSet> changeSets, List<HTableDescriptor> currentHtds, List<SchemaChangeLog> executedLogs) {
        logger.info("[{}] Updating hbase schema.", namespace);

        List<String> executedChangeLogIds = executedLogs.stream().map(SchemaChangeLog::getId).collect(Collectors.toList());
        logger.info("[{}] Executed change logs : {}", namespace, executedChangeLogIds);

        ChangeSetManager changeSetManager = new ChangeSetManager(changeSets);

        // Check if the current table schema matches the expected schema specified by the executed schema change logs.
        HbaseSchemaCommandManager initCommandManager = new HbaseSchemaCommandManager(namespace, compression);
        List<ChangeSet> executedChangeSets = changeSetManager.getExecutedChangeSets(executedLogs);
        for (ChangeSet executedChangeSet : executedChangeSets) {
            initCommandManager.applyChangeSet(executedChangeSet);
        }
        if (!hbaseSchemaVerifier.verifySchemas(initCommandManager.getSchemaSnapshot(), currentHtds)) {
            throw new IllegalStateException("Current table schema does not match the schema change log records.");
        }

        List<ChangeSet> changeSetsToApply = changeSetManager.filterExecutedChangeSets(executedLogs);
        if (changeSetsToApply.isEmpty()) {
            logger.info("[{}] Hbase schema already at latest version", namespace);
            return false;
        }
        HbaseSchemaCommandManager updateCommandManager = new HbaseSchemaCommandManager(namespace, compression, currentHtds);
        return applyChangeSets(updateCommandManager, changeSetsToApply, executedLogs);
    }


    private boolean applyChangeSets(HbaseSchemaCommandManager commandManager, List<ChangeSet> changeSets, List<SchemaChangeLog> executedLogs) {
        if (CollectionUtils.isEmpty(changeSets)) {
            return false;
        }

        String namespace = commandManager.getNamespace();
        List<String> changeSetIds = changeSets.stream().map(ChangeSet::getId).collect(Collectors.toList());
        logger.info("[{}] Applying change sets : {}", namespace, changeSetIds);

        for (ChangeSet changeSet : changeSets) {
            commandManager.applyChangeSet(changeSet);
        }
        List<TableCommand> commands = commandManager.getCommands();
        boolean changesMade = commands.stream()
                .map(command -> command.execute(hbaseAdminOperation))
                .reduce(Boolean::logicalOr)
                .orElse(Boolean.FALSE);
        schemaChangeLogService.recordChangeSets(namespace, executedLogs.size() + 1, changeSets);
        return changesMade;
    }

    @Override
    public boolean reset(String namespace) {
        logger.info("[{}] Resetting hbase schema change logs.", namespace);
        if (schemaChangeLogService.reset(namespace)) {
            logger.info("[{}] Deleted all hbase schema change logs.", namespace, schemaChangeLogService.getTableName());
            return true;
        }
        logger.debug("[{}] {} table not found, skipping.", namespace, schemaChangeLogService.getTableName());
        return false;
    }

    @Override
    public List<SchemaChangeLog> getChangeLogs(String namespace) {
        assertInitialization(namespace);
        return schemaChangeLogService.getSchemaChangeLogs(namespace);
    }

    @Override
    public SchemaChangeLog getChangeLog(String namespace, String changeSetId) {
        if (StringUtils.isEmpty(changeSetId)) {
            throw new IllegalArgumentException("Change set id must not be empty");
        }
        assertInitialization(namespace);
        return schemaChangeLogService.getSchemaChangeLog(namespace, changeSetId);
    }

    private void assertInitialization(String namespace) {
        if (!schemaChangeLogService.isAvailable(namespace)) {
            String tableName = schemaChangeLogService.getTableName();
            throw new IllegalStateException(tableName + " table not found. Initialization required.");
        }
    }
}
