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

package com.navercorp.pinpoint.otlp.trace.collector.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * Admission control for the OTLP/HTTP trace ingestion endpoint (POST /v1/traces).
 *
 * <p>Mirrors the gRPC path ({@code GrpcOtlpTraceService}) safeguards on the servlet side, but
 * enforced in a filter <b>before</b> the protobuf body is materialized by
 * {@code ProtobufHttpMessageConverter}:
 * <ul>
 *   <li>per-request size cap &rarr; 413 (Content-Length check; unknown-length bodies are bounded
 *       by a limiting input stream)</li>
 *   <li>global in-flight byte budget (Semaphore) &rarr; 503 + Retry-After</li>
 *   <li>concurrent request cap (Semaphore) &rarr; 503 + Retry-After</li>
 * </ul>
 * Permits are released in {@code finally} after the whole request (parse + insert) completes.
 *
 * <p>The byte budget is kept separate from the gRPC {@code admission.max-in-flight-bytes} for now
 * (independent tuning / fault isolation); it can be unified behind a shared admission bean later.
 */
public class OtlpTraceHttpAdmissionFilter extends OncePerRequestFilter {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final int maxRequestBytes;
    private final int maxInFlightBytes;
    private final int maxConcurrentRequests;
    private final int retryAfterSeconds;

    // Global byte-based admission: caps the total in-flight request bytes so concurrent requests
    // cannot exhaust the heap regardless of request count.
    private final Semaphore admissionBytes;
    // Request-count gate: guards the servlet thread pool against a flood of small requests.
    private final Semaphore concurrency;

    public OtlpTraceHttpAdmissionFilter(int maxRequestBytes, int maxInFlightBytes, int maxConcurrentRequests, int retryAfterSeconds) {
        this.maxRequestBytes = maxRequestBytes;
        this.maxInFlightBytes = maxInFlightBytes;
        this.maxConcurrentRequests = maxConcurrentRequests;
        this.retryAfterSeconds = retryAfterSeconds;
        this.admissionBytes = new Semaphore(maxInFlightBytes);
        this.concurrency = new Semaphore(maxConcurrentRequests);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1) Size cap: reject before the body is buffered/parsed.
        final long contentLength = request.getContentLengthLong();
        if (contentLength > maxRequestBytes) {
            reject(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, false,
                    "payload too large: contentLength=" + contentLength + ", max=" + maxRequestBytes);
            return;
        }
        // Reserve the known length, or the max (pessimistic) when the length is unknown (chunked).
        // contentLength is already <= maxRequestBytes (an int) here, so the cast is safe.
        final int reserveBytes = contentLength >= 0 ? (int) contentLength : maxRequestBytes;

        // 2) Concurrency gate.
        if (!concurrency.tryAcquire()) {
            reject(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, true,
                    "concurrency limit exceeded: max=" + maxConcurrentRequests);
            return;
        }
        // 3) In-flight byte gate.
        if (!admissionBytes.tryAcquire(reserveBytes)) {
            concurrency.release();
            reject(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, true,
                    "in-flight byte budget exhausted: reserve=" + reserveBytes + ", budget=" + maxInFlightBytes);
            return;
        }

        try {
            final HttpServletRequest effectiveRequest = contentLength >= 0
                    ? request
                    // Unknown length: cap the stream so a chunked body cannot exceed maxRequestBytes in heap.
                    : new LimitedRequestWrapper(request, maxRequestBytes);
            filterChain.doFilter(effectiveRequest, response);
        } finally {
            admissionBytes.release(reserveBytes);
            concurrency.release();
        }
    }

    private void reject(HttpServletResponse response, int status, boolean retryable, String reason) {
        if (retryable) {
            response.setHeader("Retry-After", Integer.toString(retryAfterSeconds));
        }
        response.setStatus(status);
        logger.warn("OTLP/HTTP trace request rejected. status={}, reason={}", status, reason);
    }

    /**
     * Wraps the request so an unknown-length (chunked) body is read through a size-limited stream.
     */
    private static final class LimitedRequestWrapper extends HttpServletRequestWrapper {
        private final long limit;

        private LimitedRequestWrapper(HttpServletRequest request, long limit) {
            super(request);
            this.limit = limit;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new LimitedServletInputStream(super.getInputStream(), limit);
        }
    }

    /**
     * Counting {@link ServletInputStream} that aborts once the body exceeds {@code limit} bytes.
     * The resulting {@link IOException} surfaces to the protobuf converter as a read failure
     * (HTTP 400), bounding heap usage for bodies without a Content-Length.
     */
    private static final class LimitedServletInputStream extends ServletInputStream {
        private final ServletInputStream delegate;
        private final long limit;
        private long count;

        private LimitedServletInputStream(ServletInputStream delegate, long limit) {
            this.delegate = delegate;
            this.limit = limit;
        }

        private void add(int read) throws IOException {
            if (read <= 0) {
                return;
            }
            count += read;
            if (count > limit) {
                throw new IOException("OTLP/HTTP request body exceeded max size: limit=" + limit);
            }
        }

        @Override
        public int read() throws IOException {
            final int b = delegate.read();
            add(b < 0 ? 0 : 1);
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            final int n = delegate.read(b, off, len);
            add(n);
            return n;
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            delegate.setReadListener(readListener);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
