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

package com.navercorp.pinpoint.otlp.trace.collector;

import java.util.ArrayList;
import java.util.List;

public class OtlpTraceCollectorRejectedSpan {
    private long count;
    private List<String> messageList = new ArrayList<>();

    public long count() {
        return count;
    }

    public void addCount(long count) {
        this.count += count;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        for (String message : messageList) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(message);
        }

        return sb.toString();
    }

    public void putMessage(String message) {
        messageList.add(message);
    }
}
