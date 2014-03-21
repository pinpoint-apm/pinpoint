package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.nhn.pinpoint.profiler.monitor.MonitorName;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.thrift.dto.TJvmGc;

import java.util.Collection;

import static com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

/**
 * @author harebox
 */
public class GarbageCollectorFactory {
    private final MetricMonitorRegistry monitorRegistry;
    /**
     * Metrics 통계 데이터를 이용하여 가비지 컬렉터 타입을 지정한다.
     */
    public GarbageCollectorFactory() {
        this.monitorRegistry = createRegistry();
    }

    private MetricMonitorRegistry createRegistry() {
        final MetricMonitorRegistry monitorRegistry = new MetricMonitorRegistry();

        // FIXME 설정에 따라 어떤 데이터를 수집할 지 선택할 수 있도록 해야한다. 여기서는 JVM 메모리 정보를 default로 수집.
        monitorRegistry.registerJvmMemoryMonitor(new MonitorName(MetricMonitorValues.JVM_MEMORY));
        monitorRegistry.registerJvmGcMonitor(new MonitorName(MetricMonitorValues.JVM_GC));
        return monitorRegistry;
    }
    /**
     * 통계 키를 기반으로 생성
     */
    public GarbageCollector createGarbageCollector() {
        MetricMonitorRegistry registry = this.monitorRegistry;
        Collection<String> keys = registry.getRegistry().getNames();
        if (keys.contains(JVM_GC_SERIAL_MSC_COUNT)) {
            return new SerialCollector(registry);
        } else if (keys.contains(JVM_GC_PS_MS_COUNT)) {
            return new ParallelCollector(registry);
        } else if (keys.contains(JVM_GC_CMS_COUNT)) {
            return new CmsCollector(registry);
        } else if (keys.contains(JVM_GC_G1_OLD_COUNT)) {
            return new G1Collector(registry);
        } else {
            // error말고 unknownCollector를 던지는 걸로 수정해야함..
            throw new RuntimeException("unknown garbage collector");
        }
    }

}
