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

import com.navercorp.pinpoint.collector.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * application names list.
 *
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseApplicationIndexDao implements ApplicationIndexDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseTemplate;

    private final TableDescriptor<HbaseColumnFamily.ApplicationIndex> descriptor;

    public HbaseApplicationIndexDao(HbaseOperations2 hbaseTemplate, TableDescriptor<HbaseColumnFamily.ApplicationIndex> descriptor) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public void insert(final AgentInfoBo agentInfo) {
        Objects.requireNonNull(agentInfo, "agentInfo");

        // Assert agentId
        CollectorUtils.checkAgentId(agentInfo.getAgentId());
        // Assert applicationName
        CollectorUtils.checkApplicationName(agentInfo.getApplicationName());

        final Put put = new Put(Bytes.toBytes(agentInfo.getApplicationName()));
        final byte[] qualifier = Bytes.toBytes(agentInfo.getAgentId());
        final byte[] value = Bytes.toBytes(agentInfo.getServiceTypeCode());
        put.addColumn(descriptor.getColumnFamilyName(), qualifier, value);

        final TableName applicationIndexTableName = descriptor.getTableName();
        hbaseTemplate.put(applicationIndexTableName, put);

        logger.debug("Insert ApplicationIndex: {}", agentInfo);
    }
}