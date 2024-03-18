package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PDataSource;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogramUtils;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AgentStatMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AgentStatMapperImpl;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.CustomMetricMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.CustomMetricMapperImpl;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.JvmGcTypeMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.JvmGcTypeMapperImpl;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.UriStatMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.UriStatMapperImpl;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.JvmGcDetailedMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.JvmGcMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSource;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.filedescriptor.FileDescriptorMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.JvmGcType;
import com.navercorp.pinpoint.profiler.monitor.metric.loadedclass.LoadedClassMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeValue;
import com.navercorp.pinpoint.profiler.monitor.metric.totalthread.TotalThreadMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetricSnapshot;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Random;

import static com.navercorp.pinpoint.profiler.context.grpc.MapperTestUtil.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author intr3p1d
 */
class GrpcStatMessageConverterTest {

    private static final Random random = new Random();

    private static AgentStatMetricSnapshot newAgentStatMetricSnapshot() {
        AgentStatMetricSnapshot agentStatMetricSnapshot = mock(AgentStatMetricSnapshot.class);

        when(agentStatMetricSnapshot.getAgentId()).thenReturn("agent");
        when(agentStatMetricSnapshot.getStartTimestamp()).thenReturn(random.nextLong());
        when(agentStatMetricSnapshot.getTimestamp()).thenReturn(random.nextLong());
        when(agentStatMetricSnapshot.getCollectInterval()).thenReturn(random.nextLong());

        JvmGcMetricSnapshot jvmGcMetricSnapshot = newJvmGcMetricSnapshot();
        when(agentStatMetricSnapshot.getGc()).thenReturn(jvmGcMetricSnapshot);

        CpuLoadMetricSnapshot cpuLoadMetricSnapshot = newCpuLoadMetricSnapshot();
        when(agentStatMetricSnapshot.getCpuLoad()).thenReturn(cpuLoadMetricSnapshot);

        TransactionMetricSnapshot transactionMetricSnapshot = newTransactionMetricSnapshot();
        when(agentStatMetricSnapshot.getTransaction()).thenReturn(transactionMetricSnapshot);

        ActiveTraceHistogram activeTraceHistogram = newActiveTraceHistogram();
        when(agentStatMetricSnapshot.getActiveTrace()).thenReturn(activeTraceHistogram);

        DataSourceMetricSnapshot dataSourceMetricSnapshot = newDataSourceMetricSnapshot();
        when(agentStatMetricSnapshot.getDataSourceList()).thenReturn(dataSourceMetricSnapshot);

        ResponseTimeValue responseTimeValue = newResponseTimeValue();
        when(agentStatMetricSnapshot.getResponseTime()).thenReturn(responseTimeValue);

        DeadlockMetricSnapshot deadlockMetricSnapshot = newDeadlockMetricSnapshot();
        when(agentStatMetricSnapshot.getDeadlock()).thenReturn(deadlockMetricSnapshot);

        FileDescriptorMetricSnapshot fileDescriptorMetricSnapshot = newFileDescriptorMetricSnapshot();
        when(agentStatMetricSnapshot.getFileDescriptor()).thenReturn(fileDescriptorMetricSnapshot);

        BufferMetricSnapshot bufferMetricSnapshot = newBufferMetricSnapshot();
        when(agentStatMetricSnapshot.getDirectBuffer()).thenReturn(bufferMetricSnapshot);

        TotalThreadMetricSnapshot totalThreadMetricSnapshot = newTotalThreadMetricSnapshot();
        when(agentStatMetricSnapshot.getTotalThread()).thenReturn(totalThreadMetricSnapshot);

        LoadedClassMetricSnapshot loadedClassMetricSnapshot = newLoadedClassMetricSnapshot();
        when(agentStatMetricSnapshot.getLoadedClassCount()).thenReturn(loadedClassMetricSnapshot);
        return agentStatMetricSnapshot;
    }

    private static JvmGcMetricSnapshot newJvmGcMetricSnapshot() {
        JvmGcMetricSnapshot jvmGcMetricSnapshot = mock(JvmGcMetricSnapshot.class);
        when(jvmGcMetricSnapshot.getType()).thenReturn(JvmGcType.values()[random.nextInt(JvmGcType.values().length)]);
        when(jvmGcMetricSnapshot.getJvmMemoryHeapUsed()).thenReturn(random.nextLong());
        when(jvmGcMetricSnapshot.getJvmMemoryHeapMax()).thenReturn(random.nextLong());
        when(jvmGcMetricSnapshot.getJvmMemoryNonHeapUsed()).thenReturn(random.nextLong());
        when(jvmGcMetricSnapshot.getJvmMemoryNonHeapMax()).thenReturn(random.nextLong());
        when(jvmGcMetricSnapshot.getJvmGcOldCount()).thenReturn(random.nextLong());
        when(jvmGcMetricSnapshot.getJvmGcOldTime()).thenReturn(random.nextLong());

        JvmGcDetailedMetricSnapshot jvmGcDetailedMetricSnapshot = newJvmGcDetailedMetricSnapshot();
        when(jvmGcMetricSnapshot.getJvmGcDetailed()).thenReturn(jvmGcDetailedMetricSnapshot);
        return jvmGcMetricSnapshot;
    }

