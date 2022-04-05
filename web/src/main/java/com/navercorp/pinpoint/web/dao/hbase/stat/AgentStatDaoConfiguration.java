package com.navercorp.pinpoint.web.dao.hbase.stat;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
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
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentStatDaoConfiguration {

    @Bean("jvmGcDaoFactory")
    public AgentStatDao<JvmGcBo> getJvmGcDao(@Qualifier("jvmGcDaoV2") AgentStatDao<JvmGcBo> v2) {
        return v2;
    }

    @Bean("jvmGcDetailedDaoFactory")
    public AgentStatDao<JvmGcDetailedBo> getJvmGcDetailedDao(@Qualifier("jvmGcDetailedDaoV2") AgentStatDao<JvmGcDetailedBo> v2) {
        return v2;
    }

    @Bean("cpuLoadDaoFactory")
    public AgentStatDao<CpuLoadBo> getCpuLoadDao(@Qualifier("cpuLoadDaoV2") AgentStatDao<CpuLoadBo> v2) {
        return v2;
    }

    @Bean("transactionDaoFactory")
    public AgentStatDao<TransactionBo> getTransactionDao(@Qualifier("transactionDaoV2") AgentStatDao<TransactionBo> v2) {
        return v2;
    }

    @Bean("activeTraceDaoFactory")
    public AgentStatDao<ActiveTraceBo> getActiveTraceDao(@Qualifier("activeTraceDaoV2") AgentStatDao<ActiveTraceBo> v2) {
        return v2;
    }

    @Bean("dataSourceDaoFactory")
    public AgentStatDao<DataSourceListBo> getDataSourceListDao(@Qualifier("dataSourceDaoV2") AgentStatDao<DataSourceListBo> v2) {
        return v2;
    }

    @Bean("responseTimeDaoFactory")
    public AgentStatDao<ResponseTimeBo> getResponseTimeDao(@Qualifier("responseTimeDaoV2") AgentStatDao<ResponseTimeBo> v2) {
        return v2;
    }

    @Bean("deadlockDaoFactory")
    public AgentStatDao<DeadlockThreadCountBo> getDeadlockThreadCountDao(@Qualifier("deadlockDaoV2") AgentStatDao<DeadlockThreadCountBo> v2) {
        return v2;
    }

    @Bean("fileDescriptorDaoFactory")
    public AgentStatDao<FileDescriptorBo> getFileDescriptorDao(@Qualifier("fileDescriptorDaoV2") AgentStatDao<FileDescriptorBo> v2) {
        return v2;
    }

    @Bean("directBufferDaoFactory")
    public AgentStatDao<DirectBufferBo> getDirectBufferDao(@Qualifier("directBufferDaoV2") AgentStatDao<DirectBufferBo> v2) {
        return v2;
    }

    @Bean("totalThreadCountDaoFactory")
    public AgentStatDao<TotalThreadCountBo> getTotalThreadCountDao(@Qualifier("totalThreadCountDaoV2") AgentStatDao<TotalThreadCountBo> v2) {
        return v2;
    }

    @Bean("loadedClassCountDaoFactory")
    public AgentStatDao<LoadedClassBo> getLoadedClassDao(@Qualifier("loadedClassDaoV2") AgentStatDao<LoadedClassBo> v2) {
        return v2;
    }

    @Bean("agentUriStatDao")
    public AgentStatDao<AgentUriStatBo> getAgentUriStatDao(@Qualifier("agentUriStatDaoV2") AgentStatDao<AgentUriStatBo> v2) {
        return v2;
    }

}
