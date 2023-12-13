package com.navercorp.pinpoint.web.dao.hbase.config;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.web.dao.hbase.stat.HbaseAgentStatDaoOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AgentStatOperationConfiguration {

    @Primary
    @Bean
    public HbaseAgentStatDaoOperations agentStatDaoOperations(HbaseOperations hbaseOperations,
                                                              TableNameProvider tableNameProvider,
                                                              AgentStatHbaseOperationFactory operationFactory) {
        HbaseColumnFamily.AgentStatStatistics stat = HbaseColumnFamily.AGENT_STAT_STATISTICS;
        return new HbaseAgentStatDaoOperations(stat, stat.TIMESPAN_MS, hbaseOperations, tableNameProvider, operationFactory);
    }

    @Bean
    public HbaseAgentStatDaoOperations agentUriDaoOperations(HbaseOperations hbaseOperations,
                                                             TableNameProvider tableNameProvider,
                                                             AgentStatHbaseOperationFactory operationFactory) {
        HbaseColumnFamily.AgentUriStatStatistics uri = HbaseColumnFamily.AGENT_URI_STAT_STATISTICS;
        return new HbaseAgentStatDaoOperations(uri, uri.TIMESPAN_MS, hbaseOperations, tableNameProvider, operationFactory);
    }
}
