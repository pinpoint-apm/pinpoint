/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.grpc;

import com.navercorp.pinpoint.common.server.bo.ServerMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.ServiceInfoBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.trace.PServerMetaData;
import com.navercorp.pinpoint.grpc.trace.PServiceInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author hyungil.jeong
 */
@Component
public class GrpcServerMetaDataBoMapper {

    public ServerMetaDataBo map(final PServerMetaData serverMetaData) {
        final String serverInfo = serverMetaData.getServerInfo();
        final List<String> vmArgs = serverMetaData.getVmArgList();
        final ServerMetaDataBo.Builder builder = new ServerMetaDataBo.Builder();
        builder.serverInfo(serverInfo);
        builder.vmArgs(vmArgs);

        final List<PServiceInfo> serviceInfoList = serverMetaData.getServiceInfoList();
        if (CollectionUtils.hasLength(serviceInfoList)) {
            final List<ServiceInfoBo> serviceInfoBoList = new ArrayList<>(serverMetaData.getServiceInfoCount());
            for (PServiceInfo tServiceInfo : serviceInfoList) {
                final ServiceInfoBo serviceInfoBo = mapServiceInfo(tServiceInfo);
                serviceInfoBoList.add(serviceInfoBo);
            }
            builder.serviceInfos(serviceInfoBoList);
            return builder.build();
        } else {
            builder.serviceInfos(Collections.emptyList());
            return builder.build();
        }
    }

    private ServiceInfoBo mapServiceInfo(final PServiceInfo serviceInfo) {
        final String serviceName = serviceInfo.getServiceName();
        final List<String> serviceLibs = serviceInfo.getServiceLibList();
        return new ServiceInfoBo.Builder().serviceName(serviceName).serviceLibs(serviceLibs).build();
    }
}