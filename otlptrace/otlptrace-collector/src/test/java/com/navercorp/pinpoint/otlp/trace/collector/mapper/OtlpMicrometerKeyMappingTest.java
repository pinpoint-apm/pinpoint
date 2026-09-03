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

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Boot micrometer-tracing (OTel bridge) emits Micrometer key names ({@code uri}, {@code method},
 * {@code status}, {@code http.url}) instead of OTel semconv. Verifies the scope-gated mapping in
 * {@link OtlpMicrometerAttributes}.
 */
class OtlpMicrometerKeyMappingTest {

    private static final byte[] TRACE_ID = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    private static final byte[] ROOT = {1, 1, 1, 1, 1, 1, 1, 1};
    private static final byte[] CHILD = {2, 2, 2, 2, 2, 2, 2, 2};

    private static final InstrumentationScope SPRING_BOOT = InstrumentationScope.newBuilder().setName("org.springframework.boot").setVersion("3.5.14").build();
    private static final InstrumentationScope OTHER = InstrumentationScope.newBuilder().setName("io.opentelemetry.spring-webmvc-6.0").build();

    private static Span.Builder span(String name, byte[] id, byte[] parent, int kind) {
        Span.Builder b = Span.newBuilder().setName(name).setTraceId(ByteString.copyFrom(TRACE_ID)).setSpanId(ByteString.copyFrom(id))
                .setKindValue(kind).setStartTimeUnixNano(1_000_000_000L).setEndTimeUnixNano(2_000_000_000L);
        if (parent != null) {
            b.setParentSpanId(ByteString.copyFrom(parent));
        }
        return b;
    }

    /** Exactly what Spring MVC's DefaultServerRequestObservationConvention puts on a server span. */
    private static Span.Builder micrometerServer(String uri, String httpUrl, String status) {
        return span("http get " + uri, ROOT, null, Span.SpanKind.SPAN_KIND_SERVER_VALUE)
                .addAttributes(kv("exception", strVal("none")))
                .addAttributes(kv("http.url", strVal(httpUrl)))
                .addAttributes(kv("method", strVal("GET")))
                .addAttributes(kv("outcome", strVal("SUCCESS")))
                .addAttributes(kv("status", strVal(status)))
                .addAttributes(kv("uri", strVal(uri)));
    }

    private static OtlpTraceMapperData map(InstrumentationScope scope, Span... spans) {
        Resource resource = Resource.newBuilder()
                .addAttributes(kv("pinpoint.applicationName", strVal("boot-app")))
                .addAttributes(kv("service.instance.id", strVal("56d7dd8f-f1c8-4b81-a424-736421b7f530")))
                .build();
        ScopeSpans.Builder scopeSpans = ScopeSpans.newBuilder().setScope(scope);
        for (Span s : spans) {
            scopeSpans.addSpans(s);
        }
        return OtlpTraceMapperTest.newMapper().map(List.of(ResourceSpans.newBuilder().setResource(resource).addScopeSpans(scopeSpans).build()));
    }

    private static Optional<AnnotationBo> annotation(List<AnnotationBo> list, AnnotationKey key) {
        return list.stream().filter(a -> a.getKey() == key.getCode()).findFirst();
    }

    private static List<String> attributeKeys(List<AttributeBo> list) {
        return list == null ? List.of() : list.stream().map(AttributeBo::getKey).toList();
    }

