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

import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.server.bo.stat.join.*;
import com.navercorp.pinpoint.flink.Bootstrap;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.CollectionUtil;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class StatisticsDao implements OutputFormat<Tuple3<String, JoinStatBo, Long>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final long serialVersionUID = 1L;
    private transient TableName APPLICATION_STAT_AGGRE;
    private transient CpuLoadDao cpuLoadDao;
    private transient MemoryDao memoryDao;
    private transient TransactionDao transactionDao;
    private transient ActiveTraceDao activeTraceDao;
    private transient ResponseTimeDao responseTimeDao;
    private transient DataSourceDao dataSourceDao;


    public StatisticsDao() {
    }

    @Override
    public void configure(Configuration parameters) {
        this.APPLICATION_STAT_AGGRE = HBaseTables.APPLICATION_STAT_AGGRE;
        Bootstrap bootstrap = Bootstrap.getInstance();
        cpuLoadDao = bootstrap.getCpuLoadDao();
        memoryDao = bootstrap.getMemoryDao();
        transactionDao = bootstrap.getTransactionDao();
        activeTraceDao = bootstrap.getActiveTraceDao();
        responseTimeDao = bootstrap.getResponseTimeDao();
        dataSourceDao = bootstrap.getDataSourceDao();
    }

    @Override
    public void open(int taskNumber, int numTasks) throws IOException {
    }

    @Override
    public void writeRecord(Tuple3<String, JoinStatBo, Long> statData) throws IOException {
        JoinStatBo joinStatBo = (JoinStatBo)statData.f1;
        if (joinStatBo instanceof JoinAgentStatBo) {
            if (logger.isDebugEnabled()) {
                logger.debug("JoinAgentStatBo insert data : {}", joinStatBo);
            }

            insertJoinAgentStatBo((JoinAgentStatBo)joinStatBo);
        } else if (joinStatBo instanceof JoinApplicationStatBo) {
//            logger.info("JoinApplicationStatBo insert data : " + joinStatBo);
            insertJoinApplicationStatBo((JoinApplicationStatBo)joinStatBo);
        }

    }

    private void insertJoinApplicationStatBo(JoinApplicationStatBo joinApplicationStatBo) {
        List<JoinStatBo> joinCpuLoadBoList = castJoinStatBoList(joinApplicationStatBo.getJoinCpuLoadBoList());
        List<JoinStatBo> joinMemoryBoList = castJoinStatBoList(joinApplicationStatBo.getJoinMemoryBoList());
        List<JoinStatBo> joinTransactionBoList = castJoinStatBoList(joinApplicationStatBo.getJoinTransactionBoList());
        List<JoinStatBo> joinActiveTraceBoList = castJoinStatBoList(joinApplicationStatBo.getJoinActiveTraceBoList());
        List<JoinStatBo> joinResponseTimeBoList = castJoinStatBoList(joinApplicationStatBo.getJoinResponseTimeBoList());
        List<JoinStatBo> joinDataSourceBoList = castJoinStatBoList(joinApplicationStatBo.getJoinDataSourceListBoList());

        if (joinApplicationStatBo.getStatType() == StatType.APP_STST_AGGRE) {
//            logger.info("insert application aggre : " + new Date(joinApplicationStatBo.getTimestamp()) + " ("+ joinApplicationStatBo.getApplicationId() + " )");
        } else {
            final String id = joinApplicationStatBo.getId();
            final long timestamp = joinApplicationStatBo.getTimestamp();
            cpuLoadDao.insert(id, timestamp, joinCpuLoadBoList, StatType.APP_CPU_LOAD);
            memoryDao.insert(id, timestamp, joinMemoryBoList, StatType.APP_MEMORY_USED);
            transactionDao.insert(id, timestamp, joinTransactionBoList, StatType.APP_TRANSACTION_COUNT);
            activeTraceDao.insert(id, timestamp, joinActiveTraceBoList, StatType.APP_ACTIVE_TRACE_COUNT);
            responseTimeDao.insert(id, timestamp, joinResponseTimeBoList, StatType.APP_RESPONSE_TIME);
            dataSourceDao.insert(id, timestamp, joinDataSourceBoList, StatType.APP_DATA_SOURCE);
        }
    }

    private List<JoinStatBo> castJoinStatBoList(List<? extends JoinStatBo> JoinStatBoList) {
        if (CollectionUtil.isNullOrEmpty(JoinStatBoList)) {
            return new ArrayList<>(0);
        }

        return new ArrayList<>(JoinStatBoList);
    }

    private void insertJoinAgentStatBo(JoinAgentStatBo joinAgentStatBo) {
//        logger.info("insert agent data : " + new Date(joinAgentStatBo.getTimestamp()));
//        String rowKey = joinAgentStatBo.getAgentId() + "_" + AgentStatType.CPU_LOAD.getRawTypeCode() +"_" + joinAgentStatBo.getTimestamp();
//        Put put = new Put(rowKey.getBytes());
//
//        final Buffer valueBuffer = new AutomaticBuffer();
//        JoinCpuLoadBo joinCpuLoadBo = joinAgentStatBo.getJoinCpuLoadBoList().get(0);
//        valueBuffer.putByte(joinCpuLoadBo.getVersion());
//        valueBuffer.putDouble(joinCpuLoadBo.getJvmCpuLoad());
//        valueBuffer.putDouble(joinCpuLoadBo.getMaxJvmCpuLoad());
//        valueBuffer.putDouble(joinCpuLoadBo.getMinJvmCpuLoad());
//        valueBuffer.putDouble(joinCpuLoadBo.getSystemCpuLoad());
//        valueBuffer.putDouble(joinCpuLoadBo.getMaxSystemCpuLoad());
//        valueBuffer.putDouble(joinCpuLoadBo.getMinSystemCpuLoad());
//
//        final Buffer qualifierBuffer = new AutomaticBuffer(64);
//        qualifierBuffer.putVLong(joinAgentStatBo.getTimestamp());
//
//        put.addColumn(STAT_METADATA_CF, Bytes.toBytes(qualifierBuffer.wrapByteBuffer()), Bytes.toBytes(valueBuffer.wrapByteBuffer()));
//        hbaseTemplate2.put(TableName.valueOf("AgentStatV2Aggre"), put);
    }

    @Override
    public void close() throws IOException {
    }
}