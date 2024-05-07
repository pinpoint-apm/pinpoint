package com.navercorp.pinpoint.web.applicationmap.config;


import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.LinkFilter;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.MapStatisticsCalleeMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.MapStatisticsCallerMapper;
import com.navercorp.pinpoint.web.applicationmap.dao.mapper.ResponseTimeMapper;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.util.TimeWindowFunction;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapMapperConfiguration {

    @Bean
    public RowMapper<LinkDataMap> mapStatisticsCallerMapper(ApplicationFactory applicationFactory,
                                                            @Qualifier("statisticsCallerRowKeyDistributor")
                                                            RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return new MapStatisticsCallerMapper(applicationFactory, rowKeyDistributor, LinkFilter::skip, TimeWindowFunction.identity());
    }

    @Bean
    public RowMapper<LinkDataMap> mapStatisticsCallerTimeAggregatedMapper(ApplicationFactory applicationFactory,
                                                                          @Qualifier("statisticsCallerRowKeyDistributor")
                                                                          RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return new MapStatisticsCallerMapper(applicationFactory, rowKeyDistributor, LinkFilter::skip, TimeWindowFunction.ALL_IN_ONE);
    }

    @Bean
    public RowMapper<LinkDataMap> mapStatisticsCalleeMapper(ServiceTypeRegistryService registry,
                                                            ApplicationFactory applicationFactory,
                                                            @Qualifier("statisticsCalleeRowKeyDistributor")
                                                            RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return new MapStatisticsCalleeMapper(registry, applicationFactory, rowKeyDistributor, LinkFilter::skip, TimeWindowFunction.identity());
    }

    @Bean
    public RowMapper<LinkDataMap> mapStatisticsCalleeTimeAggregatedMapper(ServiceTypeRegistryService registry,
                                                                          ApplicationFactory applicationFactory,
                                                                          @Qualifier("statisticsCalleeRowKeyDistributor")
                                                                          RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return new MapStatisticsCalleeMapper(registry, applicationFactory, rowKeyDistributor, LinkFilter::skip, TimeWindowFunction.ALL_IN_ONE);
    }

    @Bean
    public RowMapper<ResponseTime> responseTimeMapper(ServiceTypeRegistryService registry,
                                                      @Qualifier("statisticsSelfRowKeyDistributor")
                                                      RowKeyDistributorByHashPrefix rowKeyDistributor) {
        return new ResponseTimeMapper(registry, rowKeyDistributor);
    }
}
