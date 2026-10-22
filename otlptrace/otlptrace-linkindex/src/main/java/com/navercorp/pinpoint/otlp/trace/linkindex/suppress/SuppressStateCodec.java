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

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;

/**
 * Binary form of a {@link SuppressState} hash value: version, state code, then the five counters as VLong.
 */
public final class SuppressStateCodec {

    static final byte VERSION = 1;

    private SuppressStateCodec() {
    }

    public static byte[] encode(SuppressState state) {
        final Buffer buffer = new AutomaticBuffer(48);
        buffer.putByte(VERSION);
        buffer.putByte(state.state().getCode());
        buffer.putVLong(state.firstUnmatchedSlot());
        buffer.putVLong(state.lastSeenSlot());
        buffer.putVLong(state.unmatchedSlots());
        buffer.putVLong(state.unmatched());
        buffer.putVLong(state.activatedSlot());
        return buffer.copyBuffer();
    }

    /**
     * @throws IllegalArgumentException if the value is not a valid encoding
     */
    public static SuppressState decode(byte[] value) {
        if (value == null || value.length < 2) {
            throw new IllegalArgumentException("Empty value");
        }
        try {
            final Buffer buffer = new FixedBuffer(value);
            final byte version = buffer.readByte();
            if (version != VERSION) {
                throw new IllegalArgumentException("Unsupported version:" + version);
            }
            final SuppressState.State state = SuppressState.State.fromCode(buffer.readByte());
            final long firstUnmatchedSlot = buffer.readVLong();
            final long lastSeenSlot = buffer.readVLong();
            final long unmatchedSlots = buffer.readVLong();
            final long unmatched = buffer.readVLong();
            final long activatedSlot = buffer.readVLong();
            return new SuppressState(state, firstUnmatchedSlot, lastSeenSlot, unmatchedSlots, unmatched, activatedSlot);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Truncated value", e);
        }
    }
}