    private static JvmGcDetailedMetricSnapshot newJvmGcDetailedMetricSnapshot() {
        JvmGcDetailedMetricSnapshot jvmGcDetailedMetricSnapshot = mock(JvmGcDetailedMetricSnapshot.class);
        when(jvmGcDetailedMetricSnapshot.getJvmGcNewCount()).thenReturn(random.nextLong());
        when(jvmGcDetailedMetricSnapshot.getJvmGcNewTime()).thenReturn(random.nextLong());
        when(jvmGcDetailedMetricSnapshot.getJvmPoolCodeCacheUsed()).thenReturn(random.nextDouble());
        when(jvmGcDetailedMetricSnapshot.getJvmPoolNewGenUsed()).thenReturn(random.nextDouble());
        when(jvmGcDetailedMetricSnapshot.getJvmPoolOldGenUsed()).thenReturn(random.nextDouble());
        when(jvmGcDetailedMetricSnapshot.getJvmPoolSurvivorSpaceUsed()).thenReturn(random.nextDouble());
        when(jvmGcDetailedMetricSnapshot.getJvmPoolPermGenUsed()).thenReturn(random.nextDouble());
        when(jvmGcDetailedMetricSnapshot.getJvmPoolMetaspaceUsed()).thenReturn(random.nextDouble());
        return jvmGcDetailedMetricSnapshot;
    }

    private static CpuLoadMetricSnapshot newCpuLoadMetricSnapshot() {
        return new CpuLoadMetricSnapshot(
                random.nextDouble(), random.nextDouble()
        );
    }

    private static TransactionMetricSnapshot newTransactionMetricSnapshot() {
        return new TransactionMetricSnapshot(
                random.nextLong(), random.nextLong(), random.nextLong(),
                random.nextLong(), random.nextLong(), random.nextLong()
        );
    }

    private static ActiveTraceHistogram newActiveTraceHistogram() {
        ActiveTraceHistogram activeTraceHistogram = mock(ActiveTraceHistogram.class);

        HistogramSchema histogramSchema = mock(HistogramSchema.class);
        when(histogramSchema.getTypeCode()).thenReturn(random.nextInt());

        when(activeTraceHistogram.getHistogramSchema()).thenReturn(histogramSchema);
        when(activeTraceHistogram.getFastCount()).thenReturn(random.nextInt());
        when(activeTraceHistogram.getNormalCount()).thenReturn(random.nextInt());
        when(activeTraceHistogram.getSlowCount()).thenReturn(random.nextInt());
        when(activeTraceHistogram.getVerySlowCount()).thenReturn(random.nextInt());
        return activeTraceHistogram;
    }

    private static DataSourceMetricSnapshot newDataSourceMetricSnapshot() {
        DataSourceMetricSnapshot dataSourceMetricSnapshot = new DataSourceMetricSnapshot();
        for (int i = 0; i < 10; i++) {
            DataSource dataSource = mock(DataSource.class);
            when(dataSource.getId()).thenReturn(random.nextInt());
            when(dataSource.getServiceTypeCode()).thenReturn((short) random.nextInt());
            when(dataSource.getDatabaseName()).thenReturn(randomString());
            when(dataSource.getUrl()).thenReturn(randomString());
            when(dataSource.getActiveConnectionSize()).thenReturn(random.nextInt());
            when(dataSource.getMaxConnectionSize()).thenReturn(random.nextInt());

            dataSourceMetricSnapshot.addDataSourceCollectData(dataSource);
        }
        return dataSourceMetricSnapshot;
    }

    private static ResponseTimeValue newResponseTimeValue() {
        ResponseTimeValue responseTimeValue = mock(ResponseTimeValue.class);
        when(responseTimeValue.getAvg()).thenReturn(random.nextLong());
        when(responseTimeValue.getMax()).thenReturn(random.nextLong());
        when(responseTimeValue.getTotal()).thenReturn(random.nextLong());
        when(responseTimeValue.getTransactionCount()).thenReturn(random.nextLong());
        return responseTimeValue;
    }

