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
package com.navercorp.pinpoint.flink;

import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.join.CpuLoadSerializer;
import com.navercorp.pinpoint.flink.cluster.FlinkServerRegister;
import com.navercorp.pinpoint.flink.config.FlinkConfiguration;
import com.navercorp.pinpoint.flink.dao.hbase.StatisticsDao;
import com.navercorp.pinpoint.flink.process.TbaseFlatMapper;
import com.navercorp.pinpoint.flink.receiver.AgentStatHandler;
import com.navercorp.pinpoint.flink.receiver.TCPReceiver;
import com.navercorp.pinpoint.flink.receiver.TcpDispatchHandler;
import com.navercorp.pinpoint.flink.receiver.TcpSourceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext;
import org.apache.thrift.TBase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author minwoo.jung
 */
public class Bootstrap {

    private final static Bootstrap INSTANCE = new Bootstrap();

    private final StatisticsDao statisticsDao;

    private final ApplicationContext applicationContext;

    private final TbaseFlatMapper tbaseFlatMapper;
    private final FlinkConfiguration flinkConfiguration;
    private final TcpDispatchHandler tcpDispatchHandler;
    private final TcpSourceFunction tcpSourceFunction;
    private Bootstrap() {
        String[] SPRING_CONFIG_XML = new String[]{"applicationContext-flink.xml", "applicationContext-cache.xml"};
        applicationContext = new ClassPathXmlApplicationContext(SPRING_CONFIG_XML);

        tbaseFlatMapper = applicationContext.getBean("tbaseFlatMapper", TbaseFlatMapper.class);
        flinkConfiguration = applicationContext.getBean("flinkConfiguration", FlinkConfiguration.class);
        tcpDispatchHandler = applicationContext.getBean("tcpDispatchHandler", TcpDispatchHandler.class);
        tcpSourceFunction = applicationContext.getBean("tcpSourceFunction", TcpSourceFunction.class);
        statisticsDao = applicationContext.getBean("statisticsDao", StatisticsDao.class);

    }

    public static Bootstrap getInstance() {
        return INSTANCE;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public StatisticsDao getStatisticsDao() {
        return statisticsDao;
    }

    public TbaseFlatMapper getTbaseFlatMapper() {
        return tbaseFlatMapper;
    }

    public FlinkConfiguration getFlinkConfiguration() {
        return flinkConfiguration;
    }

    public StreamExecutionEnvironment createStreamExecutionEnvironment() {
        if (flinkConfiguration.isLocalforFlinkStreamExecutionEnvironment()) {
            return StreamExecutionEnvironment.createLocalEnvironment();
        } else {
            return StreamExecutionEnvironment.getExecutionEnvironment();
        }
    }

    public void setSourceFunctionParallel(DataStreamSource rawData) {
        int parallel = flinkConfiguration.getFlinkSourceFunctionParallel();
        rawData.setParallelism(parallel);
    }

    public void setStatHandlerTcpDispatchHandler(SourceContext<TBase> sourceContext) {
        AgentStatHandler agentStatHandler = new AgentStatHandler(sourceContext);
        tcpDispatchHandler.setAgentStatHandler(agentStatHandler);
    }

    public FlinkServerRegister initFlinkServerRegister() {
        return applicationContext.getBean("flinkServerRegister", FlinkServerRegister.class);
    }

    public TCPReceiver initTcpReceiver() {
        return applicationContext.getBean("tcpReceiver", TCPReceiver.class);
    }

    public TcpSourceFunction getTcpSourceFuncation() {
        return tcpSourceFunction;
    }
}
