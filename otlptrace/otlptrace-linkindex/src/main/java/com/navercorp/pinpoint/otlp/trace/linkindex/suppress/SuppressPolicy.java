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
 * When the join job activates, keeps and drops suppression identifiers. Slots are one minute.
 *
 * @param activateAfterSlots   slots since the first unmatched slot before an identifier may become ACTIVE (a day)
 * @param activateMinSlots     slots that met {@code minUnmatchedPerSlot} before an identifier may become ACTIVE;
 *                             together with {@code activateAfterSlots} this keeps a gap of the job (or of the
 *                             traffic) from activating anything early
 * @param minUnmatchedPerSlot  unmatched entries a slot needs to count: smaller identifiers are not tracked, which
 *                             keeps destinations with changing IPs from filling the list
 * @param idleSlots            slots without entries after which an identifier is forgotten
 * @param maxKeys              size cap of the list; the longest unseen identifiers go first
 */
public record SuppressPolicy(long activateAfterSlots,
                             long activateMinSlots,
                             int minUnmatchedPerSlot,
                             long idleSlots,
                             int maxKeys) {

    public static final SuppressPolicy DEFAULT = new SuppressPolicy(1440, 1440, 100, 7 * 1440, 1000);

    public SuppressPolicy {
        if (activateAfterSlots < 1 || activateMinSlots < 1 || minUnmatchedPerSlot < 1 || idleSlots < 1 || maxKeys < 1) {
            throw new IllegalArgumentException("activateAfterSlots, activateMinSlots, minUnmatchedPerSlot, idleSlots and maxKeys must be positive");
        }
    }
}
