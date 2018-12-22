package com.navercorp.pinpoint.bootstrap.context.transaction;

import java.util.Collection;

public interface IMappingRegistry {

    IRequestMappingInfo match(String uri, String method);

    void register(IRequestMappingInfo requestMappingInfo, int level);

    void register(Collection<IRequestMappingInfo> requestMappingInfos, int level);
}
