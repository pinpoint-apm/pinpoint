/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpMicrometerAttributesTest {

    private static InstrumentationScope scope(String name, String version) {
        InstrumentationScope.Builder builder = InstrumentationScope.newBuilder().setName(name);
        if (version != null) {
            builder.setVersion(version);
        }
        return builder.build();
    }

    @Test
    void isMicrometerScope_exactNameMatch_versionAgnostic() {
        assertThat(OtlpMicrometerAttributes.isMicrometerScope(scope("org.springframework.boot", "3.5.14"))).isTrue();
        assertThat(OtlpMicrometerAttributes.isMicrometerScope(scope("org.springframework.boot", "4.1.0"))).isTrue();
        assertThat(OtlpMicrometerAttributes.isMicrometerScope(scope("org.springframework.boot", null))).isTrue();
    }

    @Test
    void isMicrometerScope_rejectsNullPrefixAndOtherScopes() {
        assertThat(OtlpMicrometerAttributes.isMicrometerScope(null)).isFalse();
        assertThat(OtlpMicrometerAttributes.isMicrometerScope(InstrumentationScope.getDefaultInstance())).isFalse();
        // the gate is an exact match — neither a sub-package nor a parent package qualifies
        assertThat(OtlpMicrometerAttributes.isMicrometerScope(scope("org.springframework.boot.actuate", null))).isFalse();
        assertThat(OtlpMicrometerAttributes.isMicrometerScope(scope("org.springframework", null))).isFalse();
        assertThat(OtlpMicrometerAttributes.isMicrometerScope(scope("io.opentelemetry.spring-webmvc-6.0", "2.28.1-alpha"))).isFalse();
        assertThat(OtlpMicrometerAttributes.isMicrometerScope(scope("ORG.SPRINGFRAMEWORK.BOOT", null))).isFalse();
    }

    @Test
    void getUriTemplate_returnsTemplateAndConsumesKey() {
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpMicrometerAttributes.getUriTemplate(Map.of("uri", AttributeValue.of("/user/{id}")), consumed))
                .isEqualTo("/user/{id}");
        assertThat(consumed).containsExactly("uri");
    }

    @Test
    void getUriTemplate_placeholdersAbsentAndEmpty_returnNullWithoutConsuming() {
        for (String placeholder : List.of("/**", "UNKNOWN", "REDIRECTION", "NOT_FOUND", "root", "")) {
            Set<String> consumed = new HashSet<>();
            assertThat(OtlpMicrometerAttributes.getUriTemplate(Map.of("uri", AttributeValue.of(placeholder)), consumed))
                    .as("uri=%s", placeholder).isNull();
            assertThat(consumed).as("uri=%s", placeholder).isEmpty();
        }
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpMicrometerAttributes.getUriTemplate(Map.of(), consumed)).isNull();
        assertThat(consumed).isEmpty();
    }

    @Test
    void getUriTemplate_placeholderSetIsExactlyTheSpringConventionValues() {
        // DefaultServerRequestObservationConvention: "/**" (no handler), "UNKNOWN", "REDIRECTION",
        // "NOT_FOUND", "root" — a change here must be deliberate
        assertThat(OtlpMicrometerAttributes.URI_PLACEHOLDERS).containsExactlyInAnyOrder("/**", "UNKNOWN", "REDIRECTION", "NOT_FOUND", "root");
    }

    @Test
    void getResponseStatus_numericString_promoted_withSourceKey() {
        OtlpHttpStatusResolver.ResponseStatus status = OtlpMicrometerAttributes.getResponseStatus(Map.of("status", AttributeValue.of("404")));
        assertThat(status).isNotNull();
        assertThat(status.code()).isEqualTo(404);
        assertThat(status.sourceKey()).isEqualTo("status");
    }

    @Test
    void getResponseStatus_nonNumericOrAbsent_returnsNull() {
        for (String value : List.of("IO_ERROR", "CLIENT_ERROR", "UNKNOWN", "")) {
            assertThat(OtlpMicrometerAttributes.getResponseStatus(Map.of("status", AttributeValue.of(value))))
                    .as("status=%s", value).isNull();
        }
        assertThat(OtlpMicrometerAttributes.getResponseStatus(Map.of())).isNull();
    }

    @Test
    void getHttpMethod_returnsMethodAndConsumesKey() {
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpMicrometerAttributes.getHttpMethod(Map.of("method", AttributeValue.of("POST")), consumed)).isEqualTo("POST");
        assertThat(consumed).containsExactly("method");
    }

    @Test
    void getHttpMethod_noneEmptyAbsent_returnNullWithoutConsuming() {
        for (String value : List.of("none", "")) {
            Set<String> consumed = new HashSet<>();
            assertThat(OtlpMicrometerAttributes.getHttpMethod(Map.of("method", AttributeValue.of(value)), consumed))
                    .as("method=%s", value).isNull();
            assertThat(consumed).as("method=%s", value).isEmpty();
        }
        Set<String> consumed = new HashSet<>();
        assertThat(OtlpMicrometerAttributes.getHttpMethod(Map.of(), consumed)).isNull();
        assertThat(consumed).isEmpty();
    }
}
