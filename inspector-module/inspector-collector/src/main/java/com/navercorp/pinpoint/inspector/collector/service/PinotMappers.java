package com.navercorp.pinpoint.inspector.collector.service;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.StatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.inspector.collector.dao.pinot.PinotTypeMapper;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStatModelConverter;
import com.navercorp.pinpoint.inspector.collector.model.kafka.ApplicationStatModelConverter;

import java.util.List;

public class PinotMappers {

    private final AgentStatModelConverter agentMapper = new AgentStatModelConverter();
    private final ApplicationStatModelConverter appMapper = new ApplicationStatModelConverter();

    public PinotMappers() {
    }

    public List<PinotTypeMapper<StatDataPoint>> getMapper() {
        List<PinotTypeMapper<? extends StatDataPoint>> mappers = List.of(
                this.getPinotCpuLoadDao(),
                this.getPinotActiveTraceDao(),
                this.getPinotJvmGcDao(),
                this.getPinotJvmGcDetailedDao(),
                this.getPinotTransactionDao(),
                this.getPinotResponseTimeDao(),

                this.getPinotDeadlockThreadCountDao(),
                this.getPinotFileDescriptorDao(),
                this.getPinotDirectBufferDao(),

                this.getPinotTotalThreadCountDao(),
                this.getPinotLoadedClassDao(),
                this.getPinotDataSourceListDao()
        );
        return (List<PinotTypeMapper<StatDataPoint>>) (List<?>) mappers;
    }

    public PinotTypeMapper<CpuLoadBo> getPinotCpuLoadDao() {
        return new PinotTypeMapper<>(AgentStatBo::getCpuLoadBos, agentMapper::convertCpuLoad);
    }


    public PinotTypeMapper<? extends StatDataPoint> getPinotActiveTraceDao() {
        return new PinotTypeMapper<>(AgentStatBo::getActiveTraceBos, agentMapper::convertActiveTrace);
    }

    public PinotTypeMapper<JvmGcBo> getPinotJvmGcDao() {
        return new PinotTypeMapper<>(AgentStatBo::getJvmGcBos, agentMapper::convertJvmGc);
    }

    public PinotTypeMapper<JvmGcDetailedBo> getPinotJvmGcDetailedDao() {
        return new PinotTypeMapper<>(AgentStatBo::getJvmGcDetailedBos, agentMapper::convertJvmGCDetailed);
    }

    public PinotTypeMapper<TransactionBo> getPinotTransactionDao() {
        return new PinotTypeMapper<>(AgentStatBo::getTransactionBos, agentMapper::convertTransaction);
    }

    public PinotTypeMapper<ResponseTimeBo> getPinotResponseTimeDao() {
        return new PinotTypeMapper<>(AgentStatBo::getResponseTimeBos, agentMapper::convertResponseTime);
    }

    public PinotTypeMapper<DeadlockThreadCountBo> getPinotDeadlockThreadCountDao() {
        return new PinotTypeMapper<>(AgentStatBo::getDeadlockThreadCountBos, agentMapper::convertDeadlockThreadCount);
    }

    public PinotTypeMapper<FileDescriptorBo> getPinotFileDescriptorDao() {
        return new PinotTypeMapper<>(AgentStatBo::getFileDescriptorBos, agentMapper::convertFileDescriptor);
    }

    public PinotTypeMapper<DirectBufferBo> getPinotDirectBufferDao() {
        return new PinotTypeMapper<>(AgentStatBo::getDirectBufferBos, agentMapper::convertDirectBuffer);
    }

    public PinotTypeMapper<TotalThreadCountBo> getPinotTotalThreadCountDao() {
        return new PinotTypeMapper<>(AgentStatBo::getTotalThreadCountBos, agentMapper::convertTotalThreadCount);
    }

    public PinotTypeMapper<LoadedClassBo> getPinotLoadedClassDao() {
        return new PinotTypeMapper<>(AgentStatBo::getLoadedClassBos, agentMapper::convertLoadedClass);
    }

    public PinotTypeMapper<DataSourceListBo> getPinotDataSourceListDao() {
        return new PinotTypeMapper<>(AgentStatBo::getDataSourceListBos, agentMapper::convertDataSource, appMapper::convertFromDataSource);
    }
}