    @Test
    void server_uriTemplateBecomesRpc_statusAndMethodPromoted() {
        OtlpTraceMapperData data = map(SPRING_BOOT, micrometerServer("/user/{id}", "/user/123", "200").build());

        SpanBo root = data.getSpanBoList().get(0);
        assertThat(root.getRpc()).isEqualTo("/user/{id}");
        assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_STATUS_CODE)).map(AnnotationBo::getValue).contains(200);
        assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_METHOD)).map(AnnotationBo::getValue).contains("GET");
        // consumed keys leave the raw attribute list; the rest (and http.url, never consumed) stay
        assertThat(attributeKeys(root.getAttributeBoList()))
                .doesNotContain("uri", "status", "method")
                .contains("http.url", "outcome", "exception");
        assertThat(data.getExceptionMetaDataBoList()).isEmpty();
    }

    @Test
    void server_uriPlaceholder_fallsBackToHttpUrlPath() {
        for (String placeholder : List.of("/**", "UNKNOWN", "REDIRECTION", "NOT_FOUND", "root")) {
            OtlpTraceMapperData data = map(SPRING_BOOT, micrometerServer(placeholder, "/no/such/path?x=1", "404").build());

            SpanBo root = data.getSpanBoList().get(0);
            assertThat(root.getRpc()).as(placeholder).isEqualTo("/no/such/path");
            // placeholder is not consumed → still visible as a raw attribute
            assertThat(attributeKeys(root.getAttributeBoList())).as(placeholder).contains("uri");
            assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_STATUS_CODE)).map(AnnotationBo::getValue).contains(404);
        }
    }

    @Test
    void server_semconvKeysTakePrecedenceOverMicrometerKeys() {
        Span both = micrometerServer("/micrometer/{id}", "/micrometer/1", "200")
                .addAttributes(kv("http.route", strVal("/semconv/{id}")))
                .addAttributes(kv("http.request.method", strVal("POST")))
                .addAttributes(kv("http.response.status_code", OtlpAnyValueFactory.intVal(201)))
                .build();
        OtlpTraceMapperData data = map(SPRING_BOOT, both);

        SpanBo root = data.getSpanBoList().get(0);
        assertThat(root.getRpc()).isEqualTo("/semconv/{id}");
        assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_STATUS_CODE)).map(AnnotationBo::getValue).contains(201);
        assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_METHOD)).map(AnnotationBo::getValue).contains("POST");
        // the Micrometer twins were not consumed
        assertThat(attributeKeys(root.getAttributeBoList())).contains("uri", "status", "method");
    }

    @Test
    void otherScope_micrometerKeysAreNotInterpreted() {
        OtlpTraceMapperData data = map(OTHER, micrometerServer("/user/{id}", "/user/123", "200").build());

        SpanBo root = data.getSpanBoList().get(0);
        assertThat(root.getRpc()).isEqualTo("/user/123");
        assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_STATUS_CODE)).isEmpty();
        assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_METHOD)).isEmpty();
        assertThat(attributeKeys(root.getAttributeBoList())).contains("uri", "status", "method");
    }

    @Test
    void exceptionTraceUriTemplate_usesTheRouteTemplate() {
        Span failing = micrometerServer("/user/{id}", "/user/123", "500")
                .setStatus(io.opentelemetry.proto.trace.v1.Status.newBuilder().setCodeValue(io.opentelemetry.proto.trace.v1.Status.StatusCode.STATUS_CODE_ERROR_VALUE))
                .addEvents(Span.Event.newBuilder().setName("exception").setTimeUnixNano(1_500_000_000L)
                        .addAttributes(kv("exception.type", strVal("java.lang.IllegalStateException")))
                        .addAttributes(kv("exception.message", strVal("boom"))))
                .build();
        OtlpTraceMapperData data = map(SPRING_BOOT, failing);

        assertThat(data.getSpanBoList().get(0).getRpc()).isEqualTo("/user/{id}");
        assertThat(data.getExceptionMetaDataBoList()).singleElement()
                .satisfies(ex -> assertThat(ex.getUriTemplate()).isEqualTo("/user/{id}"));
    }

    @Test
    void exceptionTraceUriTemplate_placeholderUri_isEmptyWhileRpcFallsBackToPath() {
        // No-route placeholder: the rpc keeps the raw http.url path for the transaction view, but the
        // exception uriTemplate must not — an unrouted request groups under "" like the agent.
        Span failing = micrometerServer("NOT_FOUND", "/no/such/path?x=1", "404")
                .setStatus(io.opentelemetry.proto.trace.v1.Status.newBuilder().setCodeValue(io.opentelemetry.proto.trace.v1.Status.StatusCode.STATUS_CODE_ERROR_VALUE))
                .addEvents(Span.Event.newBuilder().setName("exception").setTimeUnixNano(1_500_000_000L)
                        .addAttributes(kv("exception.type", strVal("org.springframework.web.servlet.NoHandlerFoundException")))
                        .addAttributes(kv("exception.message", strVal("No endpoint GET /no/such/path"))))
                .build();
        OtlpTraceMapperData data = map(SPRING_BOOT, failing);

        assertThat(data.getSpanBoList().get(0).getRpc()).isEqualTo("/no/such/path");
        assertThat(data.getExceptionMetaDataBoList()).singleElement()
                .satisfies(ex -> assertThat(ex.getUriTemplate()).isEmpty());
    }

    @Test
    void client_statusAndMethodPromoted_uriNotUsed() {
        Span root = micrometerServer("/remote", "/remote", "200").build();
        Span client = span("http get", CHILD, ROOT, Span.SpanKind.SPAN_KIND_CLIENT_VALUE)
                .addAttributes(kv("client.name", strVal("localhost")))
                .addAttributes(kv("exception", strVal("none")))
                .addAttributes(kv("http.url", strVal("http://localhost:18080/helloworld?q=1")))
                .addAttributes(kv("method", strVal("GET")))
                .addAttributes(kv("outcome", strVal("SUCCESS")))
                .addAttributes(kv("status", strVal("200")))
                .addAttributes(kv("uri", strVal("/helloworld")))
                .build();
        OtlpTraceMapperData data = map(SPRING_BOOT, root, client);

        SpanEventBo event = data.getSpanBoList().get(0).getSpanEventBoList().get(0);
        assertThat(event.getDestinationId()).isEqualTo("localhost:18080");
        assertThat(annotation(event.getAnnotationBoList(), AnnotationKey.HTTP_STATUS_CODE)).map(AnnotationBo::getValue).contains(200);
        assertThat(annotation(event.getAnnotationBoList(), AnnotationKey.HTTP_METHOD)).map(AnnotationBo::getValue).contains("GET");
        assertThat(attributeKeys(event.getAttributeBoList())).doesNotContain("status", "method").contains("uri", "client.name", "http.url");
    }

    @Test
    void client_nonNumericStatusIsNotPromoted() {
        Span root = micrometerServer("/remote", "/remote", "500").build();
        Span client = span("http get", CHILD, ROOT, Span.SpanKind.SPAN_KIND_CLIENT_VALUE)
                .addAttributes(kv("method", strVal("GET")))
                .addAttributes(kv("status", strVal("IO_ERROR")))
                .addAttributes(kv("http.url", strVal("http://localhost:1/x")))
                .build();
        OtlpTraceMapperData data = map(SPRING_BOOT, root, client);

        SpanEventBo event = data.getSpanBoList().get(0).getSpanEventBoList().get(0);
        assertThat(annotation(event.getAnnotationBoList(), AnnotationKey.HTTP_STATUS_CODE)).isEmpty();
        assertThat(attributeKeys(event.getAttributeBoList())).contains("status");
    }

    @Test
    void observedInternal_methodIsJavaMethod_notPromoted() {
        Span root = micrometerServer("/observed", "/observed", "200").build();
        Span observed = span("observed-hello", CHILD, ROOT, Span.SpanKind.SPAN_KIND_INTERNAL_VALUE)
                .addAttributes(kv("class", strVal("com.example.TestService")))
                .addAttributes(kv("method", strVal("observedHello")))
                .build();
        OtlpTraceMapperData data = map(SPRING_BOOT, root, observed);

        SpanEventBo event = data.getSpanBoList().get(0).getSpanEventBoList().get(0);
        assertThat(annotation(event.getAnnotationBoList(), AnnotationKey.HTTP_METHOD)).isEmpty();
        assertThat(attributeKeys(event.getAttributeBoList())).contains("class", "method");
    }

    @Test
    void server_specialValues_methodNoneAndNonNumericStatus_notPromoted() {
        // Micrometer's "none" method and a non-numeric status ("UNKNOWN") carry no HTTP value → stay raw
        Span server = span("http none", ROOT, null, Span.SpanKind.SPAN_KIND_SERVER_VALUE)
                .addAttributes(kv("http.url", strVal("/weird")))
                .addAttributes(kv("method", strVal("none")))
                .addAttributes(kv("status", strVal("UNKNOWN")))
                .addAttributes(kv("uri", strVal("/weird")))
                .build();
        OtlpTraceMapperData data = map(SPRING_BOOT, server);

        SpanBo root = data.getSpanBoList().get(0);
        assertThat(root.getRpc()).isEqualTo("/weird");
        assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_METHOD)).isEmpty();
        assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_STATUS_CODE)).isEmpty();
        assertThat(attributeKeys(root.getAttributeBoList())).contains("method", "status").doesNotContain("uri");
    }

    @Test
    void server_uriAbsentOrEmpty_fallsBackToHttpUrlPath() {
        Span noUri = span("http get", ROOT, null, Span.SpanKind.SPAN_KIND_SERVER_VALUE)
                .addAttributes(kv("http.url", strVal("/no-uri?x=1")))
                .addAttributes(kv("method", strVal("GET")))
                .addAttributes(kv("status", strVal("200")))
                .build();
        assertThat(map(SPRING_BOOT, noUri).getSpanBoList().get(0).getRpc()).isEqualTo("/no-uri");

        Span emptyUri = micrometerServer("", "/empty-uri", "200").build();
        SpanBo root = map(SPRING_BOOT, emptyUri).getSpanBoList().get(0);
        assertThat(root.getRpc()).isEqualTo("/empty-uri");
        assertThat(attributeKeys(root.getAttributeBoList())).contains("uri"); // not consumed
    }

    @Test
    void clientRoot_statusAndMethodPromoted_uriNotUsedForRpc() {
        // Boot app without an inbound span (batch job): the HTTP client observation is the root span.
        // Its Micrometer status/method are HTTP-typed and are promoted like on the SpanEvent path;
        // `uri` (client URI template) is not an rpc source.
        Span clientRoot = span("http get", ROOT, null, Span.SpanKind.SPAN_KIND_CLIENT_VALUE)
                .addAttributes(kv("client.name", strVal("api.example.com")))
                .addAttributes(kv("http.url", strVal("https://api.example.com/items/7")))
                .addAttributes(kv("method", strVal("GET")))
                .addAttributes(kv("status", strVal("200")))
                .addAttributes(kv("uri", strVal("/items/{id}")))
                .build();
        OtlpTraceMapperData data = map(SPRING_BOOT, clientRoot);

        SpanBo root = data.getSpanBoList().get(0);
        assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_STATUS_CODE)).map(AnnotationBo::getValue).contains(200);
        assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_METHOD)).map(AnnotationBo::getValue).contains("GET");
        assertThat(root.getRpc()).isNotEqualTo("/items/{id}");
        assertThat(attributeKeys(root.getAttributeBoList())).contains("uri").doesNotContain("status", "method");
    }

    @Test
    void internalRoot_methodIsJavaMethod_notPromoted() {
        // @Observed root (no HTTP observation at all): `method` is the Java method name
        Span observedRoot = span("observed-job", ROOT, null, Span.SpanKind.SPAN_KIND_INTERNAL_VALUE)
                .addAttributes(kv("class", strVal("com.example.JobService")))
                .addAttributes(kv("method", strVal("runJob")))
                .build();
        OtlpTraceMapperData data = map(SPRING_BOOT, observedRoot);

        SpanBo root = data.getSpanBoList().get(0);
        assertThat(annotation(root.getAnnotationBoList(), AnnotationKey.HTTP_METHOD)).isEmpty();
        assertThat(attributeKeys(root.getAttributeBoList())).contains("class", "method");
    }
}
