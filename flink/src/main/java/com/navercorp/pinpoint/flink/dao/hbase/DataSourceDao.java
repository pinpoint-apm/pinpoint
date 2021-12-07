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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.join.DataSourceSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class DataSourceDao {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseTemplate2 hbaseTemplate2;
    private final ApplicationStatHbaseOperationFactory applicationStatHbaseOperationFactory;
    private final DataSourceSerializer dataSourceSerializer;
    private final TableNameProvider tableNameProvider;

    public DataSourceDao(@Qualifier("asyncPutHbaseTemplate") HbaseTemplate2 hbaseTemplate2,
                         ApplicationStatHbaseOperationFactory applicationStatHbaseOperationFactory,
                         DataSourceSerializer dataSourceSerializer,
                         TableNameProvider tableNameProvider) {
        this.hbaseTemplate2 = Objects.requireNonNull(hbaseTemplate2, "hbaseTemplate2");
        this.applicationStatHbaseOperationFactory = Objects.requireNonNull(applicationStatHbaseOperationFactory, "applicationStatHbaseOperationFactory");
        this.dataSourceSerializer = Objects.requireNonNull(dataSourceSerializer, "dataSourceSerializer");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    public void insert(String id, long timestamp, List<JoinStatBo> joinResponseTimeBoList, StatType statType) {
        if (logger.isDebugEnabled()) {
            logger.debug("[insert] {} : ({})", new Date(timestamp), joinResponseTimeBoList);
        }
        List<Put> responseTimePuts = applicationStatHbaseOperationFactory.createPuts(id, joinResponseTimeBoList, statType, dataSourceSerializer);
        if (!responseTimePuts.isEmpty()) {
            TableName applicationStatAggreTableName = tableNameProvider.getTableName(HbaseTable.APPLICATION_STAT_AGGRE);
            hbaseTemplate2.asyncPut(applicationStatAggreTableName, responseTimePuts);
        }
    }
}
