package com.navercorp.pinpoint.web.dao.hbase.stat;

import com.navercorp.pinpoint.web.dao.SampledAgentStatDao;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

@Configuration
public class SampledAgentStatDaoConfiguration {

    @Bean("sampledJvmGcDaoFactory")
    public SampledAgentStatDao<SampledJvmGc> getSampledJvmGcDao(@Qualifier("sampledJvmGcDaoV2") SampledAgentStatDao<SampledJvmGc> v2) {
        return v2;
    }

    @Bean("sampledJvmGcDetailedDaoFactory")
    public SampledAgentStatDao<SampledJvmGcDetailed> getSampledJvmGcDetailedDao(@Qualifier("sampledJvmGcDetailedDaoV2") SampledAgentStatDao<SampledJvmGcDetailed> v2) {
        return v2;
    }

    @Bean("sampledCpuLoadDaoFactory")
    public SampledAgentStatDao<SampledCpuLoad> getSampledCpuLoadDao(@Qualifier("sampledCpuLoadDaoV2") SampledAgentStatDao<SampledCpuLoad> v2) {
        return v2;
    }

    @Bean("sampledTransactionDaoFactory")
    public SampledAgentStatDao<SampledTransaction> getSampledTransactionDao(@Qualifier("sampledTransactionDaoV2") SampledAgentStatDao<SampledTransaction> v2) {
        return v2;
    }


    @Bean("sampledActiveTraceDaoFactory")
    public SampledAgentStatDao<SampledActiveTrace> getSampledActiveTraceDao(@Qualifier("sampledActiveTraceDaoV2") SampledAgentStatDao<SampledActiveTrace> v2) {
        return v2;
    }


    @Bean("sampledDataSourceDaoFactory")
    public SampledAgentStatDao<SampledDataSourceList> getSampledDataSourceListDao(@Qualifier("sampledDataSourceDaoV2") SampledAgentStatDao<SampledDataSourceList> v2) {
        return v2;
    }

    @Bean("sampledResponseTimeDaoFactory")
    public SampledAgentStatDao<SampledResponseTime> getSampledResponseTimeDao(@Qualifier("sampledResponseTimeDaoV2") SampledAgentStatDao<SampledResponseTime> v2) {
        return v2;
    }

    @Bean("sampledDeadlockDaoFactory")
    public SampledAgentStatDao<SampledDeadlock> getSampledDeadlockDao(@Qualifier("sampledDeadlockDaoV2") SampledAgentStatDao<SampledDeadlock> v2) {
        return v2;
    }

    @Bean("sampledFileDescriptorDaoFactory")
    public SampledAgentStatDao<SampledFileDescriptor> getSampledFileDescriptorDao(@Qualifier("sampledFileDescriptorDaoV2") SampledAgentStatDao<SampledFileDescriptor> v2) {
        return v2;
    }


    @Bean("sampledDirectBufferDaoFactory")
    public SampledAgentStatDao<SampledDirectBuffer> getSampledDirectBufferDao(@Qualifier("sampledDirectBufferDaoV2") SampledAgentStatDao<SampledDirectBuffer> v2) {
        return v2;
    }

    @Bean("sampledTotalThreadCountDaoFactory")
    public SampledAgentStatDao<SampledTotalThreadCount> getSampledTotalThreadCountDao(@Qualifier("sampledTotalThreadCountDaoV2") SampledAgentStatDao<SampledTotalThreadCount> v2) {
        return v2;
    }


    @Bean("sampledLoadedClassCountDaoFactory")
    public SampledAgentStatDao<SampledLoadedClassCount> getSampledLoadedClassCountDao(@Qualifier("sampledLoadedClassDaoV2") SampledAgentStatDao<SampledLoadedClassCount> v2) {
        return v2;
    }

    @Bean("sampledAgentUriStatDaoFactory")
    public SampledAgentStatDao<SampledAgentUriStat> getSampledAgentUriStatDao(@Qualifier("sampledAgentUriStatDaoV2") SampledAgentStatDao<SampledAgentUriStat> v2) {
        return v2;
    }
}