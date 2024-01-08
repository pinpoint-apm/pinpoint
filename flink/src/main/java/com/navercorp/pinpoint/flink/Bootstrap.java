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

import com.navercorp.pinpoint.collector.receiver.thrift.TCPReceiverBean;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.flink.cluster.FlinkServerRegister;
import com.navercorp.pinpoint.flink.config.FlinkProperties;
import com.navercorp.pinpoint.flink.dao.hbase.ApplicationMetricDao;
import com.navercorp.pinpoint.flink.dao.hbase.StatisticsDao;
import com.navercorp.pinpoint.flink.dao.hbase.StatisticsDaoInterceptor;
import com.navercorp.pinpoint.flink.function.ApplicationStatBoWindowInterceptor;
import com.navercorp.pinpoint.flink.process.ApplicationCache;
import com.navercorp.pinpoint.flink.process.TBaseFlatMapper;
import com.navercorp.pinpoint.flink.process.TBaseFlatMapperInterceptor;
import com.navercorp.pinpoint.flink.receiver.AgentStatHandler;
import com.navercorp.pinpoint.flink.receiver.TcpDispatchHandler;
import com.navercorp.pinpoint.flink.receiver.TcpSourceFunction;
import com.navercorp.pinpoint.flink.vo.RawData;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class Bootstrap {
    private static final Logger logger = LogManager.getLogger(Bootstrap.class);
    private static final String SPRING_PROFILE = "spring.profiles.active";
    private static final String PINPOINT_PROFILE = "pinpoint.profiles.active";

    private volatile static Bootstrap instance;

    private final StatisticsDao statisticsDao;

    private final ApplicationContext applicationContext;

    private final TBaseFlatMapper tbaseFlatMapper;
    private final FlinkProperties flinkProperties;
    private final TcpDispatchHandler tcpDispatchHandler;
    private final TcpSourceFunction tcpSourceFunction;
    private final ApplicationCache applicationCache;

    private final List<ApplicationMetricDao<JoinStatBo>> applicationMetricDaoList;

    private final TBaseFlatMapperInterceptor tBaseFlatMapperInterceptor;
    private final StatisticsDaoInterceptor statisticsDaoInterceptor;
    private final ApplicationStatBoWindowInterceptor applicationStatBoWindowInterceptor;
    private final AgentStatHandler agentStatHandler;

    private Bootstrap() {
        applicationContext = new AnnotationConfigApplicationContext(FlinkModule.class);

        tbaseFlatMapper = applicationContext.getBean("tbaseFlatMapper", TBaseFlatMapper.class);
        flinkProperties = applicationContext.getBean("flinkProperties", FlinkProperties.class);
        tcpDispatchHandler = applicationContext.getBean("tcpDispatchHandler", TcpDispatchHandler.class);
        tcpSourceFunction = applicationContext.getBean("tcpSourceFunction", TcpSourceFunction.class);
        applicationCache = applicationContext.getBean("applicationCache", ApplicationCache.class);
        statisticsDao = applicationContext.getBean("statisticsDao", StatisticsDao.class);

        this.applicationMetricDaoList = getApplicationMetricDao();

        tBaseFlatMapperInterceptor = applicationContext.getBean("tBaseFlatMapperInterceptor", TBaseFlatMapperInterceptor.class);
        statisticsDaoInterceptor =  applicationContext.getBean("statisticsDaoInterceptor", StatisticsDaoInterceptor.class);
        applicationStatBoWindowInterceptor = applicationContext.getBean("applicationStatBoWindowInterceptor", ApplicationStatBoWindowInterceptor.class);
        agentStatHandler = applicationContext.getBean("agentStatHandler", AgentStatHandler.class);
    }

    @SuppressWarnings("unchecked")
    private List<ApplicationMetricDao<JoinStatBo>> getApplicationMetricDao() {
        Map<String, ApplicationMetricDao> metricDaoMap = applicationContext.getBeansOfType(ApplicationMetricDao.class);

        metricDaoMap.forEach((beanName, applicationMetricDao) -> logger.info("ApplicationMetricDao BeanName:{}", beanName));

        List<ApplicationMetricDao> values = new ArrayList<>(metricDaoMap.values());
        return (List<ApplicationMetricDao<JoinStatBo>>) (List<?>) values;
    }

    public static Bootstrap getInstance(Map<String, String> jobParameters) {
        if (instance == null)  {
            synchronized(Bootstrap.class) {
                if (instance == null) {
                    String profiles = jobParameters.getOrDefault(SPRING_PROFILE, "local");
                    System.setProperty(PINPOINT_PROFILE, profiles);
                    instance = new Bootstrap();
                    logger.info("Bootstrap initialization. : job parameter " + jobParameters);
                }
            }
        }

        return instance;
    }

    public static void close() {
        synchronized(Bootstrap.class) {
            if (instance == null) {
                logger.warn("Invalid attempt of closing bootstrap: bootstrap is not initialized yet");
                return;
            }
            logger.info("Closing bootstrap: {}", instance);
            final ApplicationContext applicationContext = instance.getApplicationContext();
            if (applicationContext instanceof Closeable closeable) {
                logger.info("Closing an instance of ApplicationContext: {}", applicationContext);
                IOUtils.closeQuietly(closeable);
            } else {
                logger.warn("Invalid type of applicationContext was found: {}", applicationContext);
            }
            instance = null;
            logger.info("Closed bootstrap: {}", instance);
        }
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public StatisticsDao getStatisticsDao() {
        return statisticsDao;
    }

    public List<ApplicationMetricDao<JoinStatBo>> getApplicationMetricDaoList() {
        return applicationMetricDaoList;
    }

    public TBaseFlatMapper getTbaseFlatMapper() {
        return tbaseFlatMapper;
    }

    public ApplicationCache getApplicationCache() {
        return applicationCache;
    }

    public FlinkProperties getFlinkProperties() {
        return flinkProperties;
    }

    public StreamExecutionEnvironment createStreamExecutionEnvironment() {
        if (flinkProperties.isLocalforFlinkStreamExecutionEnvironment()) {
            LocalStreamEnvironment localEnvironment = StreamExecutionEnvironment.createLocalEnvironment();
            localEnvironment.setParallelism(1);
            return localEnvironment;
        } else {
            return StreamExecutionEnvironment.getExecutionEnvironment();
        }
    }

    public void setStatHandlerTcpDispatchHandler(SourceContext<RawData> sourceContext) {
        agentStatHandler.addSourceContext(sourceContext);
        tcpDispatchHandler.setSimpletHandler(agentStatHandler);
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

    public TBaseFlatMapperInterceptor getTbaseFlatMapperInterceptor() {
        return tBaseFlatMapperInterceptor;
    }

    public StatisticsDaoInterceptor getStatisticsDaoInterceptor() {
        return statisticsDaoInterceptor;
    }

    public ApplicationStatBoWindowInterceptor getApplicationStatBoWindowInterceptor() {
        return applicationStatBoWindowInterceptor;
    }
}