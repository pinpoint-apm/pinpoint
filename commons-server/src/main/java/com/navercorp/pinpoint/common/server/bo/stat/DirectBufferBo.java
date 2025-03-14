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
public class DirectBufferBo extends AbstractStatDataPoint {

    public static final long UNCOLLECTED_VALUE = -1;


    private long directCount = UNCOLLECTED_VALUE;
    private long directMemoryUsed = UNCOLLECTED_VALUE;
    private long mappedCount = UNCOLLECTED_VALUE;
    private long mappedMemoryUsed = UNCOLLECTED_VALUE;

    public DirectBufferBo(DataPoint point) {
        super(point);
    }

    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.DIRECT_BUFFER;
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
    public String toString() {
        return "DirectBufferBo{" +
                "point=" + point +
                ", directCount=" + directCount +
                ", directMemoryUsed=" + directMemoryUsed +
                ", mappedCount=" + mappedCount +
                ", mappedMemoryUsed=" + mappedMemoryUsed +
                '}';
    }
}
