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
package com.navercorp.pinpoint.alarm.service;

import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves a persisted data source code back to its definition.
 *
 * <p>With one enum, {@code valueOf} did this and the compiler guaranteed the codes
 * were distinct. Once the set is contributed per module that guarantee moves here,
 * so two contributors claiming the same code is a startup failure rather than a
 * silent winner -- the code is what {@code alarm_rule_v2.data_source} stores, and
 * resolving it to the wrong definition would validate a rule against the wrong
 * metrics and filter keys.
 *
 * <p>An unknown code is not a startup failure: a deployable may legitimately run
 * without the module that owns a rule's data source, and the rest of that
 * deployable's rules still have to be evaluated. {@link #find(String)} lets the
 * caller degrade per rule; {@link #get(String)} is for callers that cannot carry on
 * without a definition, including the request paths that turn the resulting
 * {@code IllegalArgumentException} into a 400.
 */
public class AlarmDataSourceRegistry {

    private final Map<String, AlarmDataSource> byName;

    public AlarmDataSourceRegistry(List<AlarmDataSourceProvider> providers) {
        Objects.requireNonNull(providers, "providers");

        Map<String, AlarmDataSource> collected = new LinkedHashMap<>();
        for (AlarmDataSourceProvider provider : providers) {
            for (AlarmDataSource dataSource : provider.dataSources()) {
                AlarmDataSource previous = collected.putIfAbsent(dataSource.name(), dataSource);
                if (previous != null) {
                    throw new IllegalStateException(
                            "Duplicate alarm data source code '" + dataSource.name() + "': "
                                    + previous.getClass().getName() + " and "
                                    + dataSource.getClass().getName());
                }
            }
        }
        // LinkedHashMap, not Map.copyOf: an immutable map randomizes its iteration
        // order per JVM, and all() feeds the rule editor catalog, so the dropdown
        // would reorder itself after every restart.
        this.byName = Collections.unmodifiableMap(collected);
    }

    /** The definition for a persisted code, or empty when no installed module owns it. */
    public Optional<AlarmDataSource> find(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byName.get(name));
    }

    /** As {@link #find(String)}, but throws when no installed module owns the code. */
    public AlarmDataSource get(String name) {
        return find(name).orElseThrow(() -> new IllegalArgumentException(
                "Unknown alarm data source: " + name));
    }

    /** Every installed data source, for the rule editor catalog. */
    public List<AlarmDataSource> all() {
        return List.copyOf(byName.values());
    }
}
