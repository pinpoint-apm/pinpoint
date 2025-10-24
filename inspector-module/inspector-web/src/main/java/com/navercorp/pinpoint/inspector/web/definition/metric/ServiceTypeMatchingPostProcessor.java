/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.web.definition.metric;

import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo-jung
 */
@Component
public class ServiceTypeMatchingPostProcessor implements MetricPostProcessor {

    public static final String SERVICE_TYPE = "serviceType";
    private static final String SERVICE_TYPE_CODE = "serviceTypeCode";
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    public ServiceTypeMatchingPostProcessor(ServiceTypeRegistryService serviceTypeRegistryService) {
        this.serviceTypeRegistryService = serviceTypeRegistryService;
    }

    @Override
    public String getName() {
        return "matchingServiceType";
    }

    @Override
    public List<InspectorMetricValue> postProcess(List<InspectorMetricValue> metricValueList) {
        if (metricValueList.isEmpty()) {
            return metricValueList;
        }

        List<InspectorMetricValue> newMetricValueList = new ArrayList<>(metricValueList.size());
        
        for(InspectorMetricValue inspectorMetricValue : metricValueList) {
            List<Tag> tagList = changeServiceTypeCodeToName(inspectorMetricValue.getTagList());
            newMetricValueList.add(new InspectorMetricValue(inspectorMetricValue.getFieldName(),
                                                            tagList,
                                                            inspectorMetricValue.getChartType(),
                                                            inspectorMetricValue.getUnit(),
                                                            inspectorMetricValue.getValueList()));
        }

        return newMetricValueList;
    }

    private List<Tag> changeServiceTypeCodeToName(List<Tag> tagList) {
        List<Tag> newTagList = new ArrayList<>(tagList.size());

        for (Tag tag : tagList) {
            if (SERVICE_TYPE_CODE.equals(tag.getName())) {
                int serviceTypeCode = Integer.parseInt(tag.getValue());
                String serviceTypeName = serviceTypeRegistryService.findServiceType(serviceTypeCode).getName();
                newTagList.add(new Tag(SERVICE_TYPE, serviceTypeName));
            } else {
                newTagList.add(tag);
            }
        }

        return newTagList;
    }
}
