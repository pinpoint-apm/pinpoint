/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.collector.controller;

import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceIngestMetrics;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.util.StreamUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceHttpAdmissionFilterTest {

    private static final int MAX_REQUEST_BYTES = 100;
    private static final int RETRY_AFTER = 1;

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OtlpTraceIngestMetrics metrics = new OtlpTraceIngestMetrics(registry);

    private static MockHttpServletRequest request(int bodyBytes) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/traces");
        request.setContent(new byte[bodyBytes]);
        return request;
    }

    private double gauge(String name) {
        return registry.get(name).tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "http").gauge().value();
    }

    @Test
    void admitted_recordsDeclaredLength_andGaugesShowOccupancyDuringChain() throws ServletException, IOException {
        OtlpTraceHttpAdmissionFilter filter = new OtlpTraceHttpAdmissionFilter(MAX_REQUEST_BYTES, 1_000, 8, RETRY_AFTER, metrics);
        double[] duringBytes = new double[1];
        double[] duringRequests = new double[1];
        MockFilterChain chain = new MockFilterChain(new jakarta.servlet.http.HttpServlet() {
            @Override
            protected void service(jakarta.servlet.http.HttpServletRequest req, HttpServletResponse res) {
                duringBytes[0] = gauge(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_BYTES);
                duringRequests[0] = gauge(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_REQUESTS);
            }
        });

        filter.doFilter(request(40), new MockHttpServletResponse(), chain);

        assertThat(duringBytes[0]).isEqualTo(40.0);
        assertThat(duringRequests[0]).isEqualTo(1.0);
        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isZero();
        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_REQUESTS)).isZero();
        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_LIMIT_BYTES)).isEqualTo(1_000.0);
        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_LIMIT_REQUESTS)).isEqualTo(8.0);
        DistributionSummary bytes = registry.get(OtlpTraceIngestMetrics.REQUEST_BYTES).tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "http").summary();
        assertThat(bytes.count()).isEqualTo(1);
        assertThat(bytes.totalAmount()).isEqualTo(40.0);
    }

    @Test
    void chunkedBody_recordsBytesActuallyRead() throws ServletException, IOException {
        OtlpTraceHttpAdmissionFilter filter = new OtlpTraceHttpAdmissionFilter(MAX_REQUEST_BYTES, 1_000, 8, RETRY_AFTER, metrics);
        MockHttpServletRequest body = new MockHttpServletRequest("POST", "/v1/traces");
        body.setContent(new byte[33]);
        // MockHttpServletRequest reports the content length from the body; hide it like a chunked request.
        jakarta.servlet.http.HttpServletRequest chunked = new UnknownLengthRequest(body);
        MockFilterChain chain = new MockFilterChain(new jakarta.servlet.http.HttpServlet() {
            @Override
            protected void service(jakarta.servlet.http.HttpServletRequest req, HttpServletResponse res) throws IOException {
                StreamUtils.copyToByteArray(req.getInputStream());
            }
        });

        filter.doFilter(chunked, new MockHttpServletResponse(), chain);

        DistributionSummary bytes = registry.get(OtlpTraceIngestMetrics.REQUEST_BYTES).tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "http").summary();
        assertThat(bytes.count()).isEqualTo(1);
        assertThat(bytes.totalAmount()).isEqualTo(33.0);
    }

    /** Hides the body length so the filter takes the chunked (unknown-length) path. */
    private static final class UnknownLengthRequest extends jakarta.servlet.http.HttpServletRequestWrapper {
        private UnknownLengthRequest(jakarta.servlet.http.HttpServletRequest request) {
            super(request);
        }

        @Override
        public int getContentLength() {
            return -1;
        }

        @Override
        public long getContentLengthLong() {
            return -1;
        }
    }

    private double requestRejected(String reason) {
        return registry.get(OtlpTraceIngestMetrics.REQUEST_REJECTED)
                .tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "http")
                .tag(OtlpTraceIngestMetrics.TAG_REASON, reason)
                .counter().count();
    }

    @Test
    void payloadTooLarge_413_countsPayloadTooLarge() throws ServletException, IOException {
        OtlpTraceHttpAdmissionFilter filter = new OtlpTraceHttpAdmissionFilter(MAX_REQUEST_BYTES, 1_000, 8, RETRY_AFTER, metrics);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request(MAX_REQUEST_BYTES + 1), response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        assertThat(response.getHeader("Retry-After")).as("client fault: not retryable").isNull();
        assertThat(chain.getRequest()).isNull();
        assertThat(requestRejected("payload_too_large")).isEqualTo(1.0);
        assertThat(requestRejected("concurrency")).isZero();
        assertThat(requestRejected("inflight_bytes")).isZero();
    }

    @Test
    void concurrencyLimit_503RetryAfter_countsConcurrency() throws ServletException, IOException {
        // Zero permits: the gate rejects every request without needing a blocked in-flight one.
        OtlpTraceHttpAdmissionFilter filter = new OtlpTraceHttpAdmissionFilter(MAX_REQUEST_BYTES, 1_000, 0, RETRY_AFTER, metrics);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request(10), response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        assertThat(response.getHeader("Retry-After")).isEqualTo(Integer.toString(RETRY_AFTER));
        assertThat(chain.getRequest()).isNull();
        assertThat(requestRejected("concurrency")).isEqualTo(1.0);
        assertThat(requestRejected("inflight_bytes")).isZero();
    }

    @Test
    void inFlightBudgetExhausted_503RetryAfter_countsInflightBytes_andReleasesConcurrencyPermit() throws ServletException, IOException {
        // Budget 10 bytes, request 50 bytes (below the per-request cap): the byte gate rejects.
        OtlpTraceHttpAdmissionFilter filter = new OtlpTraceHttpAdmissionFilter(MAX_REQUEST_BYTES, 10, 1, RETRY_AFTER, metrics);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request(50), response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        assertThat(response.getHeader("Retry-After")).isEqualTo(Integer.toString(RETRY_AFTER));
        assertThat(requestRejected("inflight_bytes")).isEqualTo(1.0);
        assertThat(requestRejected("concurrency")).isZero();

        // The single concurrency permit was given back: a small request now passes the gate.
        MockHttpServletResponse okResponse = new MockHttpServletResponse();
        MockFilterChain okChain = new MockFilterChain();
        filter.doFilter(request(5), okResponse, okChain);
        assertThat(okChain.getRequest()).isNotNull();
        assertThat(okResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void admitted_passesToChain_countsNothing() throws ServletException, IOException {
        OtlpTraceHttpAdmissionFilter filter = new OtlpTraceHttpAdmissionFilter(MAX_REQUEST_BYTES, 1_000, 8, RETRY_AFTER, metrics);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request(10), response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(registry.get(OtlpTraceIngestMetrics.REQUEST_REJECTED).counters())
                .allSatisfy(c -> assertThat(c.count()).isZero());
    }

    @Test
    void rejectedRequests_doNotRecordWireBytes() throws ServletException, IOException {
        // 413 (per-request cap), 503 concurrency (zero permits) and 503 in-flight budget (10 bytes):
        // request.bytes only measures admitted requests, so all three leave the summary empty.
        new OtlpTraceHttpAdmissionFilter(MAX_REQUEST_BYTES, 1_000, 8, RETRY_AFTER, metrics)
                .doFilter(request(MAX_REQUEST_BYTES + 1), new MockHttpServletResponse(), new MockFilterChain());
        new OtlpTraceHttpAdmissionFilter(MAX_REQUEST_BYTES, 1_000, 0, RETRY_AFTER, metrics)
                .doFilter(request(10), new MockHttpServletResponse(), new MockFilterChain());
        new OtlpTraceHttpAdmissionFilter(MAX_REQUEST_BYTES, 10, 1, RETRY_AFTER, metrics)
                .doFilter(request(50), new MockHttpServletResponse(), new MockFilterChain());

        DistributionSummary bytes = registry.get(OtlpTraceIngestMetrics.REQUEST_BYTES).tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "http").summary();
        assertThat(bytes.count()).isZero();
        assertThat(requestRejected("payload_too_large")).isEqualTo(1.0);
        assertThat(requestRejected("concurrency")).isEqualTo(1.0);
        assertThat(requestRejected("inflight_bytes")).isEqualTo(1.0);
    }

    @Test
    void chunkedBody_chainThrows_stillRecordsBytesRead_andReleasesPermits() throws ServletException, IOException {
        // Single permit on both gates: a leaked permit would make the follow-up request fail.
        OtlpTraceHttpAdmissionFilter filter = new OtlpTraceHttpAdmissionFilter(MAX_REQUEST_BYTES, MAX_REQUEST_BYTES, 1, RETRY_AFTER, metrics);
        MockHttpServletRequest body = new MockHttpServletRequest("POST", "/v1/traces");
        body.setContent(new byte[21]);
        jakarta.servlet.http.HttpServletRequest chunked = new UnknownLengthRequest(body);
        MockFilterChain failing = new MockFilterChain(new jakarta.servlet.http.HttpServlet() {
            @Override
            protected void service(jakarta.servlet.http.HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
                StreamUtils.copyToByteArray(req.getInputStream());
                throw new ServletException("downstream failure");
            }
        });

        assertThatThrownBy(() -> filter.doFilter(chunked, new MockHttpServletResponse(), failing))
                .isInstanceOf(ServletException.class);

        DistributionSummary bytes = registry.get(OtlpTraceIngestMetrics.REQUEST_BYTES).tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "http").summary();
        assertThat(bytes.count()).isEqualTo(1);
        assertThat(bytes.totalAmount()).isEqualTo(21.0);
        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_BYTES)).isZero();
        assertThat(gauge(OtlpTraceIngestMetrics.ADMISSION_INFLIGHT_REQUESTS)).isZero();

        MockFilterChain next = new MockFilterChain();
        MockHttpServletResponse nextResponse = new MockHttpServletResponse();
        filter.doFilter(request(5), nextResponse, next);
        assertThat(next.getRequest()).as("permits released after the failed request").isNotNull();
        assertThat(nextResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(registry.get(OtlpTraceIngestMetrics.REQUEST_REJECTED).counters())
                .allSatisfy(c -> assertThat(c.count()).isZero());
    }
}
