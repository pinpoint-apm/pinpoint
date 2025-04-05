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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PinotMappers {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentStatModelConverter agentMapper = new AgentStatModelConverter();
    private final ApplicationStatModelConverter appMapper = new ApplicationStatModelConverter();

    public PinotMappers() {
    }

    public List<PinotTypeMapper<StatDataPoint>> getMapper() {

        List<Method> methods = findDeclaredMethods(this.getClass(), this::isTypeMapper);

        List<PinotTypeMapper<StatDataPoint>> mappers = new ArrayList<>();
        for (Method method : methods) {
            logger.info("Found PinotTypeMapper : {}", method.getName());
            PinotTypeMapper<StatDataPoint> mapper = newMapper(method);
            mappers.add(mapper);
        }
        return mappers;
    }

    private List<Method> findDeclaredMethods(Class<?> clazz, Predicate<Method> filter) {
        final Method[] methods = clazz.getDeclaredMethods();
        final List<Method> list = new ArrayList<>();
        for (Method method : methods) {
            if (filter.test(method)) {
                list.add(method);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private PinotTypeMapper<StatDataPoint> newMapper(Method method) {
        try {
            return (PinotTypeMapper<StatDataPoint>) method.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            ReflectionUtils.handleReflectionException(e);
        }
        throw new IllegalStateException("Failed to invoke method: " + method.getName());
    }

    private boolean isTypeMapper(Method method) {
        return method.getName().startsWith("get") &&
                PinotTypeMapper.class.isAssignableFrom(method.getReturnType());
    }

    public PinotTypeMapper<CpuLoadBo> getCpuLoad() {
        return new PinotTypeMapper<>(AgentStatBo::getCpuLoadBos, agentMapper::convertCpuLoad);
    }

    public PinotTypeMapper<? extends StatDataPoint> getActiveTrace() {
        return new PinotTypeMapper<>(AgentStatBo::getActiveTraceBos, agentMapper::convertActiveTrace);
    }

    public PinotTypeMapper<JvmGcBo> getJvmGc() {
        return new PinotTypeMapper<>(AgentStatBo::getJvmGcBos, agentMapper::convertJvmGc);
    }

    public PinotTypeMapper<JvmGcDetailedBo> getJvmGcDetailed() {
        return new PinotTypeMapper<>(AgentStatBo::getJvmGcDetailedBos, agentMapper::convertJvmGCDetailed);
    }

    public PinotTypeMapper<TransactionBo> getTransaction() {
        return new PinotTypeMapper<>(AgentStatBo::getTransactionBos, agentMapper::convertTransaction);
    }

    public PinotTypeMapper<ResponseTimeBo> getResponseTime() {
        return new PinotTypeMapper<>(AgentStatBo::getResponseTimeBos, agentMapper::convertResponseTime);
    }

    public PinotTypeMapper<DeadlockThreadCountBo> getDeadlockThreadCount() {
        return new PinotTypeMapper<>(AgentStatBo::getDeadlockThreadCountBos, agentMapper::convertDeadlockThreadCount);
    }

    public PinotTypeMapper<FileDescriptorBo> getFileDescriptor() {
        return new PinotTypeMapper<>(AgentStatBo::getFileDescriptorBos, agentMapper::convertFileDescriptor);
    }

    public PinotTypeMapper<DirectBufferBo> getDirectBuffer() {
        return new PinotTypeMapper<>(AgentStatBo::getDirectBufferBos, agentMapper::convertDirectBuffer);
    }

    public PinotTypeMapper<TotalThreadCountBo> getTotalThreadCount() {
        return new PinotTypeMapper<>(AgentStatBo::getTotalThreadCountBos, agentMapper::convertTotalThreadCount);
    }

    public PinotTypeMapper<LoadedClassBo> getLoadedClass() {
        return new PinotTypeMapper<>(AgentStatBo::getLoadedClassBos, agentMapper::convertLoadedClass);
    }

    public PinotTypeMapper<DataSourceListBo> getDataSourceList() {
        return new PinotTypeMapper<>(AgentStatBo::getDataSourceListBos, agentMapper::convertDataSource, appMapper::convertFromDataSource);
    }
}
