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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpSensitiveAttributeFilterTest {

    private static final List<String> SENSITIVE_KEYS = List.of(
            // headers / metadata — whole prefix dropped, name-agnostic
            "http.request.header.authorization",
            "http.request.header.x-custom",
            "http.response.header.set-cookie",
            "rpc.request.metadata.authorization",
            "rpc.response.metadata.x-trace",
            "rpc.grpc.request.metadata.authorization",
            "rpc.grpc.response.metadata.x-token",
            "messaging.header.x-token",
            // bodies
            "http.request.body",
            "http.response.body",
            "messaging.message.body",
            "rpc.request.body",
            "rpc.response.body",
            // credentials (exact and last-segment)
            "authorization",
            "cookie",
            "set_cookie",
            "password",
            "access_token",
            "api_key",
            "db.connection_string",
            "db.user",
            "custom.password",
            "myapp.api-key",
            // DB bind parameters (exact and prefix)
            "db.query.parameters",
            "db.statement.parameters",
            "db.query.parameter.customer_id",
            "db.statement.parameter.1",
            // URL components
            "url.query",
            "url.fragment",
            "url.user_info",
            // end-user / tenant identifiers
            "enduser.id",
            "enduser.role",
            "enduser.scope",
            "enduser.scopes",
            "user.email",
            "user.account_id",
            "user.account_uuid",
            "organization.id",
            // free-form prompt content (Claude Code interaction span) — no safe partial form
            "user_prompt",
            "tenant.id",
            // case / dash insensitivity
            "URL.QUERY",
            "Http.Request.Header.Cookie"
    );

    private static final List<String> KEPT_KEYS = List.of(
            "url.full",
            "http.url",
            "http.target",
            "url.path",
            "http.request.method",
            "db.system",
            "db.statement",
            "http.response.status_code",
            "service.name",
            "safe.custom",
            // Claude Code keys deliberately kept (policy decision, 2026-08-18): session.id is
            // the only explicit session-correlation key and adds little linkability beyond the
            // agentId; user_prompt_length is the safe size signal for the dropped user_prompt;
            // full_command is kept as a key but its VALUE is reduced — see sanitizeCommand tests.
            "session.id",
            "user_prompt_length",
            "full_command"
    );

    @Test
    void sensitiveKeys() {
        for (String key : SENSITIVE_KEYS) {
            assertThat(OtlpSensitiveAttributeFilter.isSensitive(key)).as("sensitive: %s", key).isTrue();
        }
    }

    @Test
    void nonSensitiveKeys() {
        for (String key : KEPT_KEYS) {
            assertThat(OtlpSensitiveAttributeFilter.isSensitive(key)).as("kept: %s", key).isFalse();
        }
    }

    @Test
    void sanitizeUrlStripsQueryOnUrlKeysOnly() {
        assertThat(OtlpSensitiveAttributeFilter.sanitizeUrl("url.full", "https://example.com/a?b=c#d"))
                .isEqualTo("https://example.com/a");
        assertThat(OtlpSensitiveAttributeFilter.sanitizeUrl("http.target", "/orders?customer=42#x"))
                .isEqualTo("/orders");
        // non-URL keys keep their value verbatim
        assertThat(OtlpSensitiveAttributeFilter.sanitizeUrl("db.system", "mysql?x=1"))
                .isEqualTo("mysql?x=1");
    }

    @Test
    void sanitizeUrlHandlesNullAndEmpty() {
        assertThat(OtlpSensitiveAttributeFilter.sanitizeUrl("url.full", null)).isNull();
        assertThat(OtlpSensitiveAttributeFilter.sanitizeUrl("url.full", "")).isEmpty();
    }

    @Test
    void sanitizeCommandKeepsFirstCharacterOnly() {
        assertThat(OtlpSensitiveAttributeFilter.sanitizeCommand("full_command", "echo pinpoint-otel-probe"))
                .isEqualTo("e...");
        // worst cases that defeated word-level extraction: assignment prefix / secret in args
        assertThat(OtlpSensitiveAttributeFilter.sanitizeCommand("full_command", "API_KEY=sk-live-123 node app.js"))
                .isEqualTo("A...");
        assertThat(OtlpSensitiveAttributeFilter.sanitizeCommand("full_command", "curl -H \"Authorization: Bearer x\""))
                .isEqualTo("c...");
        // leading whitespace is not the marker character
        assertThat(OtlpSensitiveAttributeFilter.sanitizeCommand("full_command", "  git push"))
                .isEqualTo("g...");
        // single-character and non-BMP-safe handling
        assertThat(OtlpSensitiveAttributeFilter.sanitizeCommand("full_command", "a")).isEqualTo("a...");
        assertThat(OtlpSensitiveAttributeFilter.sanitizeCommand("full_command", "실행 스크립트")).isEqualTo("실...");
        // supplementary-plane leading character (surrogate pair, 2 Java chars): the whole code
        // point survives — a charAt-based cut would emit a lone surrogate (broken glyph)
        assertThat(OtlpSensitiveAttributeFilter.sanitizeCommand("full_command", "🚀 launch.sh"))
                .isEqualTo("🚀...");
    }

    @Test
    void sanitizeCommandLeavesOtherKeysAndBlanksAlone() {
        // non-command keys keep their value verbatim
        assertThat(OtlpSensitiveAttributeFilter.sanitizeCommand("db.statement", "select 1"))
                .isEqualTo("select 1");
        assertThat(OtlpSensitiveAttributeFilter.sanitizeCommand("full_command", null)).isNull();
        assertThat(OtlpSensitiveAttributeFilter.sanitizeCommand("full_command", "")).isEmpty();
        assertThat(OtlpSensitiveAttributeFilter.sanitizeCommand("full_command", "   ")).isEqualTo("...");
    }

    @Test
    void stripUrlRemovesUserInfoQueryAndFragment() {
        assertThat(OtlpSensitiveAttributeFilter.stripUrl(
                "https://alice:password@example.com:8443/orders?token=secret#receipt"))
                .isEqualTo("https://example.com:8443/orders");
        assertThat(OtlpSensitiveAttributeFilter.stripUrl("/orders?email=user@example.com#details"))
                .isEqualTo("/orders");
        assertThat(OtlpSensitiveAttributeFilter.stripUrl("https://alice@[::1]:8443/path"))
                .isEqualTo("https://[::1]:8443/path");
        assertThat(OtlpSensitiveAttributeFilter.stripUrl("//alice:password@example.com/path?token=secret"))
                .isEqualTo("//example.com/path");
        assertThat(OtlpSensitiveAttributeFilter.stripUrl("https://example.com/path"))
                .isEqualTo("https://example.com/path");
        assertThat(OtlpSensitiveAttributeFilter.stripUrl("urn:orders:42?token=secret"))
                .isEqualTo("urn:orders:42");
    }
}
