package com.navercorp.pinpoint.pinot.mybatis;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;

public class MyBatisConfiguration {
    public static Configuration defaultConfiguration() {
        Configuration config = new org.apache.ibatis.session.Configuration();
        config.setCacheEnabled(true);
        config.setLazyLoadingEnabled(true);
        config.setAggressiveLazyLoading(true);
        config.setDefaultExecutorType(ExecutorType.SIMPLE);
        return config;
    }
}
