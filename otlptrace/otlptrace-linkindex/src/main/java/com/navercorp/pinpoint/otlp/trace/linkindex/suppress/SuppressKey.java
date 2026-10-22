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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Identifier of a CLIENT link index entry for the suppression list: {@code applicationName + US + destinationId}
 * (US = 0x1f, a character that appears in neither). Only CLIENT entries are identified: a SERVER entry
 * does not know its caller, so it cannot be told apart from the SERVER entries of the same application
 * that do match.
 */
public final class SuppressKey {

    static final char SEPARATOR = '\u001f';
    /** separator of the human readable form used in logs and in the force / pin settings */
    static final char DISPLAY_SEPARATOR = '|';

    private SuppressKey() {
    }

    public static String of(String applicationName, String destinationId) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(destinationId, "destinationId");
        return applicationName + SEPARATOR + destinationId;
    }

    public static String applicationName(String key) {
        final int index = key.indexOf(SEPARATOR);
        return index < 0 ? key : key.substring(0, index);
    }

    public static String destinationId(String key) {
        final int index = key.indexOf(SEPARATOR);
        return index < 0 ? null : key.substring(index + 1);
    }

    /** {@code application|destination}, for logs */
    public static String display(String key) {
        return key.replace(SEPARATOR, DISPLAY_SEPARATOR);
    }

    /**
     * Parses a comma separated list of {@code application|destination} (one CLIENT identifier) or
     * {@code application} (every CLIENT entry of that application). Items are trimmed; empty items are skipped.
     */
    public static Rules parseRules(String text) {
        if (text == null || text.isBlank()) {
            return Rules.EMPTY;
        }
        final Set<String> applications = new HashSet<>();
        final Set<String> keys = new HashSet<>();
        for (String item : text.split(",")) {
            final String rule = item.trim();
            if (rule.isEmpty()) {
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
        return new Rules(Set.copyOf(applications), Set.copyOf(keys));
    }

    /**
     * Static rules from the settings: whole applications and single CLIENT identifiers.
     */
    public record Rules(Set<String> applications, Set<String> keys) {
        public static final Rules EMPTY = new Rules(Set.of(), Set.of());

        public Rules {
            applications = Set.copyOf(Objects.requireNonNull(applications, "applications"));
            keys = Set.copyOf(Objects.requireNonNull(keys, "keys"));
        }

        public boolean isEmpty() {
            return applications.isEmpty() && keys.isEmpty();
        }

        public boolean matches(String applicationName, String key) {
            return applications.contains(applicationName) || keys.contains(key);
        }

        @Override
        public String toString() {
            final Set<String> display = new HashSet<>(applications);
            for (String key : keys) {
                display.add(SuppressKey.display(key));
            }
            return display.toString();
        }
    }
}
