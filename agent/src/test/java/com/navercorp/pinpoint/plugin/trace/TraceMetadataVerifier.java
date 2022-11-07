/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.trace;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import com.navercorp.pinpoint.common.profiler.trace.StaticFieldLookUp;
import org.junit.rules.ErrorCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author HyunGil Jeong
 */
class TraceMetadataVerifier {

    private final Map<Short, List<String>> serviceTypeNamesByCode = new TreeMap<>();
    private final Map<String, List<Short>> serviceTypeCodesByName = new TreeMap<>();
    private final List<ServiceType> serviceTypeList = new ArrayList<>();
    private final Map<Integer, List<String>> annotationKeyNamesByCode = new TreeMap<>();

    private final TraceMetadataVerifierSetupContext traceMetadataSetupContext;

    TraceMetadataVerifier() {
        StaticFieldLookUp<ServiceType> staticServiceTypes = new StaticFieldLookUp<>(ServiceType.class, ServiceType.class);
        for (ServiceType staticServiceType : staticServiceTypes.lookup()) {
            addServiceType(staticServiceType);
        }
        StaticFieldLookUp<AnnotationKey> staticAnnotationKeys = new StaticFieldLookUp<>(AnnotationKey.class, AnnotationKey.class);
        for (AnnotationKey staticAnnotationKey : staticAnnotationKeys.lookup()) {
            addAnnotationKey(staticAnnotationKey);
        }
        this.traceMetadataSetupContext = new TraceMetadataVerifierSetupContext();
    }

    private void addServiceType(ServiceType serviceType) {

        serviceTypeList.add(serviceType);
        Short serviceTypeCode = serviceType.getCode();
        String serviceTypeName = serviceType.getName();

        List<String> serviceTypeNames = serviceTypeNamesByCode.computeIfAbsent(serviceTypeCode, k -> new ArrayList<>());
        serviceTypeNames.add(serviceTypeName);

        List<Short> serviceTypeCodes = serviceTypeCodesByName.computeIfAbsent(serviceTypeName, k -> new ArrayList<>());
        serviceTypeCodes.add(serviceTypeCode);
    }

    private void addAnnotationKey(AnnotationKey annotationKey) {
        Integer annotationKeyCode = annotationKey.getCode();
        List<String> annotationKeyNames = annotationKeyNamesByCode.computeIfAbsent(annotationKeyCode, k -> new ArrayList<>());
        String annotationKeyName = annotationKey.getName();
        annotationKeyNames.add(annotationKeyName);
    }

    TraceMetadataSetupContext getTraceMetadataSetupContext() {
        return traceMetadataSetupContext;
    }

    void verifyServiceTypes(ErrorCollector collector) {
        collector.checkSucceeds(() -> assertThat(traceMetadataSetupContext.dynamicServiceTypes)
                .isNotEmpty()
                .overridingErrorMessage("No service types registered by code.")
        );
        for (Map.Entry<Short, List<String>> e : serviceTypeNamesByCode.entrySet()) {
            Short serviceTypeCode = e.getKey();
            List<String> serviceTypeNames = e.getValue();
            collector.checkSucceeds(() -> assertThat(serviceTypeNames)
                    .hasSize(1)
                    .overridingErrorMessage("Duplicate ServiceType names. Code : " + serviceTypeCode + ", names : " + serviceTypeNames)
            );
        }
        for (Map.Entry<String, List<Short>> e : serviceTypeCodesByName.entrySet()) {
            String serviceTypeName = e.getKey();
            List<Short> serviceTypeCodes = e.getValue();
            collector.checkSucceeds(() -> assertThat(serviceTypeCodes)
                    .hasSize(1)
                    .overridingErrorMessage("Duplicate ServiceType codes. Name : " + serviceTypeName + ", codes : " + serviceTypeCodes)
            );
        }

        for(ServiceType serviceType : serviceTypeList){
            if (serviceType.isAlias()) {
                collector.checkSucceeds(() -> assertThat(serviceType.isRpcClient()).isTrue().overridingErrorMessage("ServiceType's code with ALIAS should be in range of RPC"));
                collector.checkSucceeds(() -> assertThat(serviceType.isRecordStatistics()).isFalse().overridingErrorMessage("ServiceType with ALIAS should NOT have RECORD_STATISTICS"));
            }
        }
    }

    void verifyAnnotationKeys(ErrorCollector collector) {
        collector.checkSucceeds(() -> assertThat(traceMetadataSetupContext.dynamicAnnotationKeys).isNotEmpty().overridingErrorMessage("No annotation keys registered."));
        for (Map.Entry<Integer, List<String>> e : annotationKeyNamesByCode.entrySet()) {
            Integer annotationKeyCode = e.getKey();
            List<String> annotationKeyNames = e.getValue();
            collector.checkSucceeds(() -> assertThat(annotationKeyNames).hasSize(1).overridingErrorMessage("Duplicate annotation keys. Code : " + annotationKeyCode + ", names : " + annotationKeyNames));
        }
    }

    private class TraceMetadataVerifierSetupContext implements TraceMetadataSetupContext {

        private final List<ServiceType> dynamicServiceTypes;
        private final List<AnnotationKey> dynamicAnnotationKeys;

        private TraceMetadataVerifierSetupContext() {
            this.dynamicServiceTypes = new ArrayList<>();
            this.dynamicAnnotationKeys = new ArrayList<>();
        }

        @Override
        public void addServiceType(ServiceType serviceType) {
            TraceMetadataVerifier.this.addServiceType(serviceType);
            dynamicServiceTypes.add(serviceType);
        }

        @Override
        public void addServiceType(ServiceType serviceType, AnnotationKeyMatcher primaryAnnotationKeyMatcher) {
            TraceMetadataVerifier.this.addServiceType(serviceType);
            dynamicServiceTypes.add(serviceType);
        }

        @Override
        public void addAnnotationKey(AnnotationKey annotationKey) {
            TraceMetadataVerifier.this.addAnnotationKey(annotationKey);
            dynamicAnnotationKeys.add(annotationKey);
        }
    }
}
