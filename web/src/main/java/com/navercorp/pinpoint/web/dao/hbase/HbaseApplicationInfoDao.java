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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.HashUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.UuidUtils;
import com.navercorp.pinpoint.web.dao.ApplicationInfoDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    private final RowMapper<Application> applicationRowMapper;

    public HbaseApplicationInfoDao(
            HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            @Qualifier("applicationIdForwardMapper") RowMapper<UUID> forwardRowMapper,
            @Qualifier("applicationIdInverseMapper") RowMapper<String> inverseRowMapper,
            @Qualifier("applicationForwardMapper") RowMapper<Application> applicationRowMapper
    ) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.forwardRowMapper = Objects.requireNonNull(forwardRowMapper, "forwardRowMapper");
        this.inverseRowMapper = Objects.requireNonNull(inverseRowMapper, "inverseRowMapper");
        this.applicationRowMapper = Objects.requireNonNull(applicationRowMapper, "applicationRowMapper");
    }

    @Override
    public UUID getApplicationId(String applicationName) {
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

        return hbaseTemplate.get(tableName, get, this.forwardRowMapper);
    }

    @Override
    public String getApplicationName(UUID applicationId) {
        Objects.requireNonNull(applicationId, "applicationId");

        if (logger.isDebugEnabled()) {
            logger.debug("getApplicationName() applicationId:{}", applicationId);
        }

        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_INVERSE.getTable());
        byte[] rowKey = UuidUtils.toBytes(applicationId);
        byte[] family = DESCRIPTOR_INVERSE.getName();
        byte[] qualifier = DESCRIPTOR_INVERSE.getName();

        Get get = new Get(rowKey);
        get.addColumn(family, qualifier);

        return hbaseTemplate.get(tableName, get, this.inverseRowMapper);
    }

    @Override
    public List<Application> getApplications() {
        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_FORWARD.getTable());
        byte[] family = DESCRIPTOR_FORWARD.getName();
        byte[] qualifier = DESCRIPTOR_FORWARD.getName();

        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addColumn(family, qualifier);

        return hbaseTemplate.find(tableName, scan, this.applicationRowMapper);
    }

    private static byte[] encodeStringAsRowKey(String str) {
        return HashUtils.hashBytes(BytesUtils.toBytes(str)).asBytes();
    }

}
