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

package com.navercorp.pinpoint.otlp.trace.linkindex.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Connection settings of the link index store given by properties.
 *
 * @param mode           {@code cluster} or {@code standalone}
 * @param nodes          comma separated {@code host:port}
 * @param password       optional
 * @param connectTimeout socket connect timeout
 * @param commandTimeout command timeout
 */
public record LinkIndexRedisProperties(String mode, String nodes, String password,
                                       Duration connectTimeout, Duration commandTimeout) {

    public LinkIndexRedisProperties {
        Objects.requireNonNull(connectTimeout, "connectTimeout");
        Objects.requireNonNull(commandTimeout, "commandTimeout");
    }

    public LettuceLinkIndexRedisConnector.Mode parseMode() {
        if (mode == null || mode.isBlank()) {
            return LettuceLinkIndexRedisConnector.Mode.CLUSTER;
        }
        return LettuceLinkIndexRedisConnector.Mode.valueOf(mode.trim().toUpperCase(Locale.ROOT));
    }

    public List<String> parseNodes() {
        final List<String> list = new ArrayList<>();
        if (nodes == null) {
            return list;
        }
        for (String node : nodes.split(",")) {
            final String trimmed = node.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    public boolean hasNodes() {
        return !parseNodes().isEmpty();
    }

    public LinkIndexRedisConnector newConnector() {
        return new LettuceLinkIndexRedisConnector(parseMode(), parseNodes(), password, connectTimeout, commandTimeout);
    }

    @Override
    public String toString() {
        return "LinkIndexRedisProperties{" +
                "mode='" + mode + '\'' +
                ", nodes='" + nodes + '\'' +
                ", password=" + (password == null || password.isEmpty() ? "none" : "****") +
                ", connectTimeout=" + connectTimeout +
                ", commandTimeout=" + commandTimeout +
                '}';
    }
}
