/*
 * Copyright 2018 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.stat;

/**
 * @author Roy Kim
 */
public class DirectBufferBo extends AbstractAgentStatDataPoint {

    public static final long UNCOLLECTED_VALUE = UNCOLLECTED_LONG;

    private long directCount = UNCOLLECTED_VALUE;;
    private long directMemoryUsed = UNCOLLECTED_VALUE;;
    private long mappedCount = UNCOLLECTED_VALUE;;
    private long mappedMemoryUsed = UNCOLLECTED_VALUE;;

    public DirectBufferBo() {
        super(AgentStatType.DIRECT_BUFFER);
    }

    public long getDirectCount() {
        return directCount;
    }

    public void setDirectCount(long directCount) {
        this.directCount = directCount;
    }

    public long getDirectMemoryUsed() {
        return directMemoryUsed;
    }

    public void setDirectMemoryUsed(long directMemoryUsed) {
        this.directMemoryUsed = directMemoryUsed;
    }

    public long getMappedCount() {
        return mappedCount;
    }

    public void setMappedCount(long mappedCount) {
        this.mappedCount = mappedCount;
    }

    public long getMappedMemoryUsed() {
        return mappedMemoryUsed;
    }

    public void setMappedMemoryUsed(long mappedMemoryUsed) {
        this.mappedMemoryUsed = mappedMemoryUsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DirectBufferBo that = (DirectBufferBo) o;

        if (directCount != that.directCount) return false;
        if (directMemoryUsed != that.directMemoryUsed) return false;
        if (mappedCount != that.mappedCount) return false;
        return mappedMemoryUsed == that.mappedMemoryUsed;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (directCount ^ (directCount >>> 32));
        result = 31 * result + (int) (directMemoryUsed ^ (directMemoryUsed >>> 32));
        result = 31 * result + (int) (mappedCount ^ (mappedCount >>> 32));
        result = 31 * result + (int) (mappedMemoryUsed ^ (mappedMemoryUsed >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "DirectBufferBo{" +
                "directCount=" + directCount +
                ", directMemoryUsed=" + directMemoryUsed +
                ", mappedCount=" + mappedCount +
                ", mappedMemoryUsed=" + mappedMemoryUsed +
                "} " + super.toString();
    }
}
