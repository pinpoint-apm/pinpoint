/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.loader.plugins.trace.yaml;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.AnnotationKeyProperty;
import com.navercorp.pinpoint.common.trace.DefaultServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import com.navercorp.pinpoint.loader.plugins.trace.yaml.TraceMetadataProviderYamlParser;
import com.navercorp.pinpoint.loader.plugins.trace.TraceMetadataProviderParser;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author HyunGil Jeong
 */
public class TraceMetadataProviderYamlParserTest {

    private static final String VALID_TYPE_PROVIDER_ENTRY = "type-providers/valid.yml";

    @Test
    public void parseValid() {
        TraceMetadataProviderParser parser = new TraceMetadataProviderYamlParser();
        URL validTypeProviderEntryUrl = ClassLoader.getSystemResource(VALID_TYPE_PROVIDER_ENTRY);
        TraceMetadataProvider traceMetadataProvider = parser.parse(validTypeProviderEntryUrl);

        TraceMetadataProviderVerifier verifier = new TraceMetadataProviderVerifier(traceMetadataProvider);

        ServiceTypeInfoExpectation basic = new ServiceTypeInfoExpectation(1, "BASIC");
        verifier.verifyServiceType(basic);

        ServiceTypeInfoExpectation withDesc = new ServiceTypeInfoExpectation(2, "WITH_DESC")
                .desc("BASIC");
        verifier.verifyServiceType(withDesc);

        ServiceTypeInfoExpectation withProperties = new ServiceTypeInfoExpectation(3, "WITH_PROPERTY")
                .properties(ServiceTypeProperty.TERMINAL);
        verifier.verifyServiceType(withProperties);

        ServiceTypeInfoExpectation withMatcher = new ServiceTypeInfoExpectation(4, "WITH_MATCHER")
                .matcher(AnnotationKeyMatchers.exact(-1));
        verifier.verifyServiceType(withMatcher);

        ServiceTypeInfoExpectation withDescWithProperties = new ServiceTypeInfoExpectation(5, "WITH_DESC_WITH_PROPERTY")
                .desc("BASIC")
                .properties(ServiceTypeProperty.QUEUE)
                .properties(ServiceTypeProperty.RECORD_STATISTICS);
        verifier.verifyServiceType(withDescWithProperties);

        ServiceTypeInfoExpectation withDescWithMatcher = new ServiceTypeInfoExpectation(6, "WITH_DESC_WITH_MATCHER")
                .desc("BASIC")
                .matcher(AnnotationKeyMatchers.ARGS_MATCHER);
        verifier.verifyServiceType(withDescWithMatcher);

        ServiceTypeInfoExpectation withPropertiesWithMatcher = new ServiceTypeInfoExpectation(7, "WITH_PROPERTY_WITH_MATCHER")
                .properties(ServiceTypeProperty.INCLUDE_DESTINATION_ID)
                .matcher(AnnotationKeyMatchers.NOTHING_MATCHER);
        verifier.verifyServiceType(withPropertiesWithMatcher);

        ServiceTypeInfoExpectation withDescWithPropertiesWithMatcher = new ServiceTypeInfoExpectation(8, "WITH_DESC_WITH_PROPERTY_WITH_MATCHER")
                .desc("BASIC")
                .properties(ServiceTypeProperty.TERMINAL)
                .properties(ServiceTypeProperty.INCLUDE_DESTINATION_ID)
                .matcher(AnnotationKeyMatchers.exact(101));
        verifier.verifyServiceType(withDescWithPropertiesWithMatcher);

        AnnotationKeyExpectation testAnnotationArg = new AnnotationKeyExpectation(101, "test.annotation.arg");
        verifier.verifyAnnotationKey(testAnnotationArg);

        AnnotationKeyExpectation testAnnotationView = new AnnotationKeyExpectation(102, "test.annotation.view")
                .properties(AnnotationKeyProperty.VIEW_IN_RECORD_SET);
        verifier.verifyAnnotationKey(testAnnotationView);
    }

    private static class TraceMetadataProviderVerifier {

        private final Map<Short, ServiceTypeInfo> serviceTypes = new HashMap<Short, ServiceTypeInfo>();
        private final Map<Integer, AnnotationKey> annotationKeys = new HashMap<Integer, AnnotationKey>();

