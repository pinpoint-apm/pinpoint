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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;

/**
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseApplicationIndexDao implements ApplicationIndexDao {

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    @Qualifier("applicationNameMapper")
    private RowMapper<List<Application>> applicationNameMapper;

    @Autowired
    @Qualifier("agentIdMapper")
    private RowMapper<List<String>> agentIdMapper;

    @Override
    public List<Application> selectAllApplicationNames() {
        Scan scan = new Scan();
        scan.setCaching(30);
        List<List<Application>> results = hbaseOperations2.find(HBaseTables.APPLICATION_INDEX, scan, applicationNameMapper);
        List<Application> applications = new ArrayList<>();
        for (List<Application> result : results) {
            applications.addAll(result);
        }
        return applications;
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        byte[] rowKey = Bytes.toBytes(applicationName);

        Get get = new Get(rowKey);
        get.addFamily(HBaseTables.APPLICATION_INDEX_CF_AGENTS);

        return hbaseOperations2.get(HBaseTables.APPLICATION_INDEX, get, agentIdMapper);
    }

    @Override
    public void deleteApplicationName(String applicationName) {
        byte[] rowKey = Bytes.toBytes(applicationName);
        Delete delete = new Delete(rowKey);
        hbaseOperations2.delete(HBaseTables.APPLICATION_INDEX, delete);
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
                if (!StringUtils.isEmpty(agentId)) {
                    delete.addColumns(HBaseTables.APPLICATION_INDEX_CF_AGENTS, Bytes.toBytes(agentId));
                }
            }
            // don't delete if nothing has been specified except row
            if (!delete.getFamilyCellMap().isEmpty()) {
                deletes.add(delete);
            }
        }
        hbaseOperations2.delete(HBaseTables.APPLICATION_INDEX, deletes);
    }

    @Override
    public void deleteAgentId(String applicationName, String agentId) {
        if (StringUtils.isEmpty(applicationName)) {
            throw new IllegalArgumentException("applicationName cannot be empty");
        }
        if (StringUtils.isEmpty(agentId)) {
            throw new IllegalArgumentException("agentId cannot be empty");
        }
        byte[] rowKey = Bytes.toBytes(applicationName);
        Delete delete = new Delete(rowKey);
        byte[] qualifier = Bytes.toBytes(agentId);
        delete.addColumns(HBaseTables.APPLICATION_INDEX_CF_AGENTS, qualifier);
        hbaseOperations2.delete(HBaseTables.APPLICATION_INDEX, delete);
    }
}
