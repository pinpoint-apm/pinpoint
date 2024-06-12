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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author netspider
 * @author emeroad
 */
@Component
public class HbaseApplicationIndexDao implements ApplicationIndexDao {

    private static final HbaseColumnFamily.ApplicationIndex DESCRIPTOR = HbaseColumnFamily.APPLICATION_INDEX_AGENTS;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<List<Application>> applicationNameMapper;

    private final RowMapper<List<String>> agentIdMapper;


    public HbaseApplicationIndexDao(HbaseOperations hbaseOperations,
                                    TableNameProvider tableNameProvider,
                                    @Qualifier("applicationNameMapper") RowMapper<List<Application>> applicationNameMapper,
                                    @Qualifier("agentIdMapper") RowMapper<List<String>> agentIdMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.applicationNameMapper = Objects.requireNonNull(applicationNameMapper, "applicationNameMapper");
        this.agentIdMapper = Objects.requireNonNull(agentIdMapper, "agentIdMapper");
    }

    @Override
    public List<Application> selectAllApplications() {
        Scan scan = new Scan();
        scan.setCaching(30);
        scan.addFamily(DESCRIPTOR.getName());

        TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        List<List<Application>> results = hbaseOperations.find(applicationIndexTableName, scan, applicationNameMapper);

        return ListListUtils.toList(results);
    }

    @Override
    public List<Application> selectApplicationByName(String applicationName) {
        return selectApplicationIndex0(applicationName, applicationNameMapper);
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        return selectApplicationIndex0(applicationName, agentIdMapper);
    }

    private <T> List<T> selectApplicationIndex0(String applicationName, RowMapper<List<T>> rowMapper) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(rowMapper, "rowMapper");

        byte[] rowKey = Bytes.toBytes(applicationName);

        Get get = new Get(rowKey);
        get.addFamily(DESCRIPTOR.getName());

        TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.get(applicationIndexTableName, get, rowMapper);
    }

    @Override
    public void deleteApplicationName(String applicationName) {
        Objects.requireNonNull(applicationName, "applicationName");

        byte[] rowKey = Bytes.toBytes(applicationName);
        Delete delete = new Delete(rowKey);

        TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseOperations.delete(applicationIndexTableName, delete);
    }

    @Override
    public void deleteAgentIds(Map<String, List<String>> applicationAgentIdMap) {
        if (MapUtils.isEmpty(applicationAgentIdMap)) {
            return;
        }

        List<Delete> deletes = new ArrayList<>(applicationAgentIdMap.size());

        for (Map.Entry<String, List<String>> entry : applicationAgentIdMap.entrySet()) {
            String applicationName = entry.getKey();
            List<String> agentIds = entry.getValue();
            if (StringUtils.isEmpty(applicationName) || CollectionUtils.isEmpty(agentIds)) {
                continue;
            }
            Delete delete = new Delete(Bytes.toBytes(applicationName));
            for (String agentId : agentIds) {
                if (StringUtils.hasLength(agentId)) {
                    delete.addColumns(DESCRIPTOR.getName(), Bytes.toBytes(agentId));
                }
            }
            // don't delete if nothing has been specified except row
            if (!delete.getFamilyCellMap().isEmpty()) {
                deletes.add(delete);
            }
        }

        TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseOperations.delete(applicationIndexTableName, deletes);
    }

    @Override
    public void deleteAgentId(String applicationName, String agentId) {
        Assert.hasLength(applicationName, "applicationName");
        Assert.hasLength(agentId, "agentId");

        byte[] rowKey = Bytes.toBytes(applicationName);
        Delete delete = new Delete(rowKey);
        byte[] qualifier = Bytes.toBytes(agentId);
        delete.addColumns(DESCRIPTOR.getName(), qualifier);

        TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        hbaseOperations.delete(applicationIndexTableName, delete);
    }

}
