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
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author minwoo.jung
 */
public class StatisticsDao extends RichSinkFunction<Tuple3<String, JoinStatBo, Long>> {
    private final static Logger logger = LogManager.getLogger(StatisticsDao.class);

    private static final long serialVersionUID = 1L;

    private transient ApplicationMetricDao<JoinStatBo>[] applicationMetricDaoList;
    private transient StatisticsDaoInterceptor statisticsDaoInterceptor;

    @Override
    public void open(Configuration parameters) throws Exception {
        ExecutionConfig.GlobalJobParameters globalJobParameters = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        Bootstrap bootstrap = Bootstrap.getInstance(globalJobParameters.toMap());
        applicationMetricDaoList = bootstrap.getApplicationMetricDaoList().toArray(new ApplicationMetricDao[0]);
        statisticsDaoInterceptor = bootstrap.getStatisticsDaoInterceptor();
    }

    @Override
    public void invoke(Tuple3<String, JoinStatBo, Long> statData, SinkFunction.Context context) throws Exception {
        statisticsDaoInterceptor.before(statData);

        try {
            JoinStatBo joinStatBo = statData.f1;
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

    private void insertJoinApplicationStatBo(JoinApplicationStatBo appMetric) {
        if (appMetric.getStatType() == StatType.APP_STST_AGGRE) {
//            logger.info("insert application aggre : " + new Date(joinApplicationStatBo.getTimestamp()) + " ("+ joinApplicationStatBo.getApplicationId() + " )");
        } else {
            for (ApplicationMetricDao<JoinStatBo> dao : applicationMetricDaoList) {
                dao.insert(appMetric);
            }
        }
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
}