/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.ServiceInfoDao;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.server.bo.ServiceInfo;
import com.navercorp.pinpoint.common.util.BytesUtils;
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

/**
 * @author youngjin.kim2
 */
@Repository
public class HbaseServiceInfoDao implements ServiceInfoDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final HbaseColumnFamily.ServiceId DESCRIPTOR_FORWARD = HbaseColumnFamily.SERVICE_ID_FORWARD;
    private static final HbaseColumnFamily.ServiceId DESCRIPTOR_INVERSE = HbaseColumnFamily.SERVICE_ID_INVERSE;

    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;
    private final RowMapper<ServiceInfo> forwardRowMapper;
    private final RowMapper<ServiceId> inverseRowMapper;

    public HbaseServiceInfoDao(
            HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            @Qualifier("serviceIdForwardMapper") RowMapper<ServiceInfo> forwardRowMapper,
            @Qualifier("serviceIdInverseMapper") RowMapper<ServiceId> inverseRowMapper) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.forwardRowMapper = Objects.requireNonNull(forwardRowMapper, "forwardRowMapper");
        this.inverseRowMapper = Objects.requireNonNull(inverseRowMapper, "inverseRowMapper");
    }

    @Override
    public ServiceId getServiceId(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");

        if (logger.isDebugEnabled()) {
            logger.debug("getServiceId() serviceName:{}", serviceName);
        }

        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_INVERSE.getTable());
        byte[] rowKey = encodeStringAsRowKey(serviceName);
        byte[] family = DESCRIPTOR_INVERSE.getName();
        byte[] qualifier = DESCRIPTOR_INVERSE.getName();

        Get get = new Get(rowKey);
        get.addColumn(family, qualifier);

        return hbaseTemplate.get(tableName, get, this.inverseRowMapper);
    }

    @Override
    public ServiceInfo getServiceInfo(ServiceId serviceId) {
        Objects.requireNonNull(serviceId, "serviceId");

        if (logger.isDebugEnabled()) {
            logger.debug("getServiceInfo() serviceId:{}", serviceId);
        }

        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_FORWARD.getTable());
        byte[] rowKey = serviceId.toBytes();
        byte[] family = DESCRIPTOR_FORWARD.getName();
        byte[] qualifier = DESCRIPTOR_FORWARD.getName();

        Get get = new Get(rowKey);
        get.addColumn(family, qualifier);

        return hbaseTemplate.get(tableName, get, this.forwardRowMapper);
    }

    @Override
    public ServiceId putServiceIdIfAbsent(String serviceName, ServiceId serviceId) {
        Objects.requireNonNull(serviceName, "serviceName");
        Objects.requireNonNull(serviceId, "serviceId");

        if (logger.isDebugEnabled()) {
            logger.debug("putServiceIdIfAbsent() serviceName:{}, serviceId:{}", serviceName, serviceId);
        }

        ServiceInfo service = new ServiceInfo(serviceId, serviceName);
        CheckAndMutateResult camResult = this.putInverse(service);

        if (camResult.isSuccess()) {
            putForward(service);
        }

        return getServiceId(serviceName);
    }

    @Override
    public void ensurePut(String serviceName, ServiceId serviceId) {
        Objects.requireNonNull(serviceName, "serviceName");
        Objects.requireNonNull(serviceId, "serviceId");

        if (logger.isDebugEnabled()) {
            logger.debug("ensurePut() serviceName:{}, serviceId:{}", serviceName, serviceId);
        }

        ServiceInfo service = new ServiceInfo(serviceId, serviceName);
        putForward(service);
    }

    private CheckAndMutateResult putInverse(ServiceInfo service) {
        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_INVERSE.getTable());
        byte[] rowKey = encodeStringAsRowKey(service.name());
        byte[] family = DESCRIPTOR_INVERSE.getName();
        byte[] qualifier = DESCRIPTOR_INVERSE.getName();
        byte[] value = service.id().toBytes();

        Put put = new Put(rowKey);
        put.addColumn(family, qualifier, value);

        CheckAndMutate checkAndMutate = CheckAndMutate.newBuilder(rowKey)
                .ifNotExists(family, qualifier)
                .build(put);

        return hbaseTemplate.checkAndMutate(tableName, checkAndMutate);
    }

    private void putForward(ServiceInfo service) {
        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_FORWARD.getTable());
        byte[] rowKey = service.id().toBytes();
        byte[] family = DESCRIPTOR_FORWARD.getName();
        byte[] qualifier = DESCRIPTOR_FORWARD.getName();
        byte[] value = BytesUtils.toBytes(service.name());

        Put put = new Put(rowKey);
        put.addColumn(family, qualifier, value);

        hbaseTemplate.put(tableName, put);
    }

    private static byte[] encodeStringAsRowKey(String str) {
        return BytesUtils.toBytes(str);
    }

}
