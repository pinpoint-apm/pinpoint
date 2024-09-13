/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.monitor.dao.hbase;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author intr3p1d
 */
public class MetricNameExtractor {

    static String extractName(String name) {
        int atIndex = name.lastIndexOf('.');
        if (atIndex != -1) {
            return name.substring(0, atIndex);
        } else {
            return name;
        }
    }

    static Tags extractTags(String name) {
        String regex = ".*\\.([0-9a-fA-F\\-]{36})@([0-9a-fA-F]+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);

        if (matcher.matches()) {
            String uuid = matcher.group(1);
            String hash = matcher.group(2);

            return Tags.of(
                    Tag.of("clusterId", uuid),
                    Tag.of("connectionHash", hash)
            );
        } else {
            return Tags.empty();
        }
    }

}
