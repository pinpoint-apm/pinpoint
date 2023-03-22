package com.navercorp.pinpoint.metric.collector.config;

import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

public interface MyBatisRegistryHandler {
    void registerTypeAlias(TypeAliasRegistry typeAliasRegistry);

    void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry);
}
