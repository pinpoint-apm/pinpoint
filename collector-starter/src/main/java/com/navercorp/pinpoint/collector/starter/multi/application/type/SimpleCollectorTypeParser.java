/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.starter.multi.application.type;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SimpleCollectorTypeParser implements CollectorTypeParser {

    public static final String COLLECTOR_TYPE_KEY = "pinpoint.collector.type";
    private final String key;

    public SimpleCollectorTypeParser() {
        this(COLLECTOR_TYPE_KEY);
    }

    public SimpleCollectorTypeParser(String key) {
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public CollectorTypeSet parse(String[] args) {
        ApplicationArguments arguments = new DefaultApplicationArguments(args);
        return parse(arguments);
    }

    public CollectorTypeSet parse(ApplicationArguments arguments) {
        List<String> optionValues = arguments.getOptionValues(key);
        if (CollectionUtils.isEmpty(optionValues)) {
            return new CollectorTypeSet(Set.of());
        }

        Set<CollectorType> collect = optionValues.stream()
                .flatMap(type -> StringUtils.tokenizeToStringList(type, ",").stream())
                .map(String::toUpperCase)
                .map(CollectorType::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new CollectorTypeSet(collect);
    }
}
