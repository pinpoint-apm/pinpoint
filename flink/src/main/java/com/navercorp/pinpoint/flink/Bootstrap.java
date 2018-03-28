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

import com.navercorp.pinpoint.collector.receiver.TCPReceiverBean;
import com.navercorp.pinpoint.flink.cluster.FlinkServerRegister;
import com.navercorp.pinpoint.flink.config.FlinkConfiguration;
import com.navercorp.pinpoint.flink.dao.hbase.ActiveTraceDao;
import com.navercorp.pinpoint.flink.dao.hbase.CpuLoadDao;
import com.navercorp.pinpoint.flink.dao.hbase.DataSourceDao;
import com.navercorp.pinpoint.flink.dao.hbase.DirectBufferDao;
import com.navercorp.pinpoint.flink.dao.hbase.MemoryDao;
import com.navercorp.pinpoint.flink.dao.hbase.ResponseTimeDao;
import com.navercorp.pinpoint.flink.dao.hbase.StatisticsDao;
import com.navercorp.pinpoint.flink.dao.hbase.TransactionDao;
import com.navercorp.pinpoint.flink.dao.hbase.FileDescriptorDao;
import com.navercorp.pinpoint.flink.process.ApplicationCache;
import com.navercorp.pinpoint.flink.process.TBaseFlatMapper;
import com.navercorp.pinpoint.flink.receiver.AgentStatHandler;
import com.navercorp.pinpoint.flink.receiver.TcpDispatchHandler;
import com.navercorp.pinpoint.flink.receiver.TcpSourceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
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

    private final ClassPathXmlApplicationContext applicationContext;

    private final TBaseFlatMapper tbaseFlatMapper;
    private final FlinkConfiguration flinkConfiguration;
    private final TcpDispatchHandler tcpDispatchHandler;
    private final TcpSourceFunction tcpSourceFunction;
    private final ApplicationCache applicationCache;
    private final CpuLoadDao cpuLoadDao;
    private final MemoryDao memoryDao;
    private final TransactionDao transactionDao;
    private final ActiveTraceDao activeTraceDao;
    private final ResponseTimeDao responseTimeDao;
    private final DataSourceDao dataSourceDao;
    private final FileDescriptorDao fileDescriptorDao;
    private final DirectBufferDao directBufferDao;

    private Bootstrap() {
        String[] SPRING_CONFIG_XML = new String[]{"applicationContext-flink.xml", "applicationContext-cache.xml"};
        applicationContext = new ClassPathXmlApplicationContext(SPRING_CONFIG_XML);

        tbaseFlatMapper = applicationContext.getBean("tbaseFlatMapper", TBaseFlatMapper.class);
        flinkConfiguration = applicationContext.getBean("flinkConfiguration", FlinkConfiguration.class);
        tcpDispatchHandler = applicationContext.getBean("tcpDispatchHandler", TcpDispatchHandler.class);
        tcpSourceFunction = applicationContext.getBean("tcpSourceFunction", TcpSourceFunction.class);
        applicationCache = applicationContext.getBean("applicationCache", ApplicationCache.class);
        statisticsDao = applicationContext.getBean("statisticsDao", StatisticsDao.class);
        cpuLoadDao = applicationContext.getBean("cpuLoadDao", CpuLoadDao.class);
        memoryDao = applicationContext.getBean("memoryDao", MemoryDao.class);
        transactionDao = applicationContext.getBean("transactionDao", TransactionDao.class);
        activeTraceDao = applicationContext.getBean("activeTraceDao", ActiveTraceDao.class);
        responseTimeDao = applicationContext.getBean("responseTimeDao", ResponseTimeDao.class);
        dataSourceDao = applicationContext.getBean("dataSourceDao", DataSourceDao.class);
        fileDescriptorDao = applicationContext.getBean("fileDescriptorDao", FileDescriptorDao.class);
        directBufferDao = applicationContext.getBean("directBufferDao", DirectBufferDao.class);
    }

    public FileDescriptorDao getFileDescriptorDao() {
        return fileDescriptorDao;
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

    public CpuLoadDao getCpuLoadDao() {
        return cpuLoadDao;
    }

    public MemoryDao getMemoryDao() {
        return memoryDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public ActiveTraceDao getActiveTraceDao() {
        return activeTraceDao;
    }

    public ResponseTimeDao getResponseTimeDao() {
        return responseTimeDao;
    }

    public DataSourceDao getDataSourceDao() {
        return dataSourceDao;
    }

    public DirectBufferDao getDirectBufferDao() {
        return directBufferDao;
    }

    public TBaseFlatMapper getTbaseFlatMapper() {
        return tbaseFlatMapper;
    }

    public ApplicationCache getApplicationCache() {
        return applicationCache;
    }

    public FlinkConfiguration getFlinkConfiguration() {
        return flinkConfiguration;
    }

    public StreamExecutionEnvironment createStreamExecutionEnvironment() {
        if (flinkConfiguration.isLocalforFlinkStreamExecutionEnvironment()) {
            LocalStreamEnvironment localEnvironment = StreamExecutionEnvironment.createLocalEnvironment();
            localEnvironment.setParallelism(1);
            return localEnvironment;
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

    public void initTcpReceiver() {
        // lazy init
        applicationContext.getBean("tcpReceiver", TCPReceiverBean.class);
    }

    public TcpSourceFunction getTcpSourceFunction() {
        return tcpSourceFunction;
    }
}
