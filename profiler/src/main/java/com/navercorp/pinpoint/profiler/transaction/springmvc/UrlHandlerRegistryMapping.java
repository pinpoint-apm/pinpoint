package com.navercorp.pinpoint.profiler.transaction.springmvc;

import com.navercorp.pinpoint.profiler.transaction.DefaultRegistryMapping;
import com.navercorp.pinpoint.bootstrap.context.transaction.IMappingRegistry;
import com.navercorp.pinpoint.bootstrap.context.transaction.IRequestMappingInfo;

import java.util.Collection;

public class UrlHandlerRegistryMapping extends DefaultRegistryMapping implements IMappingRegistry {
    public final static int URL_HANDLER_LEVEL = 2;

    @Override
    public void register(IRequestMappingInfo requestMappingInfo, int level) {
        if(level == URL_HANDLER_LEVEL){
            super.register(requestMappingInfo);
        }
    }

    @Override
    public void register(Collection<IRequestMappingInfo> requestMappingInfos, int level) {
        if (level == URL_HANDLER_LEVEL){
            super.register(requestMappingInfos);
        }
    }
}
