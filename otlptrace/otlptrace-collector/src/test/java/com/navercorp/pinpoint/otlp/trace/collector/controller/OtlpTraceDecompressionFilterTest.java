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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtlpTraceDecompressionFilterTest {

    private static final int MAX_DECOMPRESSED = 1024 * 1024; // 1MB

    private static byte[] gzip(byte[] plain) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gos = new GZIPOutputStream(bos)) {
            gos.write(plain);
        }
        return bos.toByteArray();
    }

    private static MockHttpServletRequest request(String contentEncoding, byte[] body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/traces");
        if (contentEncoding != null) {
            request.addHeader(HttpHeaders.CONTENT_ENCODING, contentEncoding);
        }
        request.setContent(body);
        return request;
    }

    @Test
    void gzipBody_isDecompressedForDownstream() throws ServletException, IOException {
        byte[] plain = "hello-otlp-protobuf-payload".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = request("gzip", gzip(plain));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new OtlpTraceDecompressionFilter(MAX_DECOMPRESSED).doFilter(request, response, chain);

        assertThat(chain.getRequest()).as("chain invoked with wrapped request").isNotNull();
        byte[] decoded = StreamUtils.copyToByteArray(chain.getRequest().getInputStream());
        assertThat(decoded).isEqualTo(plain);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        // Content-Length now describes the compressed size, so it is hidden (unknown) to stop a
        // downstream reader from truncating the inflated stream at the compressed length.
        assertThat(chain.getRequest().getContentLength()).isEqualTo(-1);
        assertThat(((jakarta.servlet.http.HttpServletRequest) chain.getRequest())
                .getHeader(HttpHeaders.CONTENT_ENCODING)).isNull();
    }

    @Test
    void wrappedRequest_returnsSameInputStreamInstanceOnRepeatedCalls() throws ServletException, IOException {
        MockHttpServletRequest request = request("gzip", gzip("repeatable".getBytes(StandardCharsets.UTF_8)));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new OtlpTraceDecompressionFilter(MAX_DECOMPRESSED).doFilter(request, response, chain);

        jakarta.servlet.ServletRequest wrapped = chain.getRequest();
        // Servlet wrapper contract: repeated getInputStream() must return the same instance, otherwise
        // a second GZIPInputStream over the already-consumed underlying stream would corrupt the read.
        assertThat(wrapped.getInputStream()).isSameAs(wrapped.getInputStream());
    }

    @Test
    void wrappedRequest_hidesFramingHeadersAcrossAllAccessors() throws ServletException, IOException {
        byte[] body = gzip("payload".getBytes(StandardCharsets.UTF_8));
        MockHttpServletRequest request = request("gzip", body);
        request.addHeader("Content-Length", String.valueOf(body.length));
        request.addHeader("X-Custom", "keep");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new OtlpTraceDecompressionFilter(MAX_DECOMPRESSED).doFilter(request, response, chain);

        jakarta.servlet.http.HttpServletRequest wrapped =
                (jakarta.servlet.http.HttpServletRequest) chain.getRequest();
        // Content-Length (compressed) and Content-Encoding (already applied) must be hidden everywhere.
        assertThat(wrapped.getHeader(HttpHeaders.CONTENT_ENCODING)).isNull();
        assertThat(wrapped.getHeader(HttpHeaders.CONTENT_LENGTH)).isNull();
        assertThat(java.util.Collections.list(wrapped.getHeaders(HttpHeaders.CONTENT_ENCODING))).isEmpty();
        assertThat(java.util.Collections.list(wrapped.getHeaders(HttpHeaders.CONTENT_LENGTH))).isEmpty();
        assertThat(wrapped.getIntHeader(HttpHeaders.CONTENT_LENGTH)).isEqualTo(-1);

        List<String> names = java.util.Collections.list(wrapped.getHeaderNames());
        assertThat(names).noneMatch(n -> n.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)
                || n.equalsIgnoreCase(HttpHeaders.CONTENT_ENCODING));
        // Non-framing headers must still pass through.
        assertThat(wrapped.getHeader("X-Custom")).isEqualTo("keep");
        assertThat(names).contains("X-Custom");
    }

    @Test
    void gzipCaseInsensitive_isDecompressed() throws ServletException, IOException {
        byte[] plain = "case-insensitive".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = request("GZIP", gzip(plain));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new OtlpTraceDecompressionFilter(MAX_DECOMPRESSED).doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(StreamUtils.copyToByteArray(chain.getRequest().getInputStream())).isEqualTo(plain);
    }

    @Test
    void noEncoding_passesThroughUnchanged() throws ServletException, IOException {
        MockHttpServletRequest request = request(null, "plain".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new OtlpTraceDecompressionFilter(MAX_DECOMPRESSED).doFilter(request, response, chain);

        assertThat(chain.getRequest()).as("same request instance passed through").isSameAs(request);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void identityEncoding_passesThroughUnchanged() throws ServletException, IOException {
        MockHttpServletRequest request = request("identity", "plain".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new OtlpTraceDecompressionFilter(MAX_DECOMPRESSED).doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void unsupportedEncoding_returns415AndShortCircuits() throws ServletException, IOException {
        MockHttpServletRequest request = request("br", "whatever".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new OtlpTraceDecompressionFilter(MAX_DECOMPRESSED).doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        assertThat(chain.getRequest()).as("chain must not be invoked").isNull();
    }

    @Test
    void decompressionBomb_abortsReadOnceLimitExceeded() throws ServletException, IOException {
        // 64KB inflates to a tiny gzip body; a 1KB cap must abort the read before full inflation.
        byte[] plain = new byte[64 * 1024];
        MockHttpServletRequest request = request("gzip", gzip(plain));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        final int tinyLimit = 1024;
        new OtlpTraceDecompressionFilter(tinyLimit).doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThatThrownBy(() -> StreamUtils.copyToByteArray(chain.getRequest().getInputStream()))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("exceeded max size");
    }

    @Test
    void invalidGzip_failsOnStreamOpen() throws ServletException, IOException {
        MockHttpServletRequest request = request("gzip", "this-is-not-gzip".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new OtlpTraceDecompressionFilter(MAX_DECOMPRESSED).doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        // GZIPInputStream validates the header eagerly, so opening the body fails (surfaces as a 400).
        assertThatThrownBy(() -> chain.getRequest().getInputStream())
                .isInstanceOf(IOException.class);
    }
}
