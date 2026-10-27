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

package com.navercorp.pinpoint.otlp.trace.linkindex.suppress;

import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkEntry;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkKind;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Identifier of a link index entry for the suppression list.
 * <ul>
 *     <li>CLIENT: {@code applicationName + US + destinationId} (US = 0x1f, a character that appears in neither).</li>
 *     <li>SERVER: {@code RS + "S" + US + applicationName + US + parentClass} (RS = 0x1e, so that a SERVER key can
 *     never equal a CLIENT key). A SERVER entry does not know its caller, so its application alone would mix the
 *     callers that match with those that cannot; the parent span class tells them apart:
 *     {@value #PARENT_ROOT} when the parent span id equals the low 8 bytes of the trace id, which is how B3 (Brave)
 *     and Jaeger derive the id of a root span &mdash; the caller made the call from its root span, without a client
 *     span that could be indexed &mdash; and {@value #PARENT_OTHER} for every other parent (OTel SDK ids are random).
 *     The class only splits the identifier; whether it is suppressed is still decided by the join outcome.</li>
 * </ul>
 */
public final class SuppressKey {

    static final char SEPARATOR = '\u001f';
    /** first character of a SERVER key; a CLIENT key starts with the application name */
    static final char SERVER_MARKER = '\u001e';
    static final String SERVER_PREFIX = SERVER_MARKER + "S" + SEPARATOR;
    /** separator of the human readable form used in logs and in the force / pin settings */
    static final char DISPLAY_SEPARATOR = '|';
    /**
     * display prefix of a SERVER identifier or SERVER application rule: {@code server:application|class},
     * {@code server:application}. An application name cannot contain {@code :} ({@code [a-zA-Z0-9._-]}), so no
     * CLIENT rule ({@code application|destination} or {@code application}) can start with it.
     */
    static final String SERVER_DISPLAY_PREFIX = "server:";

    /** parent span id = low 8 bytes of the trace id: the parent is a B3 / Jaeger root span */
    public static final String PARENT_ROOT = "root";
    public static final String PARENT_OTHER = "other";

    private SuppressKey() {
    }

    /** CLIENT identifier */
    public static String of(String applicationName, String destinationId) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(destinationId, "destinationId");
        return applicationName + SEPARATOR + destinationId;
    }

    /** SERVER identifier */
    public static String ofServer(String applicationName, String parentClass) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(parentClass, "parentClass");
        return SERVER_PREFIX + applicationName + SEPARATOR + parentClass;
    }

    /** the identifier of an entry: {@link #of} for a CLIENT entry, {@link #ofServer} for a SERVER entry */
    public static String of(LinkEntry entry) {
        if (entry.kind() == LinkKind.SERVER) {
            return ofServer(entry.applicationName(), serverClass(entry.traceId(), entry.spanId()));
        }
        return of(entry.applicationName(), entry.destinationId());
    }

    /**
     * @param traceId      16-byte trace id
     * @param parentSpanId the parent span id of the SERVER span
     * @return {@link #PARENT_ROOT} when the parent span id equals the low 8 bytes of the trace id, otherwise
     * {@link #PARENT_OTHER}
     */
    public static String serverClass(byte[] traceId, long parentSpanId) {
        Objects.requireNonNull(traceId, "traceId");
        if (traceId.length != LinkEntry.TRACE_ID_LENGTH) {
            throw new IllegalArgumentException("traceId must be " + LinkEntry.TRACE_ID_LENGTH + " bytes:" + traceId.length);
        }
        return BytesUtils.bytesToLong(traceId, 8) == parentSpanId ? PARENT_ROOT : PARENT_OTHER;
    }

    public static boolean isServer(String key) {
        return key.startsWith(SERVER_PREFIX);
    }

    public static LinkKind kind(String key) {
        return isServer(key) ? LinkKind.SERVER : LinkKind.CLIENT;
    }

    public static String applicationName(String key) {
        if (isServer(key)) {
            final int start = SERVER_PREFIX.length();
            final int index = key.indexOf(SEPARATOR, start);
            return index < 0 ? key.substring(start) : key.substring(start, index);
        }
        final int index = key.indexOf(SEPARATOR);
        return index < 0 ? key : key.substring(0, index);
    }

    /** the destination of a CLIENT key, {@code null} for a SERVER key */
    public static String destinationId(String key) {
        if (isServer(key)) {
            return null;
        }
        final int index = key.indexOf(SEPARATOR);
        return index < 0 ? null : key.substring(index + 1);
    }

    /** the parent class of a SERVER key, {@code null} for a CLIENT key */
    public static String parentClass(String key) {
        if (!isServer(key)) {
            return null;
        }
        final int index = key.indexOf(SEPARATOR, SERVER_PREFIX.length());
        return index < 0 ? null : key.substring(index + 1);
    }

    /** {@code application|destination} or {@code server:application|class}, for logs and the rule syntax */
    public static String display(String key) {
        if (isServer(key)) {
            return SERVER_DISPLAY_PREFIX + key.substring(SERVER_PREFIX.length()).replace(SEPARATOR, DISPLAY_SEPARATOR);
        }
        return key.replace(SEPARATOR, DISPLAY_SEPARATOR);
    }

    /**
     * Parses a comma separated list of rules. Items are trimmed; empty items are skipped.
     * <ul>
     *     <li>{@code application|destination}: one CLIENT identifier</li>
     *     <li>{@code application}: every CLIENT and SERVER entry of that application</li>
     *     <li>{@code server:application|class}: one SERVER identifier ({@value #PARENT_ROOT} or {@value #PARENT_OTHER})</li>
     *     <li>{@code server:application}: every SERVER entry of that application</li>
     * </ul>
     */
    public static Rules parseRules(String text) {
        if (text == null || text.isBlank()) {
            return Rules.EMPTY;
        }
        final Set<String> applications = new HashSet<>();
        final Set<String> serverApplications = new HashSet<>();
        final Set<String> keys = new HashSet<>();
        for (String item : text.split(",")) {
            final String rule = item.trim();
            if (rule.isEmpty()) {
                continue;
            }
            if (rule.startsWith(SERVER_DISPLAY_PREFIX)) {
                parseServerRule(rule, serverApplications, keys);
                continue;
            }
            final int index = rule.indexOf(DISPLAY_SEPARATOR);
            if (index < 0) {
                applications.add(rule);
            } else {
                final String application = rule.substring(0, index).trim();
                final String destination = rule.substring(index + 1).trim();
                if (application.isEmpty() || destination.isEmpty()) {
                    throw new IllegalArgumentException("Invalid suppression rule: '" + rule + "' (application|destination)");
                }
                keys.add(of(application, destination));
            }
        }
        return new Rules(Set.copyOf(applications), Set.copyOf(keys), Set.copyOf(serverApplications));
    }

    private static void parseServerRule(String rule, Set<String> serverApplications, Set<String> keys) {
        final String body = rule.substring(SERVER_DISPLAY_PREFIX.length());
        final int index = body.indexOf(DISPLAY_SEPARATOR);
        final String application = (index < 0 ? body : body.substring(0, index)).trim();
        if (application.isEmpty()) {
            throw new IllegalArgumentException("Invalid suppression rule: '" + rule + "' (server:application or server:application|class)");
        }
        if (index < 0) {
            serverApplications.add(application);
            return;
        }
        final String parentClass = body.substring(index + 1).trim();
        if (!PARENT_ROOT.equals(parentClass) && !PARENT_OTHER.equals(parentClass)) {
            throw new IllegalArgumentException("Invalid suppression rule: '" + rule + "' (class must be "
                    + PARENT_ROOT + " or " + PARENT_OTHER + ")");
        }
        keys.add(ofServer(application, parentClass));
    }

    /**
     * Static rules from the settings: whole applications (both kinds), single identifiers (either kind) and the
     * SERVER entries of whole applications.
     */
    public record Rules(Set<String> applications, Set<String> keys, Set<String> serverApplications) {
        public static final Rules EMPTY = new Rules(Set.of(), Set.of(), Set.of());

        public Rules {
            applications = Set.copyOf(Objects.requireNonNull(applications, "applications"));
            keys = Set.copyOf(Objects.requireNonNull(keys, "keys"));
            serverApplications = Set.copyOf(Objects.requireNonNull(serverApplications, "serverApplications"));
        }

        public Rules(Set<String> applications, Set<String> keys) {
            this(applications, keys, Set.of());
        }

        public boolean isEmpty() {
            return applications.isEmpty() && keys.isEmpty() && serverApplications.isEmpty();
        }

        /**
         * @param applicationName the application of the entry
         * @param key             {@link #of} or {@link #ofServer} of the entry
         */
        public boolean matches(String applicationName, String key) {
            if (applications.contains(applicationName) || keys.contains(key)) {
                return true;
            }
            return !serverApplications.isEmpty() && isServer(key) && serverApplications.contains(applicationName);
        }

        @Override
        public String toString() {
            final Set<String> display = new HashSet<>(applications);
            for (String key : keys) {
                display.add(SuppressKey.display(key));
            }
            for (String application : serverApplications) {
                display.add(SERVER_DISPLAY_PREFIX + application);
            }
            return display.toString();
        }
    }
}
