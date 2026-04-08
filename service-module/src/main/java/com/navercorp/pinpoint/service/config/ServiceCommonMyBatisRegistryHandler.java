package com.navercorp.pinpoint.service.config;

import com.navercorp.pinpoint.service.dao.dto.ServiceParam;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;

public class ServiceCommonMyBatisRegistryHandler implements ServiceMyBatisRegistryHandler {

    @Override
    public void registerTypeAlias(TypeAliasRegistry typeAliasRegistry) {
        typeAliasRegistry.registerAlias(ServiceEntity.class);
        typeAliasRegistry.registerAlias(ServiceParam.class);
    }

    @Override
    public void registerTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
    }
}
