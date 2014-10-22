package com.nhn.pinpoint.collector.mapper.thrift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nhn.pinpoint.common.bo.ServerMetaDataBo;
import com.nhn.pinpoint.common.bo.ServiceInfoBo;
import com.nhn.pinpoint.thrift.dto.TServerMetaData;
import com.nhn.pinpoint.thrift.dto.TServiceInfo;

/**
 * @author hyungil.jeong
 */
@Component
public class ServerMetaDataBoMapper implements ThriftBoMapper<ServerMetaDataBo, TServerMetaData> {

    @Override
    public ServerMetaDataBo map(TServerMetaData thriftObject) {
        final String serverInfo = thriftObject.getServerInfo();
        final List<String> vmArgs = thriftObject.getVmArgs();
        ServerMetaDataBo.Builder builder = new ServerMetaDataBo.Builder().serverInfo(serverInfo).vmArgs(vmArgs);
        if (thriftObject.isSetServiceInfos()) {
            final List<ServiceInfoBo> serviceInfos = new ArrayList<ServiceInfoBo>(thriftObject.getServiceInfosSize());
            for (TServiceInfo tServiceInfo : thriftObject.getServiceInfos()) {
                final ServiceInfoBo serviceInfoBo = mapServiceInfo(tServiceInfo);
                serviceInfos.add(serviceInfoBo);
            }
            return builder.serviceInfos(serviceInfos).build();
        } else {
            return builder.serviceInfos(Collections.<ServiceInfoBo> emptyList()).build();
        }
    }

    private ServiceInfoBo mapServiceInfo(TServiceInfo serviceInfo) {
        final String serviceName = serviceInfo.getServiceName();
        final List<String> serviceLibs = serviceInfo.getServiceLibs();
        return new ServiceInfoBo.Builder().serviceName(serviceName).serviceLibs(serviceLibs).build();
    }

}
