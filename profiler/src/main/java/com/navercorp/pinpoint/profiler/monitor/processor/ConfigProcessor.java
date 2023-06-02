package com.navercorp.pinpoint.profiler.monitor.processor;

import java.util.Properties;

/**
 * @author dongdd
 * @description：处理器（反射）
 */
public interface ConfigProcessor {

    boolean isReset(String configStr);

    void resetConfig(String configStr);

    void resetProperties(Properties properties);
}
