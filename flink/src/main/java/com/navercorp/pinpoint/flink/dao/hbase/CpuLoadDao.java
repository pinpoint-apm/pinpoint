/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.flink.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.join.CpuLoadSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class CpuLoadDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseTemplate2 hbaseTemplate2;
    private final ApplicationStatHbaseOperationFactory applicationStatHbaseOperationFactory;
    private final CpuLoadSerializer cpuLoadSerializer;
    private final TableNameProvider tableNameProvider;

    public CpuLoadDao(HbaseTemplate2 hbaseTemplate2, ApplicationStatHbaseOperationFactory applicationStatHbaseOperationFactory, CpuLoadSerializer cpuLoadSerializer, TableNameProvider tableNameProvider) {
        this.hbaseTemplate2 = Objects.requireNonNull(hbaseTemplate2, "hbaseTemplate2 must not be null");
        this.applicationStatHbaseOperationFactory = Objects.requireNonNull(applicationStatHbaseOperationFactory, "applicationStatHbaseOperationFactory must not be null");
        this.cpuLoadSerializer = Objects.requireNonNull(cpuLoadSerializer, "cpuLoadSerializer must not be null");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider must not be null");
    }

    public void insert(String id, long timestamp, List<JoinStatBo> joinCpuLoadBoList, StatType statType) {
        if (logger.isDebugEnabled()) {
            logger.debug("[insert] {} : ({})", new Date(timestamp), joinCpuLoadBoList);
        }
        List<Put> cpuLoadPuts = applicationStatHbaseOperationFactory.createPuts(id, joinCpuLoadBoList, statType, cpuLoadSerializer);
        if (!cpuLoadPuts.isEmpty()) {
            TableName applicationStatAggreTableName = tableNameProvider.getTableName(HbaseTable.APPLICATION_STAT_AGGRE);
            List<Put> rejectedPuts = hbaseTemplate2.asyncPut(applicationStatAggreTableName, cpuLoadPuts);
            if (CollectionUtils.isNotEmpty(rejectedPuts)) {
                hbaseTemplate2.put(applicationStatAggreTableName, rejectedPuts);
            }
        }
    }
}
