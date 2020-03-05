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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.flink.Bootstrap;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.io.RichOutputFormat;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class StatisticsDao extends RichOutputFormat<Tuple3<String, JoinStatBo, Long>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final long serialVersionUID = 1L;
    private transient CpuLoadDao cpuLoadDao;
    private transient MemoryDao memoryDao;
    private transient TransactionDao transactionDao;
    private transient ActiveTraceDao activeTraceDao;
    private transient ResponseTimeDao responseTimeDao;
    private transient DataSourceDao dataSourceDao;
    private transient FileDescriptorDao fileDescriptorDao;
    private transient DirectBufferDao directBufferDao;
    private transient StatisticsDaoInterceptor statisticsDaoInterceptor;

    @Override
    public void configure(Configuration parameters) {
        ExecutionConfig.GlobalJobParameters globalJobParameters = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        Bootstrap bootstrap = Bootstrap.getInstance(globalJobParameters.toMap());
        cpuLoadDao = bootstrap.getCpuLoadDao();
        memoryDao = bootstrap.getMemoryDao();
        transactionDao = bootstrap.getTransactionDao();
        activeTraceDao = bootstrap.getActiveTraceDao();
        responseTimeDao = bootstrap.getResponseTimeDao();
        dataSourceDao = bootstrap.getDataSourceDao();
        fileDescriptorDao = bootstrap.getFileDescriptorDao();
        directBufferDao = bootstrap.getDirectBufferDao();
        statisticsDaoInterceptor = bootstrap.getStatisticsDaoInterceptor();
    }

    @Override
    public void open(int taskNumber, int numTasks) throws IOException {
    }

    @Override
    public void writeRecord(Tuple3<String, JoinStatBo, Long> statData) throws IOException {
        statisticsDaoInterceptor.before(statData);

        try {
            JoinStatBo joinStatBo = (JoinStatBo) statData.f1;
            if (joinStatBo instanceof JoinAgentStatBo) {
                if (logger.isDebugEnabled()) {
                    logger.debug("JoinAgentStatBo insert data : {}", joinStatBo);
                }
                insertJoinAgentStatBo((JoinAgentStatBo) joinStatBo);
            } else if (joinStatBo instanceof JoinApplicationStatBo) {
//            logger.info("JoinApplicationStatBo insert data : " + joinStatBo);
                insertJoinApplicationStatBo((JoinApplicationStatBo) joinStatBo);
            }
        } finally {
            statisticsDaoInterceptor.after();
        }
    }

    private void insertJoinApplicationStatBo(JoinApplicationStatBo joinApplicationStatBo) {
        List<JoinStatBo> joinCpuLoadBoList = castJoinStatBoList(joinApplicationStatBo.getJoinCpuLoadBoList());
        List<JoinStatBo> joinMemoryBoList = castJoinStatBoList(joinApplicationStatBo.getJoinMemoryBoList());
        List<JoinStatBo> joinTransactionBoList = castJoinStatBoList(joinApplicationStatBo.getJoinTransactionBoList());
        List<JoinStatBo> joinActiveTraceBoList = castJoinStatBoList(joinApplicationStatBo.getJoinActiveTraceBoList());
        List<JoinStatBo> joinResponseTimeBoList = castJoinStatBoList(joinApplicationStatBo.getJoinResponseTimeBoList());
        List<JoinStatBo> joinDataSourceBoList = castJoinStatBoList(joinApplicationStatBo.getJoinDataSourceListBoList());
        List<JoinStatBo> joinFileDescriptorBoList = castJoinStatBoList(joinApplicationStatBo.getJoinFileDescriptorBoList());
        List<JoinStatBo> joinDirectBufferBoList = castJoinStatBoList(joinApplicationStatBo.getJoinDirectBufferBoList());

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
            fileDescriptorDao.insert(id, timestamp, joinFileDescriptorBoList, StatType.APP_FILE_DESCRIPTOR);
            directBufferDao.insert(id, timestamp, joinDirectBufferBoList, StatType.APP_DIRECT_BUFFER);
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