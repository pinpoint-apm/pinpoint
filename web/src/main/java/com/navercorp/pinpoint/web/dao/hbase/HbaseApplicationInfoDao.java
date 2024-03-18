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
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.server.bo.ApplicationInfo;
import com.navercorp.pinpoint.common.server.bo.ApplicationSelector;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.dao.ApplicationInfoDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.CheckAndMutateResult;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final RowMapper<ApplicationInfo> forwardRowMapper;
    private final RowMapper<ApplicationId> inverseRowMapper;
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    public HbaseApplicationInfoDao(
            HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            @Qualifier("applicationIdForwardMapper") RowMapper<ApplicationInfo> forwardRowMapper,
            @Qualifier("applicationIdInverseMapper") RowMapper<ApplicationId> inverseRowMapper,
            ServiceTypeRegistryService serviceTypeRegistryService
    ) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.forwardRowMapper = Objects.requireNonNull(forwardRowMapper, "forwardRowMapper");
        this.inverseRowMapper = Objects.requireNonNull(inverseRowMapper, "inverseRowMapper");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
    }

    @Override
    public ApplicationId getApplicationId(ApplicationSelector selector) {
        Objects.requireNonNull(selector, "selector");
        Objects.requireNonNull(selector.serviceId(), "selector.serviceId");
        Objects.requireNonNull(selector.name(), "selector.applicationName");

        if (logger.isDebugEnabled()) {
            logger.debug("getApplicationId() serviceId: {}, applicationName: {}", selector.serviceId(), selector.name());
        }

        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_INVERSE.getTable());
        byte[] rowKey = selector.toBytes();
        byte[] family = DESCRIPTOR_INVERSE.getName();
        byte[] qualifier = DESCRIPTOR_INVERSE.getName();

        Get get = new Get(rowKey);
        get.addColumn(family, qualifier);

        return hbaseTemplate.get(tableName, get, this.inverseRowMapper);
    }

    @Override
    public Application getApplication(ApplicationId applicationId) {
        Objects.requireNonNull(applicationId, "applicationId");

        if (logger.isDebugEnabled()) {
            logger.debug("getApplication() applicationId:{}", applicationId);
        }

        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_FORWARD.getTable());
        byte[] rowKey = applicationId.toBytes();
        byte[] family = DESCRIPTOR_FORWARD.getName();
        byte[] qualifier = DESCRIPTOR_FORWARD.getName();

        Get get = new Get(rowKey);
        get.addColumn(family, qualifier);

        ApplicationInfo info = hbaseTemplate.get(tableName, get, this.forwardRowMapper);
        if (info == null) {
            return null;
        }

        return new Application(info.id(), info.name(), getServiceType(info.serviceTypeCode()));
    }

    @Override
    public List<Application> getApplications() {
        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_FORWARD.getTable());
        byte[] family = DESCRIPTOR_FORWARD.getName();
        byte[] qualifier = DESCRIPTOR_FORWARD.getName();

        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addColumn(family, qualifier);

        List<ApplicationInfo> infos = hbaseTemplate.find(tableName, scan, this.forwardRowMapper);
        return mapApplicationInfos(infos);
    }

    @Override
    public ApplicationId putApplicationIdIfAbsent(ApplicationInfo application) {
        Objects.requireNonNull(application, "application");

        if (logger.isDebugEnabled()) {
            logger.debug("putApplicationIdIfAbsent() serviceId: {}, applicationName:{}, applicationId:{}",
                    application.serviceId(), application.name(), application.id());
        }

        CheckAndMutateResult camResult = putInverse(application);
        if (camResult.isSuccess()) {
            putForward(application);
        }

        return getApplicationId(new ApplicationSelector(application.serviceId(), application.name(), application.serviceTypeCode()));
    }

    @Override
    public List<ApplicationId> getApplications(ServiceId serviceId) {
        Objects.requireNonNull(serviceId, "serviceId");

        if (logger.isDebugEnabled()) {
            logger.debug("getApplications() serviceId:{}", serviceId);
        }

        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_INVERSE.getTable());

        Scan scan = new Scan();
        scan.setStartStopRowForPrefixScan(serviceId.toBytes());

        return hbaseTemplate.find(tableName, scan, this.inverseRowMapper);
    }

    private CheckAndMutateResult putInverse(ApplicationInfo application) {
        ApplicationSelector selector = new ApplicationSelector(application.serviceId(), application.name(), application.serviceTypeCode());

        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_INVERSE.getTable());
        byte[] rowKey = selector.toBytes();
        byte[] family = DESCRIPTOR_INVERSE.getName();
        byte[] qualifier = DESCRIPTOR_INVERSE.getName();
        byte[] value = application.id().toBytes();

        Put put = new Put(rowKey);
        put.addColumn(family, qualifier, value);

        CheckAndMutate checkAndMutate = CheckAndMutate.newBuilder(rowKey)
                .ifNotExists(family, qualifier)
                .build(put);

        return hbaseTemplate.checkAndMutate(tableName, checkAndMutate);
    }

    private void putForward(ApplicationInfo application) {
        ApplicationSelector selector = new ApplicationSelector(application.serviceId(), application.name(), application.serviceTypeCode());

        TableName tableName = this.tableNameProvider.getTableName(DESCRIPTOR_FORWARD.getTable());
        byte[] rowKey = application.id().toBytes();
        byte[] family = DESCRIPTOR_FORWARD.getName();
        byte[] qualifier = DESCRIPTOR_FORWARD.getName();
        byte[] value = selector.toBytes();

        Put put = new Put(rowKey);
        put.addColumn(family, qualifier, value);

        hbaseTemplate.put(tableName, put);
    }

    private List<Application> mapApplicationInfos(List<ApplicationInfo> infos) {
        List<Application> applications = new ArrayList<>(infos.size());
        for (ApplicationInfo info : infos) {
            applications.add(new Application(info.id(), info.name(), getServiceType(info.serviceTypeCode())));
        }
        return applications;
    }

    private ServiceType getServiceType(short serviceTypeCode) {
        return this.serviceTypeRegistryService.findServiceType(serviceTypeCode);
    }

}
