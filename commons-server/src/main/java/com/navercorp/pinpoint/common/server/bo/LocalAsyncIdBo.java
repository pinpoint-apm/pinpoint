/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LocalAsyncIdBo {
    private final int asyncId;
    private final int sequence;

    public LocalAsyncIdBo(int asyncId, int sequence) {
        this.asyncId = asyncId;
        this.sequence = sequence;
    }

    public int getAsyncId() {
        return asyncId;
    }

    public int getSequence() {
        return sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalAsyncIdBo)) return false;

        LocalAsyncIdBo that = (LocalAsyncIdBo) o;

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
        return "LocalAsyncIdBo{" +
                "asyncId=" + asyncId +
                ", sequence=" + sequence +
                '}';
    }
}
