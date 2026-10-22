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

/**
 * Per {@link SuppressKey} outcome of one joined slot: CLIENT entries that found their SERVER partner, and CLIENT
 * entries of the previous slot that never did (the final verdict; an entry still waiting for the next slot is
 * neither).
 */
public record LinkCounts(long matched, long unmatched) {
    public static final LinkCounts NONE = new LinkCounts(0, 0);

    public LinkCounts {
        if (matched < 0 || unmatched < 0) {
            throw new IllegalArgumentException("counts must not be negative");
        }
    }

    public LinkCounts plus(long matched, long unmatched) {
        return new LinkCounts(this.matched + matched, this.unmatched + unmatched);
    }
}
