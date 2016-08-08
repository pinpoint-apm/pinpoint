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

package com.navercorp.pinpoint.collector.dao.hbase;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.navercorp.pinpoint.collector.dao.AgentIdApplicationIndexDao;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.RowMapper;

import org.springframework.stereotype.Repository;

/**
 * find applicationname by agentId
 * 
 * @author netspider
 * @author emeroad
 */
@Repository
@Deprecated
public class HbaseAgentIdApplicationIndexDao implements AgentIdApplicationIndexDao {

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    @Qualifier("applicationNameMapper")
    private RowMapper<String> applicationNameMapper;

    @Override
    public void insert(String agentId, String applicationName) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }

        byte[] agentIdByte = Bytes.toBytes(agentId);
        byte[] appNameByte = Bytes.toBytes(applicationName);

        Put put = new Put(agentIdByte);
        put.addColumn(AGENTID_APPLICATION_INDEX_CF_APPLICATION, appNameByte, appNameByte);

        hbaseTemplate.put(AGENTID_APPLICATION_INDEX, put);
    }

    @Override
    public String selectApplicationName(String agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        byte[] rowKey = Bytes.toBytes(agentId);
        Get get = new Get(rowKey);
        get.addFamily(AGENTID_APPLICATION_INDEX_CF_APPLICATION);

        return hbaseTemplate.get(AGENTID_APPLICATION_INDEX, get, applicationNameMapper);
    }
}
