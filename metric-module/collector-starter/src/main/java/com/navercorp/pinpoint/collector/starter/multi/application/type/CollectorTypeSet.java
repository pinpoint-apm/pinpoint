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


import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Set;

public class CollectorTypeSet {
    private final Set<CollectorType> types;

    public CollectorTypeSet(Set<CollectorType> types) {
        this.types = Objects.requireNonNull(types, "types");
    }

    public boolean isEmpty() {
        return types.isEmpty();
    }

    public boolean hasType(CollectorType type) {
        Objects.requireNonNull(type, "type");

        for (CollectorType collectorType : types) {
            if (collectorType.hasType(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return StringUtils.join(types, ",");
    }
}