        private TraceMetadataProviderVerifier(TraceMetadataProvider traceMetadataProvider) {
            traceMetadataProvider.setup(new TraceMetadataSetupContext() {
                @Override
                public void addServiceType(ServiceType serviceType) {
                    serviceTypes.put(serviceType.getCode(), new DefaultServiceTypeInfo(serviceType));
                }

                @Override
                public void addServiceType(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
                    serviceTypes.put(serviceType.getCode(), new DefaultServiceTypeInfo(serviceType, annotationKeyMatcher));
                }

                @Override
                public void addAnnotationKey(AnnotationKey annotationKey) {
                    annotationKeys.put(annotationKey.getCode(), annotationKey);
                }
            });
        }

        void verifyServiceType(ServiceTypeInfoExpectation expectation) {
            ServiceTypeInfo actualServiceTypeInfo = serviceTypes.get(expectation.code);
            assertThat(actualServiceTypeInfo, is(notNullValue()));

            ServiceType actualServiceType = actualServiceTypeInfo.getServiceType();
            assertThat(actualServiceType.getCode(), is(expectation.code));
            assertThat(actualServiceType.getName(), is(expectation.name));
            if (expectation.desc == null) {
                assertThat(actualServiceType.getDesc(), is(expectation.name));
            } else {
                assertThat(actualServiceType.getDesc(), is(expectation.desc));
            }

            for (ServiceTypeProperty expectedServiceTypeProperty : expectation.serviceTypeProperties) {
                switch (expectedServiceTypeProperty) {
                    case TERMINAL:
                        assertTrue(actualServiceType.isTerminal());
                        break;
                    case QUEUE:
                        assertTrue(actualServiceType.isQueue());
                        break;
                    case RECORD_STATISTICS:
                        assertTrue(actualServiceType.isRecordStatistics());
                        break;
                    case INCLUDE_DESTINATION_ID:
                        assertTrue(actualServiceType.isIncludeDestinationId());
                        break;
                }
            }

            AnnotationKeyMatcher actualMatcher = actualServiceTypeInfo.getPrimaryAnnotationKeyMatcher();
            if (expectation.annotationKeyMatcher == null) {
                assertThat(actualMatcher, is(nullValue()));
            } else {
                // do toString() matching
                assertThat(actualMatcher.toString(), is(expectation.annotationKeyMatcher.toString()));
            }
        }

        void verifyAnnotationKey(AnnotationKeyExpectation expectation) {
            AnnotationKey actualAnnotationKey = annotationKeys.get(expectation.code);
            assertThat(actualAnnotationKey, is(notNullValue()));

            assertThat(actualAnnotationKey.getCode(), is(expectation.code));
            assertThat(actualAnnotationKey.getName(), is(expectation.name));

            for (AnnotationKeyProperty expectedAnnotationKeyProperty : expectation.annotationKeyProperties) {
                switch (expectedAnnotationKeyProperty) {
                    case VIEW_IN_RECORD_SET:
                        assertTrue(actualAnnotationKey.isViewInRecordSet());
                        break;
                    case ERROR_API_METADATA:
                        assertTrue(actualAnnotationKey.isErrorApiMetadata());
                        break;
                }
            }
        }
    }

    private static class ServiceTypeInfoExpectation {
        private final short code;
        private final String name;
        private final List<ServiceTypeProperty> serviceTypeProperties = new ArrayList<ServiceTypeProperty>();
        private String desc = null;
        private AnnotationKeyMatcher annotationKeyMatcher = null;

        ServiceTypeInfoExpectation(int code, String name) {
            this.code = (short) code;
            this.name = name;
        }

        ServiceTypeInfoExpectation desc(String desc) {
            this.desc = desc;
            return this;
        }

        ServiceTypeInfoExpectation properties(ServiceTypeProperty serviceTypeProperty) {
            this.serviceTypeProperties.add(serviceTypeProperty);
            return this;
        }

        ServiceTypeInfoExpectation matcher(AnnotationKeyMatcher annotationKeyMatcher) {
            this.annotationKeyMatcher = annotationKeyMatcher;
            return this;
        }
    }

    private static class AnnotationKeyExpectation {
        private final int code;
        private final String name;
        private final List<AnnotationKeyProperty> annotationKeyProperties = new ArrayList<AnnotationKeyProperty>();

        AnnotationKeyExpectation(int code, String name) {
            this.code = code;
            this.name = name;
        }

        AnnotationKeyExpectation properties(AnnotationKeyProperty annotationKeyProperty) {
            this.annotationKeyProperties.add(annotationKeyProperty);
            return this;
        }

    }
}
