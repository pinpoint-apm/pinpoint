/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultLocalAsyncId implements LocalAsyncId {
    private final int asyncId;
    private final int sequence;

    public DefaultLocalAsyncId(int asyncId, int sequence) {
        this.asyncId = asyncId;
        this.sequence = sequence;
    }

    @Override
    public int getAsyncId() {
        return asyncId;
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultLocalAsyncId)) return false;

        DefaultLocalAsyncId that = (DefaultLocalAsyncId) o;

        if (asyncId != that.asyncId) return false;
        return sequence == that.sequence;
    }

    @Override
    public int hashCode() {
        int result = asyncId;
        result = 31 * result + sequence;
        return result;
    }

    @Override
    public String toString() {
        return "DefaultLocalAsyncId{" +
                "asyncId=" + asyncId +
                ", sequence=" + sequence +
                '}';
    }
}
