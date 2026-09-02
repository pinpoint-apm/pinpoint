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

package com.navercorp.pinpoint.otlp.trace.collector.mapper.stacktrace;

import java.util.ArrayList;
import java.util.List;

/**
 * Bounded frame collector shared by all parsers. The flattened stacktrace is client-supplied, so
 * the frame count is unbounded without this cap; parsers stop as soon as {@link #add} returns
 * {@code false} and the caller reads {@link #isTruncated()} for the truncation metric.
 */
public final class StackFrameSink {

    private final int maxFrames;
    private final List<StackFrame> frames = new ArrayList<>();
    private boolean truncated = false;

    /**
     * @param maxFrames maximum frames to keep; {@code <= 0} means unlimited
     */
    public StackFrameSink(int maxFrames) {
        this.maxFrames = maxFrames;
    }

    /**
     * Adds a frame; returns {@code false} (and marks this sink truncated) once the cap is reached.
     */
    public boolean add(StackFrame frame) {
        if (maxFrames > 0 && frames.size() >= maxFrames) {
            truncated = true;
            return false;
        }
        frames.add(frame);
        return true;
    }

    public List<StackFrame> frames() {
        return frames;
    }

    public boolean isTruncated() {
        return truncated;
    }
}
