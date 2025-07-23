/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.agentstatistics.collector.mapper;

import com.navercorp.pinpoint.agentstatistics.collector.entity.AgentInfoEntity;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.ServiceInfoBo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author intr3p1d
 */
@Mapper(componentModel = "spring")
public interface AgentInfoMapper {

    @Mapping(target="serverInfo", source = "serverMetaData.serverInfo")
    @Mapping(target = "vmArgs", source = "serverMetaData.vmArgs")
    @Mapping(target = "serviceInfos", source = "serverMetaData.serviceInfos", qualifiedByName = "serverMetaDataToServiceInfos")
    @Mapping(target = "version", source = "jvmInfo.version")
    @Mapping(target = "jvmVersion", source = "jvmInfo.jvmVersion")
    @Mapping(target = "gcTypeName", source = "jvmInfo.gcTypeName")
    AgentInfoEntity toEntity(AgentInfoBo agentInfoBo);


    @Named("serverMetaDataToServiceInfos")
    default List<String> serverMetaDataToServiceInfos(List<ServiceInfoBo> serviceInfoBos) {
        if (serviceInfoBos == null) {
            return Collections.emptyList();
        }
        List<String> serviceInfos = new ArrayList<>();
        for (ServiceInfoBo serviceInfoBo : serviceInfoBos) {
            List<String> flattenedServiceInfos = flattenServiceInfo(serviceInfoBo);
            if (flattenedServiceInfos != null && !flattenedServiceInfos.isEmpty()) {
                serviceInfos.addAll(flattenedServiceInfos);
            }
        }
        return serviceInfos;
    }

    default List<String> flattenServiceInfo(ServiceInfoBo serviceInfoBo) {
        List<String> serviceInfos = new ArrayList<>();
        for (String serviceLib : serviceInfoBo.getServiceLibs()) {
            if (serviceLib != null && !serviceLib.isEmpty()) {
                String s = serviceInfoBo.getServiceName() + serviceLib;
                serviceInfos.add(s);
            }
        }
        return serviceInfos;
    }
}
