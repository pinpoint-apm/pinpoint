package com.navercorp.pinpoint.web.service.appmetric;

import com.navercorp.pinpoint.web.dao.appmetric.ApplicationMetricDao;

import com.navercorp.pinpoint.web.vo.stat.AggreJoinActiveTraceBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinCpuLoadBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDirectBufferBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinFileDescriptorBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinLoadedClassBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinMemoryBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinResponseTimeBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTotalThreadCountBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTransactionBo;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationActiveTraceChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationCpuLoadChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationDirectBufferChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationFileDescriptorChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationLoadedClassChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationMemoryChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationResponseTimeChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationTotalThreadCountChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationTransactionChart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ApplicationMetricServiceConfiguration {

    @Bean
    public ApplicationStatChartService<ApplicationActiveTraceChart> getApplicationActiveTraceService(ApplicationMetricDao<AggreJoinActiveTraceBo> metricDao) {
        return new DefaultApplicationStatChartService<>(metricDao, ApplicationActiveTraceChart::new);
    }

    @Bean
    public ApplicationStatChartService<ApplicationCpuLoadChart> getApplicationCpuLoadService(ApplicationMetricDao<AggreJoinCpuLoadBo> metricDao) {
        return new DefaultApplicationStatChartService<>(metricDao, ApplicationCpuLoadChart::new);
    }

    @Bean
    public ApplicationStatChartService<ApplicationDirectBufferChart> getApplicationDirectBufferService(ApplicationMetricDao<AggreJoinDirectBufferBo> metricDao) {
        return new DefaultApplicationStatChartService<>(metricDao, ApplicationDirectBufferChart::new);
    }

    @Bean
    public ApplicationStatChartService<ApplicationFileDescriptorChart> getApplicationFileDescriptorService(ApplicationMetricDao<AggreJoinFileDescriptorBo> metricDao) {
        return new DefaultApplicationStatChartService<>(metricDao, ApplicationFileDescriptorChart::new);
    }

    @Bean
    public ApplicationStatChartService<ApplicationMemoryChart> ApplicationMemoryService (ApplicationMetricDao<AggreJoinMemoryBo> metricDao) {
        return new DefaultApplicationStatChartService<>(metricDao, ApplicationMemoryChart::new);
    }

    @Bean
    public ApplicationStatChartService<ApplicationLoadedClassChart> getApplicationLoadedClassService(ApplicationMetricDao<AggreJoinLoadedClassBo> metricDao) {
        return new DefaultApplicationStatChartService<>(metricDao, ApplicationLoadedClassChart::new);
    }


    @Bean
    public ApplicationStatChartService<ApplicationResponseTimeChart> getApplicationResponseTimeService(ApplicationMetricDao<AggreJoinResponseTimeBo> metricDao) {
        return new DefaultApplicationStatChartService<>(metricDao, ApplicationResponseTimeChart::new);
    }

    @Bean
    public ApplicationStatChartService<ApplicationTotalThreadCountChart> getApplicationTotalThreadCountService(ApplicationMetricDao<AggreJoinTotalThreadCountBo> metricDao) {
        return new DefaultApplicationStatChartService<>(metricDao, ApplicationTotalThreadCountChart::new);
    }

    @Bean
    public ApplicationStatChartService<ApplicationTransactionChart> getApplicationTransactionService(ApplicationMetricDao<AggreJoinTransactionBo> metricDao) {
        return new DefaultApplicationStatChartService<>(metricDao, ApplicationTransactionChart::new);
    }
}
