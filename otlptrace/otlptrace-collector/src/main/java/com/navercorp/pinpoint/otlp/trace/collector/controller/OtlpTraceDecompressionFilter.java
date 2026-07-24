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
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Content-Encoding decompression for the OTLP/HTTP trace endpoint (POST /v1/traces).
 *
 * <p>The OTel Collector's {@code otlphttp} exporter compresses with gzip by default, so without
 * this filter the raw gzip bytes reach {@code ProtobufHttpMessageConverter} and fail to parse
 * (HTTP 400) — an OTLP/HTTP interop break. This filter transparently inflates a
 * {@code Content-Encoding: gzip} body so the converter sees plain protobuf.
 *
 * <ul>
 *   <li>no / empty Content-Encoding, or {@code identity} &rarr; passed through unchanged</li>
 *   <li>{@code gzip} &rarr; body inflated through a size-limited stream</li>
 *   <li>any other encoding &rarr; 415 Unsupported Media Type (only gzip is defined by OTLP/HTTP)</li>
 * </ul>
 *
 * <p><b>Decompression-bomb guard.</b> The admission filter's per-request cap bounds only the
 * <i>compressed</i> size (Content-Length); a small gzip body can inflate by orders of magnitude.
 * The inflated stream is therefore capped at {@code maxDecompressedBytes}: exceeding it aborts the
 * read with an {@link IOException}, which surfaces to the converter as a 400 (same as the admission
 * filter's chunked-body guard). Worst-case heap is {@code maxConcurrentRequests * maxDecompressedBytes},
 * since the converter materializes the whole decompressed message.
 *
 * <p>Runs just after {@link OtlpTraceHttpAdmissionFilter} so the compressed-size gates (413 /
 * in-flight byte budget) apply to the raw request first. The gRPC path needs no counterpart: grpc-java
 * decompresses {@code grpc-encoding: gzip} via its default decompressor registry.
 */
public class OtlpTraceDecompressionFilter extends OncePerRequestFilter {

    private static final String GZIP = "gzip";
    private static final String IDENTITY = "identity";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final int maxDecompressedBytes;

    public OtlpTraceDecompressionFilter(int maxDecompressedBytes) {
        this.maxDecompressedBytes = maxDecompressedBytes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String encoding = request.getHeader(HttpHeaders.CONTENT_ENCODING);
        if (encoding == null || encoding.isBlank() || IDENTITY.equalsIgnoreCase(encoding.trim())) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!GZIP.equalsIgnoreCase(encoding.trim())) {
            // Only gzip is defined for OTLP/HTTP; reject anything else (incl. multi-encoding) explicitly
            // rather than letting an undecoded body fail later as an opaque 400.
            logger.warn("OTLP/HTTP trace request rejected. Unsupported Content-Encoding={}", encoding);
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        filterChain.doFilter(new GzipRequestWrapper(request, maxDecompressedBytes), response);
    }

    /**
     * Presents a gzip-encoded request as its decompressed body. Content-Length now describes the
     * compressed size, so it is cleared (reported as unknown) to stop a downstream reader from
     * truncating the inflated stream at the compressed length.
     */
    private static final class GzipRequestWrapper extends HttpServletRequestWrapper {
        private final int limit;
        // Created lazily and cached so repeated getInputStream() calls return the same instance
        // (servlet wrapper contract). A second GZIPInputStream over the already-consumed underlying
        // stream would otherwise fail to re-read the gzip header. Single-threaded per request.
        private ServletInputStream inputStream;

        private GzipRequestWrapper(HttpServletRequest request, int limit) {
            super(request);
            this.limit = limit;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (this.inputStream == null) {
                this.inputStream = new GzipLimitedServletInputStream(super.getInputStream(), limit);
            }
            return this.inputStream;
        }

        @Override
        public int getContentLength() {
            return -1;
        }

        @Override
        public long getContentLengthLong() {
            return -1;
        }

        // Hide the now-inaccurate framing headers across every accessor: Content-Length describes the
        // compressed size and Content-Encoding has already been applied, so a caller reading either
        // (directly, by enumeration, or as an int) must not see the original values.
        private static boolean isHiddenHeader(String name) {
            return HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)
                    || HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase(name);
        }

        @Override
        public String getHeader(String name) {
            return isHiddenHeader(name) ? null : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return isHiddenHeader(name) ? Collections.emptyEnumeration() : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            final List<String> names = new ArrayList<>();
            final Enumeration<String> original = super.getHeaderNames();
            if (original != null) {
                while (original.hasMoreElements()) {
                    final String name = original.nextElement();
                    if (!isHiddenHeader(name)) {
                        names.add(name);
                    }
                }
            }
            return Collections.enumeration(names);
        }

        @Override
        public int getIntHeader(String name) {
            // Contract: -1 when the header is absent, which is what hiding it should look like.
            return isHiddenHeader(name) ? -1 : super.getIntHeader(name);
        }
    }

    /**
     * Inflates a gzip stream while counting decompressed bytes, aborting once {@code limit} is exceeded.
     * The {@link GZIPInputStream} is constructed eagerly, so a body that is not valid gzip fails here
     * (surfaces to the protobuf converter as a 400). Async reads are not supported (OTLP ingestion is
     * a blocking read through the message converter).
     */
    private static final class GzipLimitedServletInputStream extends ServletInputStream {
        private final GZIPInputStream gzip;
        private final long limit;
        private long count;
        private boolean finished;

        private GzipLimitedServletInputStream(InputStream compressed, long limit) throws IOException {
            this.gzip = new GZIPInputStream(compressed);
            this.limit = limit;
        }

        private void add(int read) throws IOException {
            count += read;
            if (count > limit) {
                throw new IOException("OTLP/HTTP decompressed request body exceeded max size: limit=" + limit);
            }
        }

        @Override
        public int read() throws IOException {
            final int b = gzip.read();
            if (b < 0) {
                finished = true;
                return -1;
            }
            add(1);
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            final int n = gzip.read(b, off, len);
            if (n < 0) {
                finished = true;
                return -1;
            }
            add(n);
            return n;
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("async read not supported");
        }

        @Override
        public void close() throws IOException {
            gzip.close();
        }
    }
}
