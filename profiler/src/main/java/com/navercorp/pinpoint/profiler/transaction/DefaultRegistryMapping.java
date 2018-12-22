package com.navercorp.pinpoint.profiler.transaction;

import com.navercorp.pinpoint.bootstrap.context.transaction.IRequestMappingInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultRegistryMapping {

    private List<IRequestMappingInfo> requestMappingInfos = new ArrayList<IRequestMappingInfo>();


    protected void register(IRequestMappingInfo requestMappingInfo) {
        requestMappingInfos.add(requestMappingInfo);
    }

    protected void register(Collection<IRequestMappingInfo> requestMappingInfos){
        this.requestMappingInfos.addAll(requestMappingInfos);
    }

    public IRequestMappingInfo match(String uri, String method) {
        return this.match(requestMappingInfos, uri, method);
    }

    protected IRequestMappingInfo match(List<IRequestMappingInfo> requestMappingInfos, String uri, String method){

        if(requestMappingInfos == null){
            return null;
        }

        for (IRequestMappingInfo requestMappingInfo : requestMappingInfos) {
            if (requestMappingInfo.match(uri, method)) {
                return requestMappingInfo;
            }
        }

        return null;
    }


}
