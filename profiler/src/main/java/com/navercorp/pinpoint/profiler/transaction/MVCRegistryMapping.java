package com.navercorp.pinpoint.profiler.transaction;

import com.navercorp.pinpoint.profiler.transaction.springmvc.SpringMVCMappingRegistryFactory;
import com.navercorp.pinpoint.bootstrap.context.transaction.IMappingRegistry;
import com.navercorp.pinpoint.bootstrap.context.transaction.IRequestMappingInfo;

import java.util.Collection;
import java.util.List;

public class MVCRegistryMapping extends DefaultRegistryMapping implements IMappingRegistry {

    private List<IMappingRegistry> registries;

    public MVCRegistryMapping() {
        this.registries = SpringMVCMappingRegistryFactory.createMappingRegistries();
    }


    @Override
    public IRequestMappingInfo match(String uri, String method) {
        if (registries != null) {

            for (IMappingRegistry registry : registries) {
                IRequestMappingInfo requestMappingInfo = registry.match(uri, method);
                if (requestMappingInfo != null) {
                    return requestMappingInfo;
                }
            }
        }

        return null;
    }

    @Override
    public void register(IRequestMappingInfo requestMappingInfo, int level) {
        if (level <= 0) {
            return;
        }

        if (this.registries.size() >= level) {
            registries.get(level - 1).register(requestMappingInfo, level);
        }

    }

    @Override
    public void register(Collection<IRequestMappingInfo> requestMappingInfos, int level) {
        if (level <= 0) {
            return;
        }

        if (this.registries.size() >= level) {
            registries.get(level - 1).register(requestMappingInfos, level);
        }
    }
}
