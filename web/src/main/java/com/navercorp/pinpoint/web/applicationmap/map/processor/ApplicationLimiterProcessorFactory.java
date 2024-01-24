/*
 * Copyright 2024 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.map.processor;

import java.util.function.Supplier;

public class ApplicationLimiterProcessorFactory implements Supplier<LinkDataMapProcessor> {

    private final int limit;

    public ApplicationLimiterProcessorFactory(int limit) {
        this.limit = limit;
    }

    @Override
    public LinkDataMapProcessor get() {
        if (limit == -1) {
            return LinkDataMapProcessor.NO_OP;
        }
        return new ApplicationLimiterProcessor(limit);
    }
}
