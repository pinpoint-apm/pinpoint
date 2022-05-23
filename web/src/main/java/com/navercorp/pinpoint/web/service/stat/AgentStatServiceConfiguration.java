package com.navercorp.pinpoint.web.service.stat;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.dao.SampledAgentStatDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentUriStat;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSourceList;
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
import com.navercorp.pinpoint.web.vo.stat.SampledDirectBuffer;
import com.navercorp.pinpoint.web.vo.stat.SampledFileDescriptor;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.SampledLoadedClassCount;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;
import com.navercorp.pinpoint.web.vo.stat.SampledTotalThreadCount;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.ActiveTraceChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentUriStatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.CpuLoadChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DataSourceChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DeadlockChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DirectBufferChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.FileDescriptorChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.JvmGcChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.JvmGcDetailedChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.LoadedClassCountChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.ResponseTimeChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.TotalThreadCountChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.TransactionChart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentStatServiceConfiguration {

    @Bean
    public AgentStatChartService<ActiveTraceChart> getActiveTraceChartService(SampledAgentStatDao<SampledActiveTrace> statDao) {
        return new DefaultAgentStatChartService<>(statDao, ActiveTraceChart::new);
    }

    @Bean
    public AgentStatService<ActiveTraceBo> getActiveTraceService(AgentStatDao<ActiveTraceBo> statDao) {
        return new DefaultStatService<>(statDao);
    }

    //-----------------

    @Bean
    public AgentStatChartService<CpuLoadChart> getCpuLoadChartService(SampledAgentStatDao<SampledCpuLoad> statDao) {
        return new DefaultAgentStatChartService<>(statDao, CpuLoadChart::new);
    }

    @Bean()
    public AgentStatService<CpuLoadBo> getCpuLoadService(AgentStatDao<CpuLoadBo> statDao) {
        return new DefaultStatService<>(statDao);
    }

    //-----------------------

    @Bean
    public AgentStatChartService<DeadlockChart> getDeadlockChartService(SampledAgentStatDao<SampledDeadlock> statDao) {
        return new DefaultAgentStatChartService<>(statDao, DeadlockChart::new);
    }

    @Bean
    public AgentStatService<DeadlockThreadCountBo> getDeadlockService(AgentStatDao<DeadlockThreadCountBo> statDao) {
        return new DefaultStatService<>(statDao);
    }

    //-----------------------

    @Bean
    public AgentStatChartService<DirectBufferChart> getDirectBufferChartService(SampledAgentStatDao<SampledDirectBuffer> statDao) {
        return new DefaultAgentStatChartService<>(statDao, DirectBufferChart::new);
    }

    @Bean
    public AgentStatService<DirectBufferBo> getDirectBufferService(AgentStatDao<DirectBufferBo> statDao) {
        return new DefaultStatService<>(statDao);
    }

    //-----------------------

    @Bean
    public AgentStatChartService<FileDescriptorChart> getFileDescriptorChartService(SampledAgentStatDao<SampledFileDescriptor> statDao) {
        return new DefaultAgentStatChartService<>(statDao, FileDescriptorChart::new);
    }

    @Bean
    public AgentStatService<FileDescriptorBo> getFileDescriptorService(AgentStatDao<FileDescriptorBo> statDao) {
        return new DefaultStatService<>(statDao);
    }

    //-----------------------

    @Bean
    public AgentStatChartService<JvmGcChart> getJvmGcChartService(SampledAgentStatDao<SampledJvmGc> statDao) {
        return new DefaultAgentStatChartService<>(statDao, JvmGcChart::new);
    }

    @Bean
    public AgentStatService<JvmGcBo> getJvmGcService(AgentStatDao<JvmGcBo> statDao) {
        return new DefaultStatService<>(statDao);
    }

    //-----------------------

    @Bean
    public AgentStatChartService<JvmGcDetailedChart> getJvmGcDetailedChartService(SampledAgentStatDao<SampledJvmGcDetailed> statDao) {
        return new DefaultAgentStatChartService<>(statDao, JvmGcDetailedChart::new);
    }

    @Bean
    public AgentStatService<? extends AgentStatDataPoint> getJvmGcDetailedService(AgentStatDao<JvmGcDetailedBo> statDao) {
        return new DefaultStatService<>(statDao);
    }

    //-----------------------

    @Bean
    public AgentStatChartService<LoadedClassCountChart> getLoadedClassCountChartService(SampledAgentStatDao<SampledLoadedClassCount> statDao) {
        return new DefaultAgentStatChartService<>(statDao, LoadedClassCountChart::new);
    }

    @Bean
    public AgentStatService<LoadedClassBo> getLoadedClassCountService(AgentStatDao<LoadedClassBo> statDao) {
        return new DefaultStatService<>(statDao);
    }


    //-----------------------

    @Bean
    public AgentStatChartService<ResponseTimeChart> getResponseTimeChartService(SampledAgentStatDao<SampledResponseTime> statDao) {
        return new DefaultAgentStatChartService<>(statDao, ResponseTimeChart::new);
    }

    @Bean
    public AgentStatService<ResponseTimeBo> getResponseTimeService(AgentStatDao<ResponseTimeBo> statDao) {
        return new DefaultStatService<>(statDao);
    }

    //-----------------------

    @Bean
    public AgentStatChartService<TotalThreadCountChart> getTotalThreadCountChartService(SampledAgentStatDao<SampledTotalThreadCount> statDao) {
        return new DefaultAgentStatChartService<>(statDao, TotalThreadCountChart::new);
    }

    @Bean
    public AgentStatService<TotalThreadCountBo> getTotalThreadCountService(AgentStatDao<TotalThreadCountBo> statDao) {
        return new DefaultStatService<>(statDao);
    }

    //-----------------------

//    @Bean
    public AgentStatChartService<DataSourceChart> getDataSourceChartService(SampledAgentStatDao<SampledDataSourceList> statDao,
                                                                            ServiceTypeRegistryService registry) {
        return new DataSourceChartService(statDao, registry);
    }

    @Bean
    public AgentStatService<DataSourceListBo> getDataSourceService(AgentStatDao<DataSourceListBo> statDao) {
        return new DefaultStatService<>(statDao);
    }

    //-----------------------

    //    @Bean
    public AgentStatChartService<AgentUriStatChart> getAgentUriStatChartService(SampledAgentStatDao<SampledAgentUriStat> statDao) {
        return new AgentUriStatChartService(statDao);
    }

    @Bean
    public AgentStatService<AgentUriStatBo> getAgentUriChartService(AgentStatDao<AgentUriStatBo> statDao) {
        return new DefaultStatService<>(statDao);
    }

    //-----------------------

    @Bean
    public AgentStatChartService<TransactionChart> getTransactionChartService(SampledAgentStatDao<SampledTransaction> statDao) {
        return new DefaultAgentStatChartService<>(statDao, TransactionChart::new);
    }

    @Bean
    public AgentStatService<TransactionBo> getTransactionService(AgentStatDao<TransactionBo> statDao) {
        return new DefaultStatService<>(statDao);
    }
}