    private static DeadlockMetricSnapshot newDeadlockMetricSnapshot() {
        DeadlockMetricSnapshot deadlockMetricSnapshot = mock(DeadlockMetricSnapshot.class);
        // pass ThreadDumpMetricSnapshot
        when(deadlockMetricSnapshot.getDeadlockedThreadList()).thenReturn(Collections.emptyList());
        when(deadlockMetricSnapshot.getDeadlockedThreadCount()).thenReturn(random.nextInt());
        return deadlockMetricSnapshot;
    }

    private static FileDescriptorMetricSnapshot newFileDescriptorMetricSnapshot() {
        return new FileDescriptorMetricSnapshot(random.nextLong());
    }

    private static BufferMetricSnapshot newBufferMetricSnapshot() {
        return new BufferMetricSnapshot(
                random.nextLong(), random.nextLong(), random.nextLong(), random.nextLong()
        );
    }

    private static TotalThreadMetricSnapshot newTotalThreadMetricSnapshot() {
        return new TotalThreadMetricSnapshot(random.nextInt());
    }

    private static LoadedClassMetricSnapshot newLoadedClassMetricSnapshot() {
        return new LoadedClassMetricSnapshot(
                random.nextInt(), random.nextLong()
        );
    }

    private final JvmGcTypeMapper jvmGcTypeMapper = new JvmGcTypeMapperImpl();
    private final AgentStatMapper agentStatMapper = new AgentStatMapperImpl(jvmGcTypeMapper);
    private final CustomMetricMapper customMetricMapper = new CustomMetricMapperImpl();
    private final UriStatMapper uriStatMapper = new UriStatMapperImpl();

    private final GrpcStatMessageConverter converter = new GrpcStatMessageConverter(
            agentStatMapper, customMetricMapper, uriStatMapper
    );

