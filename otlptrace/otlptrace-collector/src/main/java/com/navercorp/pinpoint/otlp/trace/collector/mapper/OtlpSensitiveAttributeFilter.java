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

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fixed (non-configurable) security policy for OTel Span / SpanEvent attributes at the point they
 * are stored ({@link OtlpAttributeBoMapper#toAttributeBoList}). A collector exposes no per-attribute
 * redaction settings, so the rules carry no toggle.
 *
 * <ul>
 *   <li>{@link #isSensitive(String)} — attributes that must never be stored: HTTP headers, gRPC/RPC
 *       metadata and messaging headers; request/response bodies; credentials; the DB connection
 *       string and DB account (db.user); DB bind parameters; URL query/fragment/userinfo component
 *       keys; and end-user/tenant identifiers (id/role/scope, user/tenant).</li>
 *   <li>{@link #sanitizeUrl(String, String)} — URL-valued attributes ({@code url.full} /
 *       {@code http.url} / {@code http.target}) are kept but reduced to {@code scheme://host[:port]/path}
 *       (query, fragment and userinfo removed).</li>
 * </ul>
 *
 * <p>Only Span and SpanEvent attributes pass through {@code toAttributeBoList}; Span Event and
 * Span Link attributes use a separate JSON serializer and are intentionally out of scope.</p>
 */
final class OtlpSensitiveAttributeFilter {

    // URL-valued keys: kept, but the value is reduced to scheme://host[:port]/path.
    private static final Set<String> URL_KEYS = Set.of(
            "url.full",
            "http.url",
            "http.target"
    );

    // Dedicated URL component attributes — dropped (the sanitized URL already omits them).
    private static final Set<String> URL_COMPONENT_KEYS = Set.of(
            "url.query",
            "url.fragment",
            "url.user_info"
    );

    private static final Set<String> BODY_KEYS = Set.of(
            "http.request.body",
            "http.response.body",
            "messaging.message.body",
            "rpc.request.body",
            "rpc.response.body"
    );

    private static final Set<String> END_USER_KEYS = Set.of(
            "enduser.id",
            "enduser.role",
            "enduser.scope",
            "enduser.scopes",
            "enduser.email",
            "user.id",
            "user.email",
            "tenant.id",
            "tenant.name"
    );

    private static final Set<String> CREDENTIAL_KEYS = Set.of(
            "authorization",
            "proxy_authorization",
            "cookie",
            "set_cookie",
            "password",
            "passwd",
            "secret",
            "token",
            "access_token",
            "refresh_token",
            "auth_token",
            "api_key",
            "apikey",
            "client_secret",
            "credential",
            "credentials",
            "db.connection_string",
            // DB account name — always emitted by the OTel SQL client extractor (password already
            // stripped from the JDBC URL by the instrumentation). Dropped so the account is not stored.
            "db.user"
    );

    // DB bind parameters (concrete query argument values). Not consumed/promoted anywhere.
    private static final Set<String> DB_PARAMETER_KEYS = Set.of(
            "db.bind.parameters",
            "db.bind_parameters",
            "db.parameters",
            "db.query.parameters",
            "db.statement.parameters"
    );

    // Every attribute under these prefixes is dropped — headers/metadata are never stored.
    // gRPC metadata is captured under rpc.grpc.request/response.metadata.<name>
    // (CapturedGrpcMetadataUtil); the generic rpc.request/response.metadata.* variants are kept for
    // any other RPC system that follows the semconv naming.
    private static final List<String> HEADER_PREFIXES = List.of(
            "http.request.header.",
            "http.response.header.",
            "rpc.grpc.request.metadata.",
            "rpc.grpc.response.metadata.",
            "rpc.request.metadata.",
            "rpc.response.metadata.",
            "messaging.header."
    );

    private OtlpSensitiveAttributeFilter() {
    }

    /**
     * True when the attribute must never be stored. Matched case-insensitively, with {@code '-'}
     * treated as {@code '_'} (header/metadata name variants).
     */
    static boolean isSensitive(String key) {
        final String normalized = normalize(key);
        if (URL_COMPONENT_KEYS.contains(normalized)
                || BODY_KEYS.contains(normalized)
                || END_USER_KEYS.contains(normalized)) {
            return true;
        }
        for (String prefix : HEADER_PREFIXES) {
            if (normalized.startsWith(prefix)) {
                return true;
            }
        }
        return isCredential(normalized) || isDbParameter(normalized);
    }

    private static boolean isCredential(String normalized) {
        if (CREDENTIAL_KEYS.contains(normalized)) {
            return true;
        }
        final int lastDot = normalized.lastIndexOf('.');
        return lastDot >= 0 && CREDENTIAL_KEYS.contains(normalized.substring(lastDot + 1));
    }

    private static boolean isDbParameter(String normalized) {
        return DB_PARAMETER_KEYS.contains(normalized)
                || normalized.startsWith("db.query.parameter.")
                || normalized.startsWith("db.statement.parameter.");
    }

    /**
     * For URL-valued attributes, strips the query string, fragment and userinfo; every other key
     * returns its value unchanged.
     */
    static String sanitizeUrl(String key, String value) {
        if (value == null || value.isEmpty() || !URL_KEYS.contains(normalize(key))) {
            return value;
        }
        return stripUrl(value);
    }

    /**
     * Strips the query string, fragment and userinfo from a URL, keeping scheme://host[:port]/path.
     * A value with no {@code ://} and no leading {@code //} only has its query/fragment removed.
     */
    static String stripUrl(String value) {
        int fragmentIndex = value.indexOf('#');
        int queryIndex = value.indexOf('?');
        int end = value.length();
        if (fragmentIndex >= 0) {
            end = fragmentIndex;
        }
        if (queryIndex >= 0 && queryIndex < end) {
            end = queryIndex;
        }
        String sanitized = end == value.length() ? value : value.substring(0, end);

        int scheme = sanitized.indexOf("://");
        int authorityStart;
        if (scheme >= 0) {
            authorityStart = scheme + 3;
        } else if (sanitized.startsWith("//")) {
            authorityStart = 2;
        } else {
            return sanitized;
        }
        int authorityEnd = sanitized.indexOf('/', authorityStart);
        if (authorityEnd < 0) {
            authorityEnd = sanitized.length();
        }
        int userInfoEnd = sanitized.lastIndexOf('@', authorityEnd - 1);
        if (userInfoEnd < authorityStart) {
            return sanitized;
        }
        return sanitized.substring(0, authorityStart) + sanitized.substring(userInfoEnd + 1);
    }

    private static String normalize(String key) {
        return key.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
