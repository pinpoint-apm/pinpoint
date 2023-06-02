package com.navercorp.pinpoint.profiler.monitor;

import com.alibaba.nacos.api.config.ConfigService;

/**
 * @author dongdd
 * @description：
 */
public interface RemoteConfigMonitor {
    void start(ConfigService configService);

    void stop();
}
