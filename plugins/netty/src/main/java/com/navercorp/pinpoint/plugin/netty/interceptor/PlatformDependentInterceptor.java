package com.navercorp.pinpoint.plugin.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.CustomMetricRegistry;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongCounter;
import io.netty.util.internal.PlatformDependent;

public class PlatformDependentInterceptor implements AroundInterceptor {

    private CustomMetricRegistry customMetricMonitorRegistry;

    public PlatformDependentInterceptor(CustomMetricRegistry customMetricMonitorRegistry) {
        this.customMetricMonitorRegistry = customMetricMonitorRegistry;

        LongCounter usedDirectMemoryCounter = makeUsedDirectMemoryCounter();
        LongCounter maxDirectMemoryCounter = makeMaxDirectMemory();

        this.customMetricMonitorRegistry.register(usedDirectMemoryCounter);
        this.customMetricMonitorRegistry.register(maxDirectMemoryCounter);
    }

    private LongCounter makeUsedDirectMemoryCounter() {
        return new LongCounter() {
            @Override
            public String getName() {
                return "custom/netty/usedDirectMemory";
            }

            @Override
            public long getValue() {
                return PlatformDependent.usedDirectMemory();
            }
        };
    }

    private LongCounter makeMaxDirectMemory() {
        return new LongCounter() {
            @Override
            public String getName() {
                return "custom/netty/maxDirectMemory";
            }

            @Override
            public long getValue() {
                return PlatformDependent.maxDirectMemory();
            }
        };
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