    @Test
    void testAgentStat() {
        AgentStatMetricSnapshot agentStat = newAgentStatMetricSnapshot();
        PAgentStat pAgentStat = (PAgentStat) converter.toMessage(agentStat);

        // Check assertEquals against the class instance mocked above
        assertEquals(agentStat.getTimestamp(), pAgentStat.getTimestamp());
        assertEquals(agentStat.getCollectInterval(), pAgentStat.getCollectInterval());

        // check JvmGcMetricSnapshot
        assertEquals("JVM_GC_TYPE_" + agentStat.getGc().getType().name(), pAgentStat.getGc().getType().name());
        assertEquals(agentStat.getGc().getJvmMemoryHeapMax(), pAgentStat.getGc().getJvmMemoryHeapMax());
        assertEquals(agentStat.getGc().getJvmMemoryHeapUsed(), pAgentStat.getGc().getJvmMemoryHeapUsed());
        assertEquals(agentStat.getGc().getJvmMemoryNonHeapMax(), pAgentStat.getGc().getJvmMemoryNonHeapMax());
        assertEquals(agentStat.getGc().getJvmMemoryNonHeapUsed(), pAgentStat.getGc().getJvmMemoryNonHeapUsed());
        assertEquals(agentStat.getGc().getJvmGcOldCount(), pAgentStat.getGc().getJvmGcOldCount());
        assertEquals(agentStat.getGc().getJvmGcOldTime(), pAgentStat.getGc().getJvmGcOldTime());

        // check JvmGcDetailedMetricSnapshot
        assertEquals(agentStat.getGc().getJvmGcDetailed().getJvmPoolNewGenUsed(), pAgentStat.getGc().getJvmGcDetailed().getJvmPoolNewGenUsed());
        assertEquals(agentStat.getGc().getJvmGcDetailed().getJvmPoolOldGenUsed(), pAgentStat.getGc().getJvmGcDetailed().getJvmPoolOldGenUsed());
        assertEquals(agentStat.getGc().getJvmGcDetailed().getJvmPoolSurvivorSpaceUsed(), pAgentStat.getGc().getJvmGcDetailed().getJvmPoolSurvivorSpaceUsed());
        assertEquals(agentStat.getGc().getJvmGcDetailed().getJvmPoolCodeCacheUsed(), pAgentStat.getGc().getJvmGcDetailed().getJvmPoolCodeCacheUsed());
        assertEquals(agentStat.getGc().getJvmGcDetailed().getJvmPoolPermGenUsed(), pAgentStat.getGc().getJvmGcDetailed().getJvmPoolPermGenUsed());
        assertEquals(agentStat.getGc().getJvmGcDetailed().getJvmPoolMetaspaceUsed(), pAgentStat.getGc().getJvmGcDetailed().getJvmPoolMetaspaceUsed());
        assertEquals(agentStat.getGc().getJvmGcDetailed().getJvmGcNewCount(), pAgentStat.getGc().getJvmGcDetailed().getJvmGcNewCount());
        assertEquals(agentStat.getGc().getJvmGcDetailed().getJvmGcNewTime(), pAgentStat.getGc().getJvmGcDetailed().getJvmGcNewTime());

        // check CpuLoadMetricSnapshot
        assertEquals(agentStat.getCpuLoad().getJvmCpuUsage(), pAgentStat.getCpuLoad().getJvmCpuLoad());
        assertEquals(agentStat.getCpuLoad().getSystemCpuUsage(), pAgentStat.getCpuLoad().getSystemCpuLoad());

        // check TransactionMetricSnapshot
        assertEquals(agentStat.getTransaction().getSampledNewCount(), pAgentStat.getTransaction().getSampledNewCount());
        assertEquals(agentStat.getTransaction().getSampledContinuationCount(), pAgentStat.getTransaction().getSampledContinuationCount());
        assertEquals(agentStat.getTransaction().getUnsampledNewCount(), pAgentStat.getTransaction().getUnsampledNewCount());
        assertEquals(agentStat.getTransaction().getUnsampledContinuationCount(), pAgentStat.getTransaction().getUnsampledContinuationCount());
        assertEquals(agentStat.getTransaction().getSkippedNewCount(), pAgentStat.getTransaction().getSkippedNewCount());
        assertEquals(agentStat.getTransaction().getSkippedContinuationCount(), pAgentStat.getTransaction().getSkippedContinuationCount());

        // check ActiveTraceHistogram
        assertEquals(agentStat.getActiveTrace().getHistogramSchema().getTypeCode(), pAgentStat.getActiveTrace().getHistogram().getHistogramSchemaType());
        assertEquals(
                ActiveTraceHistogramUtils.asList(agentStat.getActiveTrace()),
                pAgentStat.getActiveTrace().getHistogram().getActiveTraceCountList()
        );

        // check DataSourceMetricSnapshot
        assertEquals(agentStat.getDataSourceList().getDataSourceList().size(), pAgentStat.getDataSourceList().getDataSourceList().size());
        for (int i=0;i<agentStat.getDataSourceList().getDataSourceList().size();i++) {
            DataSource dataSource = agentStat.getDataSourceList().getDataSourceList().get(i);
            PDataSource pDataSource = pAgentStat.getDataSourceList().getDataSource(i);
            assertEquals(dataSource.getId(), pDataSource.getId());
            assertEquals(dataSource.getServiceTypeCode(), pDataSource.getServiceTypeCode());
            assertEquals(dataSource.getDatabaseName(), pDataSource.getDatabaseName());
            assertEquals(dataSource.getActiveConnectionSize(), pDataSource.getActiveConnectionSize());
            assertEquals(dataSource.getUrl(), pDataSource.getUrl());
            assertEquals(dataSource.getMaxConnectionSize(), pDataSource.getMaxConnectionSize());
        }

        // check ResponseTimeValue
        assertEquals(agentStat.getResponseTime().getAvg(), pAgentStat.getResponseTime().getAvg());
        assertEquals(agentStat.getResponseTime().getMax(), pAgentStat.getResponseTime().getMax());

        // check DeadlockMetricSnapshot
        assertEquals(agentStat.getDeadlock().getDeadlockedThreadCount(), pAgentStat.getDeadlock().getCount());

        // check FileDescriptorMetricSnapshot
        assertEquals(agentStat.getFileDescriptor().getOpenFileDescriptorCount(), pAgentStat.getFileDescriptor().getOpenFileDescriptorCount());

        // check BufferMetricSnapshot
        assertEquals(agentStat.getDirectBuffer().getDirectCount(), pAgentStat.getDirectBuffer().getDirectCount());
        assertEquals(agentStat.getDirectBuffer().getDirectMemoryUsed(), pAgentStat.getDirectBuffer().getDirectMemoryUsed());
        assertEquals(agentStat.getDirectBuffer().getMappedCount(), pAgentStat.getDirectBuffer().getMappedCount());
        assertEquals(agentStat.getDirectBuffer().getMappedMemoryUsed(), pAgentStat.getDirectBuffer().getMappedMemoryUsed());

        // check TotalThreadMetricSnapshot
        assertEquals(agentStat.getTotalThread().getTotalThreadCount(), pAgentStat.getTotalThread().getTotalThreadCount());

        // check LoadedClassMetricSnapshot
        assertEquals(agentStat.getLoadedClassCount().getLoadedClassCount(), pAgentStat.getLoadedClass().getLoadedClassCount());
        assertEquals(agentStat.getLoadedClassCount().getUnloadedClassCount(), pAgentStat.getLoadedClass().getUnloadedClassCount());

    }


}