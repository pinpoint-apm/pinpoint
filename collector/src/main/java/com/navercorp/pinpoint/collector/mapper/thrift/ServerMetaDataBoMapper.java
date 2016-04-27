/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.thrift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.navercorp.pinpoint.common.server.bo.ServerMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.ServiceInfoBo;
import com.navercorp.pinpoint.thrift.dto.TServerMetaData;
import com.navercorp.pinpoint.thrift.dto.TServiceInfo;

/**
 * @author hyungil.jeong
 */
@Component
public class ServerMetaDataBoMapper implements ThriftBoMapper<ServerMetaDataBo, TServerMetaData> {

    @Override
    public ServerMetaDataBo map(TServerMetaData thriftObject) {
        final String serverInfo = thriftObject.getServerInfo();
        final List<String> vmArgs = thriftObject.getVmArgs();
        ServerMetaDataBo.Builder builder = new ServerMetaDataBo.Builder();
        builder.serverInfo(serverInfo);
        builder.vmArgs(vmArgs);
        if (thriftObject.isSetServiceInfos()) {
            final List<ServiceInfoBo> serviceInfos = new ArrayList<>(thriftObject.getServiceInfosSize());
            for (TServiceInfo tServiceInfo : thriftObject.getServiceInfos()) {
                final ServiceInfoBo serviceInfoBo = mapServiceInfo(tServiceInfo);
                serviceInfos.add(serviceInfoBo);
            }
            builder.serviceInfos(serviceInfos);
            return builder.build();
        } else {
            builder.serviceInfos(Collections.<ServiceInfoBo> emptyList());
            return builder.build();
        }
    }

    private ServiceInfoBo mapServiceInfo(TServiceInfo serviceInfo) {
        final String serviceName = serviceInfo.getServiceName();
        final List<String> serviceLibs = serviceInfo.getServiceLibs();
        return new ServiceInfoBo.Builder().serviceName(serviceName).serviceLibs(serviceLibs).build();
    }

}
