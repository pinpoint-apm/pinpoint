/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.ApplicationInfoDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.server.util.HashUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.UuidUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Repository
public class HbaseApplicationInfoDao implements ApplicationInfoDao {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.ApplicationId DESCRIPTOR_FORWARD = HbaseColumnFamily.APPLICATION_ID_FORWARD;
    private static final HbaseColumnFamily.ApplicationId DESCRIPTOR_INVERSE = HbaseColumnFamily.APPLICATION_ID_INVERSE;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;
    private final RowMapper<UUID> forwardRowMapper;
    private final RowMapper<String> inverseRowMapper;

    public HbaseApplicationInfoDao(
            HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            @Qualifier("applicationIdForwardMapper") RowMapper<UUID> forwardRowMapper,
            @Qualifier("applicationIdInverseMapper") RowMapper<String> inverseRowMapper) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.forwardRowMapper = Objects.requireNonNull(forwardRowMapper, "forwardRowMapper");
        this.inverseRowMapper = Objects.requireNonNull(inverseRowMapper, "inverseRowMapper");
    }

    @Override
    public ApplicationId getApplicationId(String applicationName) {
        Objects.requireNonNull(applicationName, "applicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("getApplicationId() applicationName:{}", applicationName);
        }

        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_FORWARD.getTable());
        byte[] rowKey = encodeStringAsRowKey(applicationName);
        byte[] family = DESCRIPTOR_FORWARD.getName();
        byte[] qualifier = DESCRIPTOR_FORWARD.getName();

        Get get = new Get(rowKey);
        get.addColumn(family, qualifier);

        return ApplicationId.of(hbaseTemplate.get(tableName, get, this.forwardRowMapper));
    }

    @Override
    public String getApplicationName(ApplicationId applicationId) {
        Objects.requireNonNull(applicationId, "applicationId");

        if (logger.isDebugEnabled()) {
            logger.debug("getApplicationName() applicationId:{}", applicationId);
        }

        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_INVERSE.getTable());
        byte[] rowKey = encodeUuidAsRowKey(applicationId.value());
        byte[] family = DESCRIPTOR_INVERSE.getName();
        byte[] qualifier = DESCRIPTOR_INVERSE.getName();

        Get get = new Get(rowKey);
        get.addColumn(family, qualifier);

        return hbaseTemplate.get(tableName, get, this.inverseRowMapper);
    }

    @Override
    public ApplicationId putApplicationIdIfAbsent(String applicationName, ApplicationId applicationId) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(applicationId, "applicationId");

        if (logger.isDebugEnabled()) {
            logger.debug("putApplicationIdIfAbsent() applicationName:{}, applicationId:{}", applicationName, applicationId);
        }

        byte[] family = DESCRIPTOR_FORWARD.getName();
        byte[] qualifier = DESCRIPTOR_FORWARD.getName();

        CheckAndMutateResult camResult = putForward(applicationName, applicationId);

        if (camResult.isSuccess()) {
            putInverse(applicationId, applicationName);
        }

        Cell cell = camResult.getResult().getColumnLatestCell(family, qualifier);
        return ApplicationId.of(UuidUtils.fromBytes(cell.getValueArray()));
    }

    @Override
    public void ensureInverse(String applicationName, ApplicationId applicationId) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(applicationId, "applicationId");

        if (logger.isDebugEnabled()) {
            logger.debug("ensureInverse() applicationName:{}, applicationId:{}", applicationName, applicationId);
        }

        putInverse(applicationId, applicationName);
    }

    private CheckAndMutateResult putForward(String applicationName, ApplicationId applicationId) {
        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_FORWARD.getTable());
        byte[] rowKey = encodeStringAsRowKey(applicationName);
        byte[] family = DESCRIPTOR_FORWARD.getName();
        byte[] qualifier = DESCRIPTOR_FORWARD.getName();
        byte[] value = UuidUtils.toBytes(applicationId.value());

        Put put = new Put(rowKey);
        put.addColumn(family, qualifier, value);

        CheckAndMutate checkAndMutate = CheckAndMutate.newBuilder(rowKey)
                .ifNotExists(family, qualifier)
                .build(put);

        return hbaseTemplate.checkAndMutate(tableName, checkAndMutate);
    }

    private void putInverse(ApplicationId applicationId, String applicationName) {
        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_INVERSE.getTable());
        byte[] rowKey = encodeUuidAsRowKey(applicationId.value());
        byte[] family = DESCRIPTOR_INVERSE.getName();
        byte[] qualifier = DESCRIPTOR_INVERSE.getName();
        byte[] value = BytesUtils.toBytes(applicationName);

        Put put = new Put(rowKey);
        put.addColumn(family, qualifier, value);

        hbaseTemplate.put(tableName, put);
    }

    private static byte[] encodeStringAsRowKey(String str) {
        return HashUtils.hashBytes(BytesUtils.toBytes(str)).asBytes();
    }

    private static byte[] encodeUuidAsRowKey(UUID uuid) {
        return UuidUtils.toBytes(uuid);
    }
}
