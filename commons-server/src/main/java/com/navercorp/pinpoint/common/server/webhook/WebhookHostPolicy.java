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
package com.navercorp.pinpoint.common.server.webhook;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Hosts allowed to resolve into private address ranges.
 * <p>
 * Deployments that run entirely on a public network keep the default {@link #denyAll()},
 * which preserves the strict behaviour of blocking every private range. Deployments whose
 * legitimate webhook targets live on an internal network list those targets here, either as
 * an exact host name or as a host suffix.
 * <p>
 * Only ranges classified as private are subject to this allowlist. Loopback, link-local,
 * and the other always-blocked ranges in {@link WebhookUrlValidator} stay blocked regardless.
 * {@link WebhookUrlValidator} does not consult this policy for an IP literal host, so an
 * internal address is reachable only through a host name that was explicitly allowed.
 */
public final class WebhookHostPolicy {

    private static final WebhookHostPolicy DENY_ALL = new WebhookHostPolicy(List.of(), List.of());

    private final Set<String> allowedHosts;
    private final List<String> allowedHostSuffixes;

    public WebhookHostPolicy(List<String> allowedHosts, List<String> allowedHostSuffixes) {
        this.allowedHosts = normalizeHosts(allowedHosts);
        this.allowedHostSuffixes = normalizeSuffixes(allowedHostSuffixes);
    }

    public static WebhookHostPolicy denyAll() {
        return DENY_ALL;
    }

    public boolean isAllowed(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }

        String normalizedHost = normalize(host);
        if (allowedHosts.contains(normalizedHost)) {
            return true;
        }
        for (String suffix : allowedHostSuffixes) {
            if (normalizedHost.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> normalizeHosts(List<String> hosts) {
        if (hosts == null) {
            return Set.of();
        }

        Set<String> normalizedHosts = new LinkedHashSet<>();
        for (String host : hosts) {
            String normalizedHost = normalize(host);
            if (!normalizedHost.isEmpty()) {
                normalizedHosts.add(normalizedHost);
            }
        }
        return Set.copyOf(normalizedHosts);
    }

    /**
     * A suffix always starts with a dot so that {@code .io.naver.com} matches
     * {@code n8n.io.naver.com} but not {@code evil-io.naver.com}.
     */
    private static List<String> normalizeSuffixes(List<String> suffixes) {
        if (suffixes == null) {
            return List.of();
        }

        Set<String> normalizedSuffixes = new LinkedHashSet<>();
        for (String suffix : suffixes) {
            String normalizedSuffix = normalize(suffix);
            if (normalizedSuffix.isEmpty()) {
                continue;
            }
            if (!normalizedSuffix.startsWith(".")) {
                normalizedSuffix = "." + normalizedSuffix;
            }
            normalizedSuffixes.add(normalizedSuffix);
        }
        return List.copyOf(normalizedSuffixes);
    }

    public int allowedHostCount() {
        return allowedHosts.size();
    }

    public int allowedHostSuffixCount() {
        return allowedHostSuffixes.size();
    }

    private static String normalize(String host) {
        if (host == null) {
            return "";
        }

        String normalizedHost = host.trim().toLowerCase(Locale.ROOT);
        while (normalizedHost.endsWith(".")) {
            normalizedHost = normalizedHost.substring(0, normalizedHost.length() - 1);
        }
        return normalizedHost;
    }

    @Override
    public String toString() {
        return "WebhookHostPolicy{" +
                "allowedHosts=" + allowedHosts +
                ", allowedHostSuffixes=" + allowedHostSuffixes +
                '}';
    }
}