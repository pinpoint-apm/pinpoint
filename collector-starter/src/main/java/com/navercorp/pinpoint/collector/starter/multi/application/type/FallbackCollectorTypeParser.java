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

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class FallbackCollectorTypeParser implements CollectorTypeParser {

    private final CollectorTypeParser main;
    private final CollectorTypeParser fallback;

    public FallbackCollectorTypeParser(CollectorTypeParser main, CollectorTypeParser fallback) {
        this.main = Objects.requireNonNull(main, "main");
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    @Override
    public CollectorTypeSet parse(String[] args) {
        CollectorTypeSet collectorTypeSet = main.parse(args);
        if (collectorTypeSet == null || collectorTypeSet.isEmpty()) {
            return fallback.parse(args);
        }
        return collectorTypeSet;
    }

}
