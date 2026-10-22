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

import java.util.Objects;

/**
 * State of one CLIENT identifier in the suppression list, kept by the join job.
 *
 * @param state              {@link State#CANDIDATE} while the identifier is observed, {@link State#ACTIVE} once the
 *                           collectors stop indexing it
 * @param firstUnmatchedSlot first slot of the current run of unmatched slots
 * @param lastSeenSlot       last slot with entries of this identifier (matched or not)
 * @param unmatchedSlots     slots with at least the policy minimum of unmatched entries in the current run
 * @param unmatched          unmatched entries accumulated over the current run
 * @param activatedSlot      slot of the ACTIVE transition, 0 while CANDIDATE
 */
public record SuppressState(State state,
                            long firstUnmatchedSlot,
                            long lastSeenSlot,
                            long unmatchedSlots,
                            long unmatched,
                            long activatedSlot) {

    public SuppressState {
        Objects.requireNonNull(state, "state");
    }

    public static SuppressState candidate(long slot, long unmatched) {
        return new SuppressState(State.CANDIDATE, slot, slot, 1, unmatched, 0);
    }

    public boolean isActive() {
        return state == State.ACTIVE;
    }

    /**
     * @param countSlot {@code true} if this slot counts toward the activation ({@code unmatched >= minimum})
     */
    public SuppressState seen(long slot, long unmatched, boolean countSlot) {
        return new SuppressState(state, firstUnmatchedSlot, Math.max(lastSeenSlot, slot),
                countSlot ? unmatchedSlots + 1 : unmatchedSlots, this.unmatched + unmatched, activatedSlot);
    }

    public SuppressState activated(long slot) {
        return new SuppressState(State.ACTIVE, firstUnmatchedSlot, lastSeenSlot, unmatchedSlots, unmatched, slot);
    }

    public enum State {
        CANDIDATE((byte) 1),
        ACTIVE((byte) 2);

        private final byte code;

        State(byte code) {
            this.code = code;
        }

        public byte getCode() {
            return code;
        }

        public static State fromCode(byte code) {
            for (State state : values()) {
                if (state.code == code) {
                    return state;
                }
            }
            throw new IllegalArgumentException("Unknown state code:" + code);
        }
    }
}
