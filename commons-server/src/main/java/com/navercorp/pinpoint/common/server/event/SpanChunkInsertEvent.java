/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.event;

import java.util.Objects;

public class SpanChunkInsertEvent implements InsertEvent {
    private final ContextData contextData;
    private final boolean success;

    public SpanChunkInsertEvent(ContextData contextData, boolean success) {
        this.contextData = Objects.requireNonNull(contextData, "contextData");
        this.success = success;
    }

    @Override
    public ContextData getContextData() {
        return contextData;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

}
