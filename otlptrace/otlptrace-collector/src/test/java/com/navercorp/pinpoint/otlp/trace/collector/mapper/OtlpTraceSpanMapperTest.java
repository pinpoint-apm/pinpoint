package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceSpanMapperTest {

    // =======================================================================
    // extractHostAndPort
    // =======================================================================

    @Test
    void extractHostAndPort_withSchemeAndPort() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com:8080/path"))
                .isEqualTo("example.com:8080");
    }

    @Test
    void extractHostAndPort_withSchemeNoPort() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("https://example.com/path"))
                .isEqualTo("example.com");
    }

    @Test
    void extractHostAndPort_withSchemeNoPath() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com:9090"))
                .isEqualTo("example.com:9090");
    }

    @Test
    void extractHostAndPort_noScheme() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("example.com:8080/path"))
                .isEqualTo("example.com:8080");
    }

    @Test
    void extractHostAndPort_withQuery() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com/path?key=val"))
                .isEqualTo("example.com");
    }

    @Test
    void extractHostAndPort_withFragment() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("http://example.com/path#section"))
                .isEqualTo("example.com");
    }

    @Test
    void extractHostAndPort_null() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort(null)).isNull();
    }

    @Test
    void extractHostAndPort_empty() {
        assertThat(OtlpTraceSpanMapper.extractHostAndPort("")).isNull();
    }

    // =======================================================================
    // extractPath
    // =======================================================================

    @Test
    void extractPath_withSchemeAndPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/api/users"))
                .isEqualTo("/api/users");
    }

    @Test
    void extractPath_withQuery() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/api?key=val"))
                .isEqualTo("/api");
    }

    @Test
    void extractPath_withFragment() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/api#section"))
                .isEqualTo("/api");
    }

    @Test
    void extractPath_noPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com"))
                .isEqualTo("/");
    }

    @Test
    void extractPath_rootPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("http://example.com/"))
                .isEqualTo("/");
    }

    @Test
    void extractPath_null() {
        assertThat(OtlpTraceSpanMapper.extractPath(null)).isEqualTo("/");
    }

    @Test
    void extractPath_empty() {
        assertThat(OtlpTraceSpanMapper.extractPath("")).isEqualTo("/");
    }

    @Test
    void extractPath_noScheme() {
        assertThat(OtlpTraceSpanMapper.extractPath("example.com/api/hello"))
                .isEqualTo("/api/hello");
    }

    @Test
    void extractPath_deepPath() {
        assertThat(OtlpTraceSpanMapper.extractPath("https://example.com:443/a/b/c/d"))
                .isEqualTo("/a/b/c/d");
    }

}